import { FormEvent, useState } from "react";
import { apiRequest } from "../api/client";

type PeriodRow = {
  courseId: number;
  courseTitle: string;
  learnerCount: number;
  avgProgress: number;
};

type CompletionRow = {
  courseId: number;
  courseTitle: string;
  completedLearners: number;
};

export default function AdminReports() {
  const [from, setFrom] = useState("2026-01-01");
  const [to, setTo] = useState("2026-01-31");
  const [courseId, setCourseId] = useState("");
  const [periodRows, setPeriodRows] = useState<PeriodRow[]>([]);
  const [completionRows, setCompletionRows] = useState<CompletionRow[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [aiInput, setAiInput] = useState("");
  const [summaryOutput, setSummaryOutput] = useState("");
  const [quizOutput, setQuizOutput] = useState("");

  const query = () => {
    const params = new URLSearchParams({ from, to });
    if (courseId) {
      params.set("courseId", courseId);
    }
    return params.toString();
  };

  const loadPeriod = async (event: FormEvent) => {
    event.preventDefault();
    try {
      const data = await apiRequest<PeriodRow[]>(
        `/api/reports/course-period?${query()}`
      );
      setPeriodRows(data);
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    }
  };

  const loadCompletion = async () => {
    try {
      const data = await apiRequest<CompletionRow[]>(
        `/api/reports/course-completion?${query()}`
      );
      setCompletionRows(data);
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    }
  };

  const downloadCsv = async (filename: string, path: string) => {
    try {
      const csv = await apiRequest<string>(path);
      const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = filename;
      link.click();
      URL.revokeObjectURL(url);
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    }
  };

  const downloadPeriodCsv = () => {
    downloadCsv("course-period.csv", `/api/reports/course-period.csv?${query()}`);
  };

  const downloadCompletionCsv = () => {
    downloadCsv(
      "course-completion.csv",
      `/api/reports/course-completion.csv?${query()}`
    );
  };

  const generateSummary = async () => {
    try {
      const data = await apiRequest<{ output: string }>("/api/ai/summary", {
        method: "POST",
        body: JSON.stringify({ text: aiInput }),
      });
      setSummaryOutput(data.output);
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    }
  };

  const generateQuiz = async () => {
    try {
      const data = await apiRequest<{ output: string }>("/api/ai/quiz", {
        method: "POST",
        body: JSON.stringify({ text: aiInput }),
      });
      setQuizOutput(data.output);
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    }
  };

  return (
    <main className="page">
      <div className="page-header">
        <h1>Admin Reports</h1>
        <p className="muted">기간/과정별 리포트와 CSV 다운로드</p>
      </div>
      {error && <p className="error">{error}</p>}
      <div className="card">
        <form onSubmit={loadPeriod} className="form-grid">
          <label>
            From (yyyy-MM-dd)
            <input value={from} onChange={(e) => setFrom(e.target.value)} />
          </label>
          <label>
            To (yyyy-MM-dd)
            <input value={to} onChange={(e) => setTo(e.target.value)} />
          </label>
          <label>
            Course ID (optional)
            <input value={courseId} onChange={(e) => setCourseId(e.target.value)} />
          </label>
          <div className="form-actions">
            <button className="btn btn-primary" type="submit">
              Load Period Report
            </button>
            <button className="btn" type="button" onClick={loadCompletion}>
              Load Completion Report
            </button>
          </div>
          <div className="form-actions">
            <button className="btn" type="button" onClick={downloadPeriodCsv}>
              Download Period CSV
            </button>
            <button className="btn" type="button" onClick={downloadCompletionCsv}>
              Download Completion CSV
            </button>
          </div>
        </form>
      </div>
      <section className="section">
        <h2>Course Period</h2>
        <table className="table">
          <thead>
            <tr>
              <th>Course</th>
              <th>Learners</th>
              <th>Avg Progress</th>
            </tr>
          </thead>
          <tbody>
            {periodRows.map((row) => (
              <tr key={row.courseId}>
                <td>
                  {row.courseId} - {row.courseTitle}
                </td>
                <td>{row.learnerCount}</td>
                <td>{row.avgProgress}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
      <section className="section">
        <h2>Course Completion</h2>
        <table className="table">
          <thead>
            <tr>
              <th>Course</th>
              <th>Completed Learners</th>
            </tr>
          </thead>
          <tbody>
            {completionRows.map((row) => (
              <tr key={row.courseId}>
                <td>
                  {row.courseId} - {row.courseTitle}
                </td>
                <td>{row.completedLearners}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
      <section className="card section">
        <h2>AI Tools</h2>
        <div className="form-grid">
          <label>
            Input Text
            <textarea
              value={aiInput}
              onChange={(e) => setAiInput(e.target.value)}
              rows={4}
            />
          </label>
          <div className="form-actions">
            <button className="btn btn-primary" type="button" onClick={generateSummary}>
              Generate Summary
            </button>
            <button className="btn" type="button" onClick={generateQuiz}>
              Generate Quiz
            </button>
          </div>
        </div>
        {summaryOutput && (
          <div className="section">
            <h3>Summary</h3>
            <pre>{summaryOutput}</pre>
          </div>
        )}
        {quizOutput && (
          <div className="section">
            <h3>Quiz</h3>
            <pre>{quizOutput}</pre>
          </div>
        )}
      </section>
    </main>
  );
}
