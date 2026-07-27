import { afterEach, describe, expect, it, vi } from "vitest";
import { createGatewayClient } from "../api/gateway-client";
import { createServiceApi } from "../api/service-api";
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
  it("sends an empty teacher action body for assignment state changes", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockImplementation(() =>
        Promise.resolve(new Response(JSON.stringify({ id: "a1" }), {
          status: 200,
          headers: { "Content-Type": "application/json" },
        })),
      ),
    );
    const api = createServiceApi(async () => "access-token");
    await api.publishAssignment("a1");
    await api.closeAssignment("a1");
    await api.releaseGrade("submission-1");
    expect(fetch).toHaveBeenNthCalledWith(
      1,
      "http://localhost:8080/assignment-service/api/v1/assignments/a1/publish",
      expect.objectContaining({ method: "PATCH", body: "{}" }),
    );
    expect(fetch).toHaveBeenNthCalledWith(
      2,
      "http://localhost:8080/assignment-service/api/v1/assignments/a1/close",
      expect.objectContaining({ method: "PATCH", body: "{}" }),
    );
    expect(fetch).toHaveBeenNthCalledWith(
      3,
      "http://localhost:8080/assignment-service/api/v1/assignments/submissions/submission-1/release-grade",
      expect.objectContaining({ method: "PATCH", body: "{}" }),
    );
  });
});
