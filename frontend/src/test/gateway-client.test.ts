import { afterEach, describe, expect, it, vi } from "vitest";
import { createGatewayClient } from "../api/gateway-client";
afterEach(() => vi.restoreAllMocks());
describe("gateway client", () => {
  it("sends the in-memory bearer token to gateway routes", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(JSON.stringify({ id: "s1" }), {
          status: 200,
          headers: { "Content-Type": "application/json" },
        }),
      ),
    );
    const client = createGatewayClient(async () => "access-token");
    await client.get("/student-service/api/v1/students/me");
    expect(fetch).toHaveBeenCalledWith(
      "http://localhost:8080/student-service/api/v1/students/me",
      expect.objectContaining({
        headers: expect.objectContaining({
          Authorization: "Bearer access-token",
        }),
      }),
    );
  });
  it("normalizes forbidden responses", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response("{}", {
          status: 403,
          headers: { "Content-Type": "application/json" },
        }),
      ),
    );
    const client = createGatewayClient(async () => "access-token");
    await expect(
      client.get("/academic-service/api/v1/academic/teachers/me"),
    ).rejects.toEqual(
      expect.objectContaining({
        status: 403,
        message: "You do not have permission to do that.",
      }),
    );
  });
});
