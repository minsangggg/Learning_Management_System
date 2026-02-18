import { Link, useLocation, useNavigate } from "react-router-dom";
import { clearAuth, getName, getRole, getUserId } from "../auth";

type NavProps = {
  onLogin: () => void;
};

export default function Nav({ onLogin }: NavProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const role = getRole();
  const userId = getUserId();
  const name = getName();
  const displayName = name && name.trim() ? name.trim() : userId ? `User ${userId}` : "";
  const displayLabel = userId
    ? [displayName, role || ""].filter(Boolean).join(" \u00b7 ")
    : "\uB85C\uADF8\uC778 \uD544\uC694";
  const isLanding = location.pathname === "/";

  return (
    <header className={`site-header ${isLanding ? "landing-header" : ""}`}>
      <div className="header-inner">
        <Link className="brand" to="/">
          Mini LMS
        </Link>
        <nav className="header-nav">
          {isLanding ? (
            <>
              <a href="#intro">소개</a>
              <a href="#packages">패키지</a>
              <a href="#samples">샘플</a>
              <a href="#contact">문의</a>
              <Link to="/board">게시판</Link>
            </>
          ) : (
            <>
              {role === "LEARNER" && (
                <>
                  <Link to="/learner/courses">학습 과정</Link>
                </>
              )}
              {role === "ADMIN" && (
                <>
                  <Link to="/admin/courses">과정 관리</Link>
                  <Link to="/admin/reports">리포트</Link>
                </>
              )}
            </>
          )}
        </nav>
        <div className="header-actions">
          <span className="user-pill">{displayLabel}</span>
          {userId ? (
            <button
              className="btn"
              onClick={() => {
                clearAuth();
                navigate("/");
              }}
            >
              로그아웃
            </button>
          ) : (
            <button className="btn" onClick={onLogin}>
              로그인
            </button>
          )}
        </div>
      </div>
    </header>
  );
}
