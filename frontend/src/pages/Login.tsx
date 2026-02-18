import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Role, setAuth } from "../auth";

export default function Login() {
  const navigate = useNavigate();
  const [userId, setUserId] = useState("1");
  const [role, setRole] = useState<Role>("ADMIN");

  const onSubmit = (event: FormEvent) => {
    event.preventDefault();
    if (!userId.trim()) {
      return;
    }
    setAuth(userId.trim(), role);
    navigate(role === "ADMIN" ? "/admin/courses" : "/learner/courses");
  };

  return (
    <main className="page">
      <div className="page-header">
        <h1>Login (Temporary)</h1>
        <p className="muted">임시 로그인으로 역할별 화면을 확인합니다.</p>
      </div>
      <div className="card">
        <form onSubmit={onSubmit} className="form-grid">
          <label>
            User ID
            <input value={userId} onChange={(e) => setUserId(e.target.value)} />
          </label>
          <label>
            Role
            <select value={role} onChange={(e) => setRole(e.target.value as Role)}>
              <option value="ADMIN">ADMIN</option>
              <option value="LEARNER">LEARNER</option>
            </select>
          </label>
          <button className="btn btn-primary" type="submit">
            Save
          </button>
        </form>
      </div>
    </main>
  );
}
