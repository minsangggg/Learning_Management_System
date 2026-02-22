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
    : "";
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
              <a href="#intro">?뚭컻</a>
              <a href="#packages">?⑦궎吏</a>
              <a href="#samples">?섑뵆</a>
              <a href="#contact">臾몄쓽</a>
              <Link to="/board">寃뚯떆??</Link>
            </>
          ) : (
            <>
              {role === "LEARNER" && (
                <>
                  <Link to="/learner/courses">?숈뒿 怨쇱젙</Link>
                </>
              )}
              {role === "ADMIN" && (
                <>
                  <Link to="/admin/courses">怨쇱젙 愿由?</Link>
                  <Link to="/admin/reports">由ы룷??</Link>
                </>
              )}
            </>
          )}
        </nav>
                <div className="header-actions">
          {userId ? <span className="user-pill">{displayLabel}</span> : null}
          {userId ? (
            <button
              className="btn"
              onClick={() => {
                clearAuth();
                navigate("/");
              }}
            >
              濡쒓렇?꾩썐
            </button>
          ) : (
            <button className="btn" onClick={onLogin}>
              濡쒓렇??
            </button>
          )}
          {userId && role === "ADMIN" ? (
            <button
              className="btn btn-secondary"
              type="button"
              onClick={() => {
                window.open("/admin/my-page", "_blank", "noopener,noreferrer");
              }}
            >
              마이페이지
            </button>
          ) : null}
        </div>
      </div>
    </header>
  );
}





