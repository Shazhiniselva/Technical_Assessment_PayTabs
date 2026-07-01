import React, { useState } from "react";
import { Plus, RefreshCcw, UserPlus } from "lucide-react";
import { TransactionTable } from "./TransactionTable";

export function AdminPanel({ transactions, isCreatingCustomer, customerCreationMessage, customerCreationStatus, onCreateCustomer, onRefresh }) {
  const [showCustomerForm, setShowCustomerForm] = useState(false);
  const [name, setName] = useState("");
  const [cardNumber, setCardNumber] = useState("");
  const [pin, setPin] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    const created = await onCreateCustomer({ name, cardNumber, pin });
    if (created) {
      setName("");
      setCardNumber("");
      setPin("");
      setShowCustomerForm(false);
    }
  }

  return (
    <section className="panel">
      <div className="panelHeader">
        <div>
          <h2>All Transactions</h2>
          <p>{transactions.length} system events captured</p>
        </div>
        <div className="panelActions">
          <button type="button" className="secondaryButton" onClick={() => setShowCustomerForm((visible) => !visible)}>
            <UserPlus size={16} /> New customer
          </button>
          <button type="button" className="iconButton" onClick={onRefresh} title="Refresh transactions">
            <RefreshCcw size={18} />
          </button>
        </div>
      </div>
      {showCustomerForm && (
        <form className="customerCreateForm" onSubmit={handleSubmit}>
          <div className="formHeading">
            <span className="formIcon"><Plus size={16} /></span>
            <div>
              <h3>Create customer</h3>
              <p>The account starts with a zero balance.</p>
            </div>
          </div>
          <label>
            Full name
            <input value={name} onChange={(event) => setName(event.target.value)} required minLength="2" maxLength="80" placeholder="Enter customer name" />
          </label>
          <label>
            Card number
            <input value={cardNumber} onChange={(event) => setCardNumber(event.target.value)} required inputMode="numeric" autoComplete="off" placeholder="Starts with 4" />
          </label>
          <label>
            4-digit PIN
            <input value={pin} onChange={(event) => setPin(event.target.value)} required type="password" inputMode="numeric" pattern="[0-9]{4}" maxLength="4" autoComplete="new-password" placeholder="••••" />
          </label>
          <button className="createButton" type="submit" disabled={isCreatingCustomer}>
            {isCreatingCustomer ? "Creating..." : "Create account"}
          </button>
        </form>
      )}
      {customerCreationMessage && (
        <p className={`creationMessage ${customerCreationStatus}`} role={customerCreationStatus === "error" ? "alert" : "status"}>
          {customerCreationMessage}
        </p>
      )}
      <TransactionTable transactions={transactions} showCustomer />
    </section>
  );
}
