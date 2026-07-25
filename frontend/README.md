# University Management Portal

React and TypeScript browser application for the university management services.

## Stack

- React with strict TypeScript
- Vite
- React Router
- Keycloak JavaScript adapter using Authorization Code with PKCE
- TanStack Query
- React Hook Form and Zod
- CSS design tokens and Lucide icons
- Vitest and Testing Library

## Local development

Copy `.env.example` to `.env.local`. The checked-in defaults match the backend's local ports and the public Keycloak client `ums-web`.

```powershell
npm install
npm run dev
```

Open `http://localhost:5173`. The API gateway must be available at `http://localhost:8080` and Keycloak at `http://localhost:8180`.

For a UI-only preview, set `VITE_DEMO_MODE=true`. Preview mode is visibly labeled and never sends mutations to the backend. Keep it disabled for integration and production.

## Authentication model

The browser redirects to Keycloak for sign-in and keeps tokens in the Keycloak adapter's memory. It never stores a client secret or calls the Keycloak Admin API. All business requests go through the API Gateway with a bearer token.

Teacher and student identity is resolved through backend `/me` endpoints. Browser request bodies do not supply a user ID or profile ID to identify the signed-in person.

## Available workflows

- Administrator: live service overview, student/teacher provisioning, people status changes, academic catalog creation and status changes, enrollment creation and cancellation.
- Teacher: self profile, assigned sections, attendance sessions and roster recording, assignment creation/publishing/closing, submission grading and grade release.
- Student: self profile, active enrollments, section attendance percentage, published assignments, submission, and released grades.

## Verification

```powershell
npm run lint
npm test
npm run build
```
