# Rental App Monorepo

## Local Development

### Prerequisites

- Java 17, Maven
- Node.js 18+, npm
- PostgreSQL (or Docker)
- Expo Go on your phone

### Setup

1. Clone the repo.
2. Install dependencies:
   ```bash
   npm install                # root
   cd web-admin && npm install
   cd ../mobile-app && npm install
   ```
3. Start PostgreSQL:
   bash
   docker-compose up -d

4. (Optional) Update the mobile app's IP in mobile-app/src/config/index.ts.

5. Run everything:

bash
npm run dev 6. Access:

Web-Admin: http://localhost:5173

Backend API: http://localhost:8585/api/properties

Mobile: scan QR code with Expo Go

---

## ✅ Final State

| Module    | Config method                          | Local URL                                    |
| --------- | -------------------------------------- | -------------------------------------------- |
| Backend   | `application.yml`                      | `http://localhost:8585`                      |
| Web-Admin | `.env` + Vite proxy                    | `http://localhost:5173` (proxies to backend) |
| Mobile    | `src/config/index.ts` (uses `__DEV__`) | `http://<your-ip>:8585/api`                  |

All are clean, configurable, and ready for production.

---

## 🚀 Next: Hetzner Deployment

Once you're comfortable with this setup, you can run `npm run build` to prepare the web-admin and backend JAR, then copy them to Hetzner. I'll guide you through that when you're ready.

Let me know if you want to implement any of these changes (e.g., the mobile `config.ts` and root build scripts) – I can provide the exact file contents.
