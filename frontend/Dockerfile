# ── Build ──────────────────────────────────────────────
FROM node:22-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ARG VITE_KEYCLOAK_URL
ARG VITE_KEYCLOAK_REALM=ums
ARG VITE_KEYCLOAK_CLIENT_ID=ums-web
ARG VITE_API_BASE_URL
ARG VITE_DEMO_MODE=false
RUN npm run build

# ── Runtime ────────────────────────────────────────────
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
