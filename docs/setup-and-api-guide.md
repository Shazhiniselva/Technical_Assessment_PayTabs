# Setup and API Guide

## 1. Project Overview

The application contains two connected parts:

- `backend`: a Core Java API that routes and processes banking transactions.
- `frontend`: a React and Vite interface for Super Admin and Customer roles.

The backend is intentionally lightweight and does not use Maven, Spring Boot, or H2. Data is stored in thread-safe in-memory Java collections. Restarting the backend clears new customers and transaction history and reloads the two sample accounts.

## 2. Prerequisites

Install the following software before starting the application:

- JDK 21 or later
- Node.js 20 or later
- npm, included with Node.js
- A browser such as Chrome, Edge, or Firefox
- Optional: Postman for API testing

Verify the tools in PowerShell:

```powershell
java --version
javac --version
node --version
npm --version
```

## 3. Backend Setup

The backend is compiled directly with `javac`. Maven is not required.

```powershell
cd "C:\Users\SRI VASAVI\OneDrive\Documents\GitHub\Technical_Assessment_PayTabs\backend"
$files = Get-ChildItem -Recurse -Filter *.java src | ForEach-Object FullName
javac -d out $files
java -cp out com.bankingpoc.BankingPocServer
```

After startup, the terminal displays:

```text
Banking POC API running at http://localhost:8080
```

Check the service in a browser or Postman:

```text
GET http://localhost:8080/api/health
```

Expected response:

```json
{"status":"ok"}
```

Keep the backend terminal running while using the frontend.

## 4. Frontend Setup

Open a second PowerShell terminal:

```powershell
cd "C:\Users\SRI VASAVI\OneDrive\Documents\GitHub\Technical_Assessment_PayTabs\frontend"
npm install
npm run dev
```

Open the UI at:

```text
http://127.0.0.1:5173
```

The frontend sends API requests to `http://localhost:8080/api`, so the backend must be running first.

To create a production frontend build:

```powershell
npm run build
```

The generated files are written to `frontend/dist`.

## 5. UI Access and Roles

No login screen is used in this POC. Use the role selector in the page header.

### Super Admin

The Super Admin view provides:

- A system-wide transaction list
- Approved and declined transaction counts
- Customer count
- Customer creation using name, Visa-style card number, and a four-digit PIN

When creating a customer, the backend generates the customer ID and sets the opening balance to `0.00`. A card number cannot be registered more than once.

### Customer

The Customer view provides:

- Customer selection
- Current balance and masked card number
- Personal transaction history
- Top-up submission using an amount and PIN

## 6. Sample Data

| Customer | Customer ID | Card number | PIN | Opening balance |
| --- | --- | --- | --- | --- |
| Asha Rao | `cust-1001` | `4111111111111111` | `1234` | `1500.00` |
| Rahul Mehta | `cust-1002` | `4222222222222222` | `5678` | `750.00` |

The sample cards start with `4`, which is the supported range in System 1.

## 7. API Requests

All POST requests use the `Content-Type: application/json` header.

### Health Check

```powershell
curl.exe http://localhost:8080/api/health
```

### Successful Withdrawal Through System 1

System 1 validates the request and routes supported cards to System 2.

```powershell
curl.exe -X POST "http://localhost:8080/api/system1/transactions" `
  -H "Content-Type: application/json" `
  -d '{"cardNumber":"4111111111111111","pin":"1234","amount":"100.00","type":"withdraw"}'
```

Example response:

```json
{
  "approved": true,
  "transactionId": "generated-transaction-id",
  "reason": "Approved",
  "customerId": "cust-1001",
  "maskedCard": "**** **** **** 1111",
  "balance": "1400.00"
}
```

### Successful Top-Up Through System 1

```powershell
curl.exe -X POST "http://localhost:8080/api/system1/transactions" `
  -H "Content-Type: application/json" `
  -d '{"cardNumber":"4111111111111111","pin":"1234","amount":"250.00","type":"topup"}'
```

### Customer Top-Up Endpoint

This route is used by the Customer UI. The backend obtains the customer's card and routes the transaction through System 1.

```powershell
curl.exe -X POST "http://localhost:8080/api/customers/cust-1001/topups" `
  -H "Content-Type: application/json" `
  -d '{"amount":"100.00","pin":"1234"}'
```

### Direct System 2 Processing

This endpoint demonstrates the card processor independently of System 1 routing.

```powershell
curl.exe -X POST "http://localhost:8080/api/system2/process" `
  -H "Content-Type: application/json" `
  -d '{"cardNumber":"4111111111111111","pin":"1234","amount":"50.00","type":"withdraw"}'
```

### Create a Customer

The card must contain 13 to 19 digits, start with `4`, and not already be registered. The PIN must contain exactly four digits.

```powershell
curl.exe -X POST "http://localhost:8080/api/customers" `
  -H "Content-Type: application/json" `
  -d '{"name":"Meera Shah","cardNumber":"4333333333333333","pin":"4321"}'
```

Example response:

```json
{
  "id": "cust-generated-id",
  "name": "Meera Shah",
  "maskedCard": "**** **** **** 3333",
  "balance": "0.00"
}
```

Submitting the same card again returns HTTP `400`:

```json
{"approved":false,"reason":"Card number is already registered"}
```

### View All Customers

```powershell
curl.exe http://localhost:8080/api/customers
```

### View a Customer Balance

```powershell
curl.exe http://localhost:8080/api/customers/cust-1001/balance
```

### View Customer Transactions

```powershell
curl.exe http://localhost:8080/api/customers/cust-1001/transactions
```

### View All Transactions as Super Admin

```powershell
curl.exe http://localhost:8080/api/admin/transactions
```

## 8. Postman Instructions

For a POST request in Postman:

1. Select `POST` and enter the endpoint URL.
2. Open the **Headers** tab and add `Content-Type` with value `application/json`.
3. Open **Body**, select **raw**, and choose **JSON**.
4. Paste one of the JSON request bodies from the examples above.
5. Select **Send**.

For GET endpoints, enter the URL, select `GET`, and select **Send**. No request body is required.

## 9. Validation Examples

### Invalid PIN

Use a valid card with an incorrect PIN. The response reason is `Invalid PIN`.

### Invalid Card

Use a card beginning with `4` that is not registered. The response reason is `Invalid card`.

### Unsupported Card Range

Use a card that does not begin with `4`. System 1 returns `Card range not supported` without forwarding it to System 2.

### Insufficient Balance

Submit a withdrawal larger than the current balance. The response reason is `Insufficient balance`.

### Invalid Amount or Type

Amounts must be greater than zero. The transaction type must be `withdraw` or `topup`.

## 10. Run the Automated Smoke Tests

```powershell
cd "C:\Users\SRI VASAVI\OneDrive\Documents\GitHub\Technical_Assessment_PayTabs\backend"
$files = Get-ChildItem -Recurse -Filter *.java src | ForEach-Object FullName
javac -d out $files
java -cp out com.bankingpoc.BankingPocSmokeTest
```

The tests cover approved withdrawals and top-ups, invalid cards, invalid PINs, insufficient funds, unsupported card ranges, customer creation, zero opening balance, and duplicate card rejection.

## 11. Security and Storage Notes

- PINs are hashed with SHA-256 before storage and compared as hashes during authentication.
- Card numbers are indexed using SHA-256 hashes and displayed in masked form.
- The POC also retains the demo card number in memory so the customer top-up endpoint can route a transaction. It is not returned by customer APIs or written to logs. A production system should replace this with tokenization or encryption.
- Plain-text PINs are not logged.
- Data is stored only in memory for this POC.
- Restarting the backend resets all balances, customers, and transactions.
- SHA-256 alone is acceptable for this assessment, but a production PIN system should use stronger controls such as a dedicated password hashing algorithm, per-record salts, encryption, rate limiting, and secure key management.

## 12. Troubleshooting

### Port 8080 Is Already in Use

Find the process using the backend port:

```powershell
Get-NetTCPConnection -LocalPort 8080 | Select-Object OwningProcess
```

Inspect the process before stopping it:

```powershell
Get-Process -Id <PROCESS_ID>
```

If it is an older instance of this application, stop it and start the backend again:

```powershell
Stop-Process -Id <PROCESS_ID>
```

### PowerShell Does Not Expand Recursive Java Wildcards

Do not use `src\com\bankingpoc\**\*.java`. PowerShell does not pass that pattern to `javac` as expected. Use:

```powershell
$files = Get-ChildItem -Recurse -Filter *.java src | ForEach-Object FullName
javac -d out $files
```

### Frontend Cannot Reach the Backend

Confirm that:

- The backend terminal is still running.
- `http://localhost:8080/api/health` returns `{"status":"ok"}`.
- The frontend is opened at `http://127.0.0.1:5173`.

### Java Package Mismatch in the Editor

Open `backend` as the Java project root or configure `backend/src` as the source folder. Java source files under `src/com/bankingpoc` declare the `com.bankingpoc` package.
