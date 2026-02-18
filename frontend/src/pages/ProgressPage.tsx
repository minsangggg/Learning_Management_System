import { useEffect, useState } from "react";
import { apiRequest } from "../api/client";
import { getRole } from "../auth";

type ProgressRow = {
  userId: number;
  userName?: string | null;
  courseId: number;
  courseTitle: string;
  avgProgress: number;
  completedLessons: number;
  totalLessons: number;
};

export default function ProgressPage() {
  const role = getRole();
  const [rows, setRows] = useState<ProgressRow[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!role) {
      return;
    }
    apiRequest<ProgressRow[]>("/api/reports/learner-progress")
      .then((data) => {
        setRows(data);
        setError(null);
      })
      .catch((err) => {
        setError((err as Error).message);
      });
  }, [role]);

  return (
    <main className="page">
      <div className="page-header">
        <h1>Progress · 진도 리포트</h1>
        <p className="muted">
          {role === "ADMIN"
            ? "전체 학습자의 진도 현황을 확인합니다."
            : "나의 학습 진도 현황을 확인합니다."}
        </p>
      </div>

      {error && <p className="error">{error}</p>}

      <section className="section">
        <div className="card">
          <h2>진도 현황</h2>
          {rows.length === 0 && !error ? (
            <p className="muted">표시할 진도 데이터가 없습니다.</p>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  {role === "ADMIN" && <th>학습자</th>}
                  <th>과정</th>
                  <th>평균 진도율</th>
                  <th>완료 레슨</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={`${row.userId}-${row.courseId}`}>
                    {role === "ADMIN" && (
                      <td>{row.userName || `User ${row.userId}`}</td>
                    )}
                    <td>{row.courseTitle}</td>
                    <td>{row.avgProgress.toFixed(1)}%</td>
                    <td>
                      {row.completedLessons}/{row.totalLessons}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </section>
    </main>
  );
}
