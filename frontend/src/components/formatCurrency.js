export function formatCurrency(value) {
  if (value === null || value === undefined || value === "") {
    return "-";
  }
  return `Rs. ${Number(value).toFixed(2)}`;
}
