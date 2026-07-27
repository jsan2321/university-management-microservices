import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => ({
  plugins: [react()],
  server: { port: 5173, strictPort: true },
  preview: { port: 4173 },
  test: { environment: "jsdom" },
  // Explicitly define DEV so Vite's dead-code eliminator can strip the demo
  // code path from production bundles at compile time, regardless of env vars.
  define:
    mode !== "development"
      ? { "import.meta.env.DEV": JSON.stringify(false) }
      : {},
}));
