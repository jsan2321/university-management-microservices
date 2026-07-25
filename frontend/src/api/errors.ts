export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
    public code?: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}
const fallback: Record<number, string> = {
  400: "Some information needs your attention.",
  401: "Your session expired. Please sign in again.",
  403: "You do not have permission to do that.",
  404: "The requested record was not found.",
  409: "This change conflicts with an existing record.",
  503: "A university service is temporarily unavailable.",
};
export async function responseError(response: Response) {
  const body = (await response.json().catch(() => ({}))) as {
    message?: string;
    detail?: string;
    code?: string;
  };
  return new ApiError(
    response.status,
    body.message ??
      body.detail ??
      fallback[response.status] ??
      "The request could not be completed.",
    body.code,
  );
}
