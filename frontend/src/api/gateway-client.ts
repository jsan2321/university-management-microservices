import { responseError } from "./errors";

export type TokenProvider = () => Promise<string>;
export function queryString(
  params: Record<string, string | number | undefined>,
) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== "") query.set(key, String(value));
  });
  return query.size ? `?${query}` : "";
}
export function createGatewayClient(getToken: TokenProvider) {
  const baseUrl = (
    import.meta.env.VITE_API_GATEWAY_URL ?? "http://localhost:8080"
  ).replace(/\/$/, "");
  async function request<T>(
    path: string,
    init: RequestInit = {},
    retry = true,
  ): Promise<T> {
    const token = await getToken();
    const response = await fetch(`${baseUrl}${path}`, {
      ...init,
      headers: {
        Accept: "application/json",
        Authorization: `Bearer ${token}`,
        ...(init.body ? { "Content-Type": "application/json" } : {}),
        ...init.headers,
      },
    });
    if (response.status === 401 && retry) return request<T>(path, init, false);
    if (!response.ok) throw await responseError(response);
    if (response.status === 204) return undefined as T;
    return response.json() as Promise<T>;
  }
  return {
    get: <T>(path: string) => request<T>(path),
    send: <T>(
      path: string,
      method: string,
      body?: unknown,
      headers?: HeadersInit,
    ) =>
      request<T>(path, {
        method,
        body: body === undefined ? undefined : JSON.stringify(body),
        headers,
      }),
  };
}
