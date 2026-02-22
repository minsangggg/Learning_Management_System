import { useEffect, useState } from "react";
import { apiRequest } from "../api/client";

type PendingEnrollment = {
  id: number;
  userId: number;
  userEmail: string;
  userName: string | null;
  courseId: number;
  courseTitle: string;
  status: string;
  enrolledAt: string;
};

export default function AdminMyPage() {
  const [rows, setRows] = useState<PendingEnrollment[]>([]);
  const [error, setError] = useState<string | null>(null);

  const loadPending = async () => {
    try {
      const data = await apiRequest<PendingEnrollment[]>("/api/enroll/pending");
      setRows(data);
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    }
  };

  useEffect(() => {
    void loadPending();
  }, []);

  const approve = async (enrollmentId: number) => {
    try {
      await apiRequest(`/api/enroll/${enrollmentId}/approve`, { method: "POST" });
      setRows((prev) => prev.filter((row) => row.id !== enrollmentId));
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    }
  };

  return (
    <main className="page">
      <div className="page-header">
        <h1>Enrollment Approvals</h1>
        <p className="muted">Approve pending enrollments.</p>
      </div>
      {error && <p className="error">{error}</p>}
      <section className="card section">
        {rows.length === 0 ? (
          <p className="muted">No pending enrollments.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>User</th>
                <th>Course</th>
                <th>Requested</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.id}>
                  <td>
                    {row.userName || `User ${row.userId}`} ({row.userEmail})
                  </td>
                  <td>
                    {row.courseId} - {row.courseTitle}
                  </td>
                  <td>{row.enrolledAt}</td>
                  <td>
                    <button
                      className="btn btn-primary btn-sm"
                      type="button"
                      onClick={() => approve(row.id)}
                    >
                      Approve
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </main>
  );
}
