import React from "react";
import { formatCurrency } from "./formatCurrency";

export function TransactionTable({ transactions, showCustomer = false }) {
  if (!transactions.length) {
    return <div className="emptyState">No transactions yet.</div>;
  }

  return (
    <div className="tableWrap">
      <table>
        <thead>
          <tr>
            <th>Time</th>
            {showCustomer && <th>Customer</th>}
            <th>Card</th>
            <th>Type</th>
            <th>Amount</th>
            <th>Status</th>
            <th>Balance</th>
          </tr>
        </thead>
        <tbody>
          {transactions.map((transaction) => (
            <tr key={transaction.id}>
              <td className="dateCell">{new Date(transaction.timestamp).toLocaleString()}</td>
              {showCustomer && <td>{transaction.customerId || "-"}</td>}
              <td>{transaction.maskedCard}</td>
              <td><span className="transactionType">{transaction.type}</span></td>
              <td className="amountCell">{formatCurrency(transaction.amount)}</td>
              <td>
                <span className={transaction.approved ? "status approved" : "status declined"}>
                  {transaction.reason}
                </span>
              </td>
              <td className="amountCell">{transaction.resultingBalance != null ? formatCurrency(transaction.resultingBalance) : "-"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
