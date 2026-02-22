import { useEffect, useState } from "react";
import { BrowserRouter, Navigate, Route, Routes, useNavigate } from "react-router-dom";
import Nav from "./components/Nav";
import { RequireRole } from "./components/Guard";
import LandingPage from "./pages/LandingPage";
import PlannerPage from "./pages/PlannerPage";
import ProgressPage from "./pages/ProgressPage";
import AiSupportPage from "./pages/AiSupportPage";
import BoardPage from "./pages/BoardPage";
import LearnerCourses from "./pages/LearnerCourses";
import LearnerCourseDetail from "./pages/LearnerCourseDetail";
import SignupPage from "./pages/Signup";
import AdminCourses from "./pages/AdminCourses";
import AdminCourseDetail from "./pages/AdminCourseDetail";
import AdminReports from "./pages/AdminReports";
import AdminMyPage from "./pages/AdminMyPage";
import LoginModal from "./components/LoginModal";
import { apiRequest } from "./api/client";
import { clearAuth, getName, getUserId, Role, setAuth } from "./auth";

function AppLayout() {
  const navigate = useNavigate();
  const [loginOpen, setLoginOpen] = useState(false);
  useEffect(() => {
    const userId = getUserId();
    const name = getName();
    if (!userId || (name && name.trim())) {
      return;
    }
    type ProfileResponse = {
      userId: number;
      email: string;
      role: Role;
      name?: string | null;
    };
    apiRequest<ProfileResponse>("/api/users/me")
      .then((profile) => {
        setAuth(String(profile.userId), profile.role, profile.name);
      })
      .catch(() => {});
  }, [loginOpen]);

  const handleLoginOpen = () => {
    setLoginOpen(true);
  };

  const handleLoginClose = () => {
    setLoginOpen(false);
  };

  const handleLoginSuccess = () => {
    setLoginOpen(false);
    navigate("/", { replace: true });
  };

  const handleLogout = () => {
    clearAuth();
    navigate("/", { replace: true });
  };

  const handleExplore = () => {
    navigate("/learner/courses");
  };

  const handlePlanner = () => {
    navigate("/planner");
  };

  const handleProgress = () => {
    navigate("/progress");
  };

  const handleAiSupport = () => {
    navigate("/ai-support");
  };

  return (
    <div className="app">
      <Nav onLogin={handleLoginOpen} />
      <Routes>
        <Route
          path="/"
          element={
            <LandingPage
              onLogin={handleLoginOpen}
              onLogout={handleLogout}
              onExplore={handleExplore}
              onPlanner={handlePlanner}
              onProgress={handleProgress}
              onAiSupport={handleAiSupport}
            />
          }
        />
        <Route path="/planner" element={<PlannerPage />} />
        <Route path="/progress" element={<ProgressPage />} />
        <Route path="/ai-support" element={<AiSupportPage />} />
        <Route path="/board" element={<BoardPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/learner/courses" element={<LearnerCourses />} />
        <Route path="/learner/course/:id" element={<LearnerCourseDetail />} />
        <Route
          path="/admin/courses"
          element={
            <RequireRole role="ADMIN">
              <AdminCourses />
            </RequireRole>
          }
        />
        <Route
          path="/admin/course/:id"
          element={
            <RequireRole role="ADMIN">
              <AdminCourseDetail />
            </RequireRole>
          }
        />
        <Route
          path="/admin/reports"
          element={
            <RequireRole role="ADMIN">
              <AdminReports />
            </RequireRole>
          }
        />
        <Route
          path="/admin/my-page"
          element={
            <RequireRole role="ADMIN">
              <AdminMyPage />
            </RequireRole>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      <LoginModal open={loginOpen} onClose={handleLoginClose} onSuccess={handleLoginSuccess} />
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppLayout />
    </BrowserRouter>
  );
}
