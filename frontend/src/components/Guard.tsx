import { Navigate } from "react-router-dom";
import { getRole, getUserId, Role } from "../auth";

export function RequireAuth({ children }: { children: JSX.Element }) {
  const userId = getUserId();
  if (!userId) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

export function RequireRole({
  role,
  children,
}: {
  role: Role;
  children: JSX.Element;
}) {
  const userId = getUserId();
  const currentRole = getRole();
  if (!userId) {
    return <Navigate to="/" replace />;
  }
  if (currentRole !== role) {
    return <Navigate to="/" replace />;
  }
  return children;
}
