import React, { useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import { createCustomer, getAdminTransactions, getCustomerBalance, getCustomers, getCustomerTransactions, submitCustomerTopUp } from "./api/bankingApi";
import { AdminPanel } from "./components/AdminPanel";
import { CustomerPanel } from "./components/CustomerPanel";
import { RoleSelector } from "./components/RoleSelector";
import { formatCurrency } from "./components/formatCurrency";
import "./styles.css";

function App() {
  const [activeRole, setActiveRole] = useState("admin");
  const [customers, setCustomers] = useState([]);
  const [selectedCustomerId, setSelectedCustomerId] = useState("cust-1001");
  const [adminTransactions, setAdminTransactions] = useState([]);
  const [customerTransactions, setCustomerTransactions] = useState([]);
  const [customerBalance, setCustomerBalance] = useState(null);
  const [topUpAmount, setTopUpAmount] = useState("100");
  const [pin, setPin] = useState("");
  const [statusMessage, setStatusMessage] = useState("");
  const [lastSyncedAt, setLastSyncedAt] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isCreatingCustomer, setIsCreatingCustomer] = useState(false);
  const [customerCreationMessage, setCustomerCreationMessage] = useState("");
  const [customerCreationStatus, setCustomerCreationStatus] = useState("");

  const selectedCustomer = useMemo(
    () => customers.find((customer) => customer.id === selectedCustomerId),
    [customers, selectedCustomerId]
  );

  const dashboardMetrics = useMemo(() => {
    const approvedTransactions = activeRole === "admin" ? adminTransactions : customerTransactions;
    const approvedCount = approvedTransactions.filter((transaction) => transaction.approved).length;
    const declinedCount = approvedTransactions.length - approvedCount;

    return activeRole === "admin"
      ? [
          { label: "Customers", value: customers.length, note: "In-memory records" },
          { label: "Approved", value: approvedCount, note: "Successful transactions" },
          { label: "Declined", value: declinedCount, note: "Failed transactions" }
        ]
      : [
          { label: "Available Balance", value: formatCurrency(customerBalance?.balance), note: selectedCustomer?.name || "Selected customer" },
          { label: "Transactions", value: customerTransactions.length, note: "Personal activity log" },
          { label: "Customer ID", value: selectedCustomerId, note: selectedCustomer?.maskedCard ? `Card ${selectedCustomer.maskedCard}` : "Choose a customer" }
        ];
  }, [activeRole, adminTransactions, customerTransactions, customers.length, customerBalance?.balance, selectedCustomer?.name, selectedCustomer?.maskedCard, selectedCustomerId]);

  const dashboardCopy =
    activeRole === "admin"
      ? {
          eyebrow: "Operations Console",
          title: "Super Admin Transaction Monitor",
          description: "Review system-wide payments, routing outcomes, and processing health from one clean dashboard."
        }
      : {
          eyebrow: "Customer Portal",
          title: "Personal Banking Workspace",
          description: "Check balance, review your history, and submit secure top-ups through a simple customer view."
        };

  async function refreshAdminData() {
    const [customerList, transactionList] = await Promise.all([
      getCustomers(),
      getAdminTransactions()
    ]);
    setCustomers(customerList);
    setAdminTransactions(transactionList);
    setLastSyncedAt(new Date().toLocaleString());
  }

  async function refreshCustomerData(customerId = selectedCustomerId) {
    const [balanceDetails, transactionList] = await Promise.all([
      getCustomerBalance(customerId),
      getCustomerTransactions(customerId)
    ]);
    setCustomerBalance(balanceDetails);
    setCustomerTransactions(transactionList);
    setLastSyncedAt(new Date().toLocaleString());
  }

  async function handleTopUpSubmit(event) {
    event.preventDefault();
    setStatusMessage("");
    setIsSubmitting(true);

    try {
      const result = await submitCustomerTopUp(selectedCustomerId, topUpAmount, pin);
      setStatusMessage(result.approved ? `Top-up approved. New balance: ${formatCurrency(result.balance)}` : result.reason);
      setPin("");

      await Promise.all([refreshAdminData(), refreshCustomerData()]);
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleCustomerCreate(customerDetails) {
    setIsCreatingCustomer(true);
    setCustomerCreationMessage("");
    setCustomerCreationStatus("");
    try {
      const result = await createCustomer(customerDetails.name, customerDetails.cardNumber, customerDetails.pin);
      if (result.reason || result.error) {
        setCustomerCreationMessage(result.reason || result.error);
        setCustomerCreationStatus("error");
        return false;
      }
      setCustomerCreationMessage(`${result.name} created successfully (${result.id}).`);
      setCustomerCreationStatus("success");
      await refreshAdminData();
      return true;
    } finally {
      setIsCreatingCustomer(false);
    }
  }

  useEffect(() => {
    refreshAdminData().then(() => refreshCustomerData(selectedCustomerId));
  }, []);

  useEffect(() => {
    refreshCustomerData(selectedCustomerId);
  }, [selectedCustomerId]);

  return (
    <main className="appShell">
      <header className="topBar">
        <div className="brand">
          <div className="brandMark" aria-hidden="true">B</div>
          <div className="brandCopy">
            <h1>Banking Operations</h1>
            <p className="topBarText">Secure transaction management</p>
          </div>
        </div>
        <RoleSelector activeRole={activeRole} onRoleChange={setActiveRole} />
      </header>

      <section className="panel heroPanel">
        <div className="heroHeader">
          <div>
            <p className="eyebrow">{dashboardCopy.eyebrow}</p>
            <h2>{dashboardCopy.title}</h2>
            <p className="heroText">{dashboardCopy.description}</p>
          </div>
          <div className="heroStatus">
            <span className="statusDot" />
            System operational
          </div>
        </div>

        <div className="metricsGrid">
          {dashboardMetrics.map((metric) => (
            <article key={metric.label} className="metricCard">
              <span>{metric.label}</span>
              <strong>{metric.value}</strong>
              <small>{metric.note}</small>
            </article>
          ))}
        </div>

        <div className="heroFooter">
          <span>Last updated</span>
          <strong>{lastSyncedAt || "Waiting for data"}</strong>
        </div>
      </section>

      {activeRole === "admin" ? (
        <AdminPanel
          transactions={adminTransactions}
          isCreatingCustomer={isCreatingCustomer}
          customerCreationMessage={customerCreationMessage}
          customerCreationStatus={customerCreationStatus}
          onCreateCustomer={handleCustomerCreate}
          onRefresh={refreshAdminData}
        />
      ) : (
        <CustomerPanel
          customers={customers}
          selectedCustomer={selectedCustomer}
          selectedCustomerId={selectedCustomerId}
          customerBalance={customerBalance}
          transactions={customerTransactions}
          topUpAmount={topUpAmount}
          pin={pin}
          statusMessage={statusMessage}
          isSubmitting={isSubmitting}
          onCustomerChange={setSelectedCustomerId}
          onTopUpAmountChange={setTopUpAmount}
          onPinChange={setPin}
          onTopUpSubmit={handleTopUpSubmit}
          onRefresh={() => refreshCustomerData()}
        />
      )}
    </main>
  );
}

createRoot(document.getElementById("root")).render(<App />);
