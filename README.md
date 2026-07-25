# 🏠 Rental App Monorepo

A full‑stack rental platform with **Spring Boot 3** backend, **React + TypeScript + Vite** web‑admin, and **Expo** mobile app.

---

## 🧩 Local Development

### Prerequisites

- Java 17, Maven
- Node.js 18+, npm
- PostgreSQL 14+ (or Docker)
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
   ```bash
   docker-compose up -d
   ```
4. (Optional) Update the mobile app's IP in `mobile-app/src/config/index.ts`.
5. Run everything:
   ```bash
   npm run dev
   ```
6. Access:

   - Web-Admin: `http://localhost:5173`
   - Backend API: `http://localhost:8585/api/properties`
   - Mobile: scan QR code with Expo Go

---

## ✅ Final State (Local)

| Module    | Config method                          | Local URL                                    |
| --------- | -------------------------------------- | -------------------------------------------- |
| Backend   | `application.yml`                      | `http://localhost:8585`                      |
| Web-Admin | `.env` + Vite proxy                    | `http://localhost:5173` (proxies to backend) |
| Mobile    | `src/config/index.ts` (uses `__DEV__`) | `http://<your-ip>:8585/api`                  |

All are clean, configurable, and ready for production.

---

## 🚀 Production Deployment (Hetzner Cloud)

### Build Artifacts

- **Backend JAR**: `mvn clean package -DskipTests` → `target/backend-0.0.1-SNAPSHOT.jar`
- **Web‑Admin static files**: `npm run build` → `dist/` folder

### Upload to Server

```bash
scp backend-0.0.1-SNAPSHOT.jar user@ksdcnit.com:/opt/rental-app/
scp -r web-admin/dist/* user@ksdcnit.com:/var/www/rental-admin/
```

---

## 🔧 Common Operations & Commands

### Connect to Cloud Server (SSH)

Use your private key to connect:

```bash
ssh -i C:\Users\Krishnappa.Guvappa\.ssh\id_ed25519_new root@91.99.92.169
```

**Note:** Replace the path and IP as needed.

---

### Systemd Service (Backend)

- **Check service status**
  ```bash
  sudo systemctl status rental-app
  ```
- **View live logs (follow)**
  ```bash
  sudo journalctl -u rental-app -f
  ```
- **View last 50 lines of logs**
  ```bash
  sudo journalctl -u rental-app -n 50
  ```
- **Restart the service**
  ```bash
  sudo systemctl restart rental-app
  ```
- **Stop the service**
  ```bash
  sudo systemctl stop rental-app
  ```
- **Reload systemd after changing service file**
  ```bash
  sudo systemctl daemon-reload
  ```
- **Edit service file**
  ```bash
  sudo systemctl edit --full rental-app
  ```
  or
  ```bash
  sudo nano /etc/systemd/system/rental-app.service
  ```

---

### Nginx

- **Check Nginx configuration syntax**
  ```bash
  sudo nginx -t
  ```
- **Reload Nginx (apply changes)**
  ```bash
  sudo systemctl reload nginx
  ```
- **View Nginx error logs (last 30 lines)**
  ```bash
  sudo tail -30 /var/log/nginx/error.log
  ```
- **Follow Nginx error logs in real‑time**
  ```bash
  sudo tail -f /var/log/nginx/error.log
  ```
- **View Nginx access logs**
  ```bash
  sudo tail -30 /var/log/nginx/access.log
  ```
- **Edit Nginx site configuration**
  ```bash
  sudo nano /etc/nginx/sites-available/default
  ```

---

### PostgreSQL (Database)

- **Connect to the database**
  ```bash
  sudo -u postgres psql -d rental_db -p 5433
  ```
  (adjust `-d` and `-p` to match your setup)
- **Run a quick SQL command** (without entering psql)
  ```bash
  sudo -u postgres psql -d rental_db -p 5433 -c "SELECT COUNT(*) FROM properties;"
  ```
- **Apply missing schema migrations** (add columns)
  ```sql
  ALTER TABLE properties ADD COLUMN IF NOT EXISTS visibility VARCHAR(50) DEFAULT 'PUBLIC';
  ALTER TABLE properties ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
  UPDATE properties SET visibility = 'PUBLIC' WHERE visibility IS NULL;
  UPDATE properties SET is_active = TRUE WHERE is_active IS NULL;
  ```
- **List all tables**
  ```bash
  sudo -u postgres psql -d rental_db -p 5433 -c "\dt"
  ```
- **Describe a table (show columns)**
  ```bash
  sudo -u postgres psql -d rental_db -p 5433 -c "\d properties"
  ```
- **Check all active connections**
  ```bash
  sudo -u postgres psql -d rental_db -p 5433 -c "SELECT * FROM pg_stat_activity;"
  ```

---

### Application Properties (Systemd Environment)

Override properties via environment variables in the systemd service file:
```ini
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/rental_db"
Environment="SPRING_DATASOURCE_USERNAME=postgres"
Environment="SPRING_DATASOURCE_PASSWORD=postgres"
Environment="APP_IMAGE_BASE_URL=https://ksdcnit.com"
Environment="APP_STORAGE_LOCAL_UPLOAD_DIR=/var/www/rental-images/properties/"
Environment="APP_STORAGE_LOCAL_BASE_URL=https://ksdcnit.com/images/"
```

---

### Quick Health Checks & Debugging

- **Test backend directly (bypass Nginx)**
  ```bash
  curl http://localhost:8585/api/properties
  ```
- **Test backend via Nginx (public URL)**
  ```bash
  curl https://ksdcnit.com/api/properties
  ```
- **Check if backend port is listening**
  ```bash
  sudo netstat -tulpn | grep 8585
  ```
  or
  ```bash
  sudo ss -tulpn | grep 8585
  ```
- **Ping Nginx**
  ```bash
  curl -I https://ksdcnit.com
  ```
- **Check disk space (for uploads)**
  ```bash
  df -h
  ```
- **Check memory usage**
  ```bash
  free -h
  ```
- **View system logs (general)**
  ```bash
  sudo journalctl -xe
  ```

---

## 📦 File Locations (Production)

| Component           | Path on Server                       |
| ------------------- | ------------------------------------ |
| Backend JAR         | `/opt/rental-app/backend-*.jar`      |
| Systemd service     | `/etc/systemd/system/rental-app.service` |
| Uploaded images     | `/var/www/rental-images/properties/` |
| Nginx site config   | `/etc/nginx/sites-available/default` |
| Web‑Admin static    | `/var/www/rental-admin/` (or your web root) |

---

## 🧹 Troubleshooting

| Issue | Solution |
|-------|----------|
| **502 Bad Gateway** | Backend not running or Nginx proxy wrong port. Check `sudo systemctl status rental-app` and Nginx logs. |
| **Column does not exist** | Run the SQL migrations above to add `visibility` and `is_active` columns. |
| **Placeholder not resolved** | Missing environment variable – check systemd service file. |
| **CORS issues** | Verify `@CrossOrigin` on backend controllers or Nginx headers. |
| **Multipart error (backend)** | Ensure `FormData` is used in frontend. Check `client.ts` interceptor. |
| **Image delete/replace fails** | Verify `@DeleteMapping("/images")` exists and `findByImageUrl` query uses `JOIN`. |
| **SSH connection refused** | Check if server IP is correct and SSH service is running (`sudo systemctl status ssh`). |
| **Port 8585 not listening** | Backend not started; check logs with `journalctl -u rental-app -f`. |

---

## 🔄 Git Workflow

### Stage and commit changes

```bash
git add .
git commit -m "feat: description of changes"
git push origin main
```

### Pull latest changes on server

```bash
git pull origin main
```

---

## 📚 More Help

- Spring Boot docs: [https://docs.spring.io/spring-boot/](https://docs.spring.io/spring-boot/)
- React/Vite: [https://vitejs.dev/](https://vitejs.dev/)
- Expo: [https://docs.expo.dev/](https://docs.expo.dev/)
- Hetzner Cloud: [https://docs.hetzner.com/cloud/](https://docs.hetzner.com/cloud/)

---

## 📝 License

This project is proprietary. All rights reserved.


manual process to Deployment# Build the JAR locally
cd backend
mvn clean package -DskipTests

# Upload to staging
scp -i C:\Users\Krishnappa.Guvappa\.ssh\id_ed25519_new target/backend-0.0.1-SNAPSHOT.jar root@91.99.92.169:/opt/rental-app-staging/

# Restart the staging service
ssh -i C:\Users\Krishnappa.Guvappa\.ssh\id_ed25519_new root@91.99.92.169 "systemctl restart rental-app-staging"

✅ Fix – Rebuild and Redeploy the Frontend to Staging
On your local machine, rebuild the frontend:

bash
cd web-admin
npm run build
Upload the new build to the staging folder:

bash
scp -i C:\Users\Krishnappa.Guvappa\.ssh\id_ed25519_new -r dist/* root@91.99.92.169:/var/www/rental-admin-staging/
Verify the upload (check the timestamp of index.html):

bash
ssh -i C:\Users\Krishnappa.Guvappa\.ssh\id_ed25519_new root@91.99.92.169 "ls -la /var/www/rental-admin-staging/index.html"
