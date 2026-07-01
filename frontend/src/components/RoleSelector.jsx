import React from "react";
import { ShieldCheck, UserRound } from "lucide-react";

export function RoleSelector({ activeRole, onRoleChange }) {
  return (
    <div className="roleSwitch" aria-label="Role selector">
      <button type="button" className={activeRole === "admin" ? "active" : ""} onClick={() => onRoleChange("admin")}>
        <ShieldCheck size={18} /> Super Admin
      </button>
      <button type="button" className={activeRole === "customer" ? "active" : ""} onClick={() => onRoleChange("customer")}>
        <UserRound size={18} /> Customer
      </button>
    </div>
  );
}
