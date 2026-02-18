import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Role, setAuth } from "../auth";

export default function SignupPage() {
  const [name, setName] = useState("");
  const [company, setCompany] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<Role>("LEARNER");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!email.trim() || !password.trim()) {
      setMessage("이메일과 비밀번호를 입력해주세요.");
      return;
    }
    setMessage("");
    setLoading(true);
    try {
      const response = await fetch("/api/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password, role, name, company }),
      });
      const contentType = response.headers.get("content-type") || "";
      if (!response.ok) {
        if (contentType.includes("application/json")) {
          const body = await response.json();
          throw new Error(`${body.code || "ERROR"}: ${body.message}`);
        }
        const text = await response.text();
        throw new Error(text || `HTTP ${response.status}`);
      }
      const data = contentType.includes("application/json")
        ? await response.json()
        : await response.text();
      if (!data || typeof data !== "object") {
        throw new Error("Unexpected signup response");
      }
      setAuth(String(data.userId), data.role, data.name ?? name);
      navigate("/");
    } catch (e) {
      setMessage((e as Error).message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="page signup-page">
      <div className="signup-form-card">
        <div className="signup-header">
          <h1>회원가입</h1>
          <p>학습자 계정을 생성하고 LMS 흐름을 그대로 체험해보세요.</p>
        </div>
        <form className="form-grid" onSubmit={handleSubmit}>
          <label>
            이름
            <input value={name} onChange={(e) => setName(e.target.value)} />
          </label>
          <label>
            회사/기관
            <input value={company} onChange={(e) => setCompany(e.target.value)} />
          </label>
          <label>
            이메일
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
          </label>
          <label>
            비밀번호
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
          </label>
          <label>
            Role
            <select value={role} onChange={(e) => setRole(e.target.value as Role)}>
              <option value="LEARNER">LEARNER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </label>
          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading ? "신청 중…" : "신청하기"}
          </button>
        </form>
        {message && <p className="muted">{message}</p>}
      </div>
    </main>
  );
}
