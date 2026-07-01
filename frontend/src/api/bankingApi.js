const API_BASE_URL = "http://localhost:8080/api";

async function request(path, options) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options
  });
  return response.json();
}

export function getCustomers() {
  return request("/customers");
}

export function createCustomer(name, cardNumber, pin) {
  return request("/customers", {
    method: "POST",
    body: JSON.stringify({ name, cardNumber, pin })
  });
}

export function getAdminTransactions() {
  return request("/admin/transactions");
}

export function getCustomerBalance(customerId) {
  return request(`/customers/${customerId}/balance`);
}

export function getCustomerTransactions(customerId) {
  return request(`/customers/${customerId}/transactions`);
}

export function submitCustomerTopUp(customerId, amount, pin) {
  return request(`/customers/${customerId}/topups`, {
    method: "POST",
    body: JSON.stringify({ amount, pin })
  });
}
