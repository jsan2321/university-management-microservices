import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { AppProviders } from "./app/AppProviders";
import { AppRouter } from "./app/AppRouter";
import { bootstrapSession } from "./auth/keycloak";
import "./design/global.css";

const initialSession = await bootstrapSession().catch(() => null);
createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <AppProviders initialSession={initialSession}>
      <AppRouter />
    </AppProviders>
  </StrictMode>,
);
