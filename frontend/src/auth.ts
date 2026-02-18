export type Role = "ADMIN" | "LEARNER";

const USER_ID_KEY = "lms.userId";
const ROLE_KEY = "lms.role";
const NAME_KEY = "lms.name";

export function getUserId(): string | null {
  return localStorage.getItem(USER_ID_KEY);
}

export function getRole(): Role | null {
  const role = localStorage.getItem(ROLE_KEY);
  if (role === "ADMIN" || role === "LEARNER") {
    return role;
  }
  return null;
}

export function getName(): string | null {
  return localStorage.getItem(NAME_KEY);
}

export function setAuth(userId: string, role: Role, name?: string | null) {
  localStorage.setItem(USER_ID_KEY, userId);
  localStorage.setItem(ROLE_KEY, role);
  if (name && name.trim()) {
    localStorage.setItem(NAME_KEY, name.trim());
  } else {
    localStorage.removeItem(NAME_KEY);
  }
}

export function clearAuth() {
  localStorage.removeItem(USER_ID_KEY);
  localStorage.removeItem(ROLE_KEY);
  localStorage.removeItem(NAME_KEY);
}
