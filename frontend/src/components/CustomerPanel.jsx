import React from "react";
import { ArrowDownToLine, CreditCard, RefreshCcw } from "lucide-react";
import { TransactionTable } from "./TransactionTable";
import { formatCurrency } from "./formatCurrency";

export function CustomerPanel({
  customers,
  selectedCustomer,
  selectedCustomerId,
  customerBalance,
  transactions,
  topUpAmount,
  pin,
  statusMessage,
  isSubmitting,
  onCustomerChange,
  onTopUpAmountChange,
  onPinChange,
  onTopUpSubmit,
  onRefresh
}) {
  return (
    <section className="customerGrid">
      <aside className="panel sidePanel">
        <label>
          Customer
          <select value={selectedCustomerId} onChange={(event) => onCustomerChange(event.target.value)}>
            {customers.map((customer) => (
              <option key={customer.id} value={customer.id}>
                {customer.name}
              </option>
            ))}
          </select>
        </label>

        <div className="balanceBlock">
          <CreditCard size={22} />
          <span>{selectedCustomer?.name || "Customer"}</span>
          <strong>{formatCurrency(customerBalance?.balance)}</strong>
          <small>{customerBalance?.maskedCard}</small>
        </div>

        <form onSubmit={onTopUpSubmit} className="topupForm">
          <label>
            Amount
            <input value={topUpAmount} onChange={(event) => onTopUpAmountChange(event.target.value)} inputMode="decimal" />
          </label>
          <label>
            PIN
            <input
              value={pin}
              onChange={(event) => onPinChange(event.target.value)}
              type="password"
              inputMode="numeric"
              autoComplete="off"
            />
          </label>
          <button type="submit" disabled={isSubmitting}>
            <ArrowDownToLine size={18} /> {isSubmitting ? "Processing..." : "Top Up"}
          </button>
          {statusMessage && <p className="message">{statusMessage}</p>}
        </form>
      </aside>

      <section className="panel">
        <div className="panelHeader">
          <div>
            <h2>My Transactions</h2>
            <p>{transactions.length} customer events captured</p>
          </div>
          <button type="button" className="iconButton" onClick={onRefresh} title="Refresh customer transactions">
            <RefreshCcw size={18} />
          </button>
        </div>
        <TransactionTable transactions={transactions} />
      </section>
    </section>
  );
}
