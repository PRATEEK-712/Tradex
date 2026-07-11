# Tradex

Tradex is a full-stack trading company workflow manager built with React, Vite, Java Spring Boot, Spring Security, and Google OpenID Connect.

It includes modules for workflows, inventory, staff, orders, billing, and sales operations. The backend exposes REST APIs and the frontend provides an operations dashboard.

## Project Structure

```text
Tradex/
  backend/    Spring Boot REST API, database, security, seeded sample data
  frontend/   React + Vite dashboard and Google OIDC login client
```

## Prerequisites

Install these before running the project:

- Java 21 or newer
- Maven 3.9 or newer
- Node.js 20 or newer
- npm
- A Google Cloud OAuth client for real Google sign-in

## Start The Backend

Open PowerShell:

```powershell
cd "C:\Users\prat7\OneDrive\Documents\Tradex\backend"

$env:OIDC_ISSUER_URI="https://accounts.google.com"
$env:OIDC_AUDIENCE="your-google-oauth-client-id.apps.googleusercontent.com"
$env:GOOGLE_ALLOWED_DOMAIN="yourcompany.com"
$env:GOOGLE_ADMIN_EMAILS="owner@yourcompany.com,ops.head@yourcompany.com"
$env:CORS_ALLOWED_ORIGINS="http://localhost:5173,http://127.0.0.1:5173"

mvn spring-boot:run
```

The backend starts here:

```text
http://localhost:8080
```

Health check:

```text
http://localhost:8080/actuator/health
```

H2 database console:

```text
http://localhost:8080/h2-console
```

Use this JDBC URL in the H2 console:

```text
jdbc:h2:mem:tradeops
```

## Start The Frontend

Open another PowerShell window:

```powershell
cd "C:\Users\prat7\OneDrive\Documents\Tradex\frontend"
npm install
npm run dev
```

The frontend starts here:

```text
http://localhost:5173
```

If npm scripts have trouble on Windows, this direct Vite command also works:

```powershell
node .\node_modules\vite\bin\vite.js --host 127.0.0.1 --port 5173
```

## Frontend Environment

Create `frontend/.env` from `frontend/.env.example`:

```text
VITE_API_BASE_URL=http://localhost:8080/api
VITE_OIDC_AUTHORITY=https://accounts.google.com
VITE_OIDC_CLIENT_ID=your-google-oauth-client-id.apps.googleusercontent.com
VITE_OIDC_REDIRECT_URI=http://localhost:5173
VITE_OIDC_SCOPE=openid profile email
```

If these OIDC values are missing, the frontend falls back to demo mode so the dashboard can still be previewed.

## Backend Environment

Create `backend/.env` from `backend/.env.example`, or set these variables in PowerShell before running Maven:

```text
OIDC_ISSUER_URI=https://accounts.google.com
OIDC_AUDIENCE=your-google-oauth-client-id.apps.googleusercontent.com
GOOGLE_ALLOWED_DOMAIN=yourcompany.com
GOOGLE_ADMIN_EMAILS=owner@yourcompany.com,ops.head@yourcompany.com
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
```

`GOOGLE_ALLOWED_DOMAIN` is optional. Leave it blank if any Google account should be allowed.

`GOOGLE_ADMIN_EMAILS` controls which Google users can create, update, and delete records.

## Google OAuth Client Setup

In Google Cloud Console:

1. Create or select a Google Cloud project.
2. Go to APIs & Services > OAuth consent screen.
3. Configure the consent screen for your app.
4. Go to APIs & Services > Credentials.
5. Create an OAuth client ID.
6. Choose Web application.
7. Add this Authorized JavaScript origin:

```text
http://localhost:5173
```

8. Add this Authorized redirect URI:

```text
http://localhost:5173
```

9. Copy the generated client ID into both places:

```text
VITE_OIDC_CLIENT_ID
OIDC_AUDIENCE
```

## How OAuth 2.0 And OIDC Work Here

OAuth 2.0 answers this question:

```text
Can this application get a token for this user?
```

OpenID Connect, or OIDC, is an identity layer built on OAuth 2.0. OIDC answers this question:

```text
Who is the signed-in user?
```

Tradex uses Google as the OIDC provider.

## Login Flow

1. The React frontend starts an OIDC Authorization Code flow with PKCE using `oidc-client-ts`.
2. The browser redirects the user to Google.
3. Google authenticates the user.
4. Google redirects back to `http://localhost:5173` with an authorization code.
5. The frontend exchanges that code for tokens.
6. The frontend sends a Google token in the `Authorization` header when calling the backend API.

The API request looks like this:

```http
Authorization: Bearer <google-token>
```

## Backend Token Validation

The Spring Boot backend is configured as an OAuth2 Resource Server.

It validates incoming JWTs by checking:

- The token was issued by Google: `https://accounts.google.com`
- The token audience matches `OIDC_AUDIENCE`
- The token signature is valid using Google's public keys
- The token is not expired
- The Google email is verified
- The hosted domain matches `GOOGLE_ALLOWED_DOMAIN`, if configured

## Authorization Model

Google authenticates the user. Tradex authorizes what the user can do.

The backend maps Google identity claims into application roles:

```text
Verified Google user -> ROLE_ops_user
Email in GOOGLE_ADMIN_EMAILS -> ROLE_ops_admin
```

API permissions:

```text
GET /api/**        ROLE_ops_user or ROLE_ops_admin
POST /api/**       ROLE_ops_admin
PUT /api/**        ROLE_ops_admin
DELETE /api/**     ROLE_ops_admin
```

That means regular verified users can view operational data, while configured admin users can modify it.

## Main API Endpoints

```text
GET    /api/workflows
GET    /api/inventory
GET    /api/staff
GET    /api/orders
GET    /api/invoices

POST   /api/workflows
PUT    /api/workflows/{id}
DELETE /api/workflows/{id}
```

The same create, update, and delete pattern exists for inventory, staff, orders, and invoices.

## Development Notes

The backend uses an in-memory H2 database, so data resets when the backend restarts. Sample trading company records are inserted automatically at startup.

For production, replace H2 with PostgreSQL, MySQL, or another persistent database, and keep all OAuth/OIDC secrets and environment values outside source control.
