# Banking System POC

A simplified banking proof of concept built with a Core Java REST API and a React frontend. It demonstrates card-range routing, withdrawals, top-ups, SHA-256 PIN verification, customer creation, in-memory account storage, and role-based transaction monitoring.

## Technology

- Backend: Core Java using the JDK HTTP server
- Frontend: React 19 and Vite
- Storage: In-memory Java collections
- Security: SHA-256 hashing for PIN verification and card lookup

## Prerequisites

- JDK 21 or later
- Node.js 20 or later
- npm

Check the installed versions:

```powershell
java --version
javac --version
node --version
npm --version
```

## Start the Backend in Eclipse or IntelliJ IDEA

You do not need to run `javac` or configure the classpath manually when using an IDE.

### Eclipse

1. Select **File > Import > Existing Projects into Workspace**.
2. Select the `backend` folder and finish the import.
3. Confirm that JDK 21 or later is selected under **Project > Properties > Java Build Path**.
4. If required, right-click `src` and select **Build Path > Use as Source Folder**.
5. Open `src/com/bankingpoc/BankingPocServer.java`.
6. Right-click the file and select **Run As > Java Application**.

### IntelliJ IDEA

1. Select **File > Open** and choose the `backend` folder.
2. Select JDK 21 or later under **File > Project Structure > Project SDK**.
3. If required, right-click `src` and select **Mark Directory as > Sources Root**.
4. Open `src/com/bankingpoc/BankingPocServer.java`.
5. Select the green Run icon beside the `main` method.

The backend runs at `http://localhost:8080`. Keep it running while using the UI.

## Start the Frontend

The frontend still uses npm. Run these commands from PowerShell or the terminal built into Eclipse or IntelliJ IDEA:

```powershell
cd "C:\Users\SRI VASAVI\OneDrive\Documents\GitHub\Technical_Assessment_PayTabs\frontend"
npm install
npm run dev
```

Open `http://127.0.0.1:5173` in a browser.

In IntelliJ IDEA, you can also open `frontend/package.json` and select the Run icon beside the `dev` script. Eclipse users can continue using the built-in terminal.

## Test Accounts

| Customer | Customer ID | Card number | PIN | Opening balance |
| --- | --- | --- | --- | --- |
| Asha Rao | `cust-1001` | `4111111111111111` | `1234` | `1500.00` |
| Rahul Mehta | `cust-1002` | `4222222222222222` | `5678` | `750.00` |

These values are for local demonstration only.

## Documentation

See [Setup and API Guide](docs/setup-and-api-guide.md) for API requests, Postman instructions, UI usage, validation rules, and troubleshooting.

## Important POC Note

All data is kept in memory. Customers, balances, and transactions return to their sample values whenever the backend is restarted. This is intentional for the assessment and is not suitable for production use.
