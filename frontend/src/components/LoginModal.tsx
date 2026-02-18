import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiRequest } from "../api/client";
import { Role, setAuth } from "../auth";

type LoginModalProps = {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
};

export default function LoginModal({ open, onClose, onSuccess }: LoginModalProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  if (!open) return null;

  type ProfileResponse = {
    userId: number;
    email: string;
    role: Role;
    name?: string | null;
  };

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!email.trim() || !password.trim()) {
      setError("이메일과 비밀번호를 모두 입력해주세요.");
      return;
    }
    setError(null);
    setLoading(true);
    try {
      const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      if (!response.ok) {
        const body = await response.json();
        throw new Error(`${body.code || "ERROR"}: ${body.message}`);
      }
      const data = await response.json();
      setAuth(String(data.userId), data.role, data.name);
      if (!data.name) {
        try {
          const profile = await apiRequest<ProfileResponse>("/api/users/me");
          setAuth(String(profile.userId), profile.role, profile.name);
        } catch {
          // Ignore profile lookup errors; login already succeeded.
        }
      }
      onSuccess();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setLoading(false);
    }
  };

  const loginWithKakao = () => {
    setAuth("2", "LEARNER", "\ud64d\uae38\ub3d9");
    onSuccess();
  };

  const goSignup = () => {
    onClose();
    navigate("/signup");
  };

  const goRecovery = () => {
    onClose();
    navigate("/signup?tab=recover");
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>로그인</h2>
          <button className="btn" type="button" onClick={onClose}>
            닫기
          </button>
        </div>
        <div className="modal-body">
          <button className="btn kakao-btn" type="button" onClick={loginWithKakao}>
            카카오로 로그인
          </button>
          <div className="divider">
            <span>또는 이메일 로그인</span>
          </div>
          <form onSubmit={submit} className="form-grid">
            <label>
              이메일
              <input
                type="email"
                placeholder="email@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </label>
            <label>
              비밀번호
              <input
                type="password"
                placeholder="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </label>
            {error && <p className="error">{error}</p>}
            <button className="btn btn-primary" type="submit" disabled={loading}>
              {loading ? "로딩 중…" : "로그인"}
            </button>
          </form>
          <div className="modal-links">
            <button className="link-btn" type="button" onClick={goSignup}>
              회원가입
            </button>
            <button className="link-btn" type="button" onClick={goRecovery}>
              아이디, 비밀번호 찾기
            </button>
          </div>
        </div>
        
      </div>
    </div>
  );
}
