import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getUserId } from "../auth";
import { apiRequest } from "../api/client";

type Course = {
  id: number;
  title: string;
  description: string | null;
  createdAt: string;
};

export default function LearnerCourses() {
  const [courses, setCourses] = useState<Course[]>([]);
  const [error, setError] = useState<string | null>(null);
  const loggedIn = Boolean(getUserId());

  useEffect(() => {
    apiRequest<Course[]>("/api/courses")
      .then((data) => {
        setCourses(data);
        setError(null);
      })
      .catch((err) => setError(err.message));
  }, []);

  const formatDate = (value: string) => {
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return "-";
    }
    return parsed.toLocaleDateString("ko-KR", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  return (
    <main className="page course-page">
      <div className="page-header">
        <h1>강의 둘러보기</h1>
        <p className="muted">
          실제 서비스 흐름을 그대로 담은 과정/강의를 확인하세요.
          {loggedIn ? " 지금 바로 수강 신청이 가능합니다." : " 로그인 후 수강이 열립니다."}
        </p>
      </div>
      {error && <p className="error">{error}</p>}
      <div className="courses-grid">
        {courses.map((course) => (
          <article key={course.id} className="course-card">
            <div className="course-banner" />
            <div className="course-body">
              <div className="course-meta">
                <span>{formatDate(course.createdAt)}</span>
                <span>코스 #{course.id}</span>
              </div>
              <Link className="course-title" to={`/learner/course/${course.id}`}>
                {course.title}
              </Link>
              <p className="course-description">
                {course.description ?? "설명이 준비 중인 과정입니다."}
              </p>
              <div className="course-actions">
                <Link className="btn btn-primary btn-sm" to={`/learner/course/${course.id}`}>
                  상세 보기
                </Link>
                {loggedIn ? (
                  <button className="btn btn-secondary btn-sm">바로 수강</button>
                ) : (
                  <span className="course-badge">로그인 필요</span>
                )}
              </div>
            </div>
          </article>
        ))}
      </div>
    </main>
  );
}
