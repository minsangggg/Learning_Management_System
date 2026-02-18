import { getRole, getUserId } from "../auth";

export type ApiError = { code: string; message: string };

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const userId = getUserId();
  const role = getRole();
  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };
  if (userId) {
    headers["X-User-Id"] = userId;
  }
  if (role) {
    headers["X-Role"] = role;
  }

  const response = await fetch(path, { ...options, headers });
  const contentType = response.headers.get("content-type") || "";

  if (!response.ok) {
    if (contentType.includes("application/json")) {
      const error = (await response.json()) as ApiError;
      throw new Error(`${error.code}: ${error.message}`);
    }
    throw new Error(`HTTP ${response.status}`);
  }

  if (contentType.includes("application/json")) {
    return (await response.json()) as T;
  }

  return (await response.text()) as T;
}
