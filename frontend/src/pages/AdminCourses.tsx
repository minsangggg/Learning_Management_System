import { FormEvent, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiRequest } from "../api/client";

type Course = {
  id: number;
  title: string;
  description: string | null;
  createdAt: string;
};

export default function AdminCourses() {
  const [courses, setCourses] = useState<Course[]>([]);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState<string | null>(null);

  const loadCourses = () => {
    apiRequest<Course[]>("/api/courses")
      .then((data) => {
        setCourses(data);
        setError(null);
      })
      .catch((err) => setError(err.message));
  };

  useEffect(() => {
    loadCourses();
  }, []);

  const createCourse = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await apiRequest<Course>("/api/courses", {
        method: "POST",
        body: JSON.stringify({ title, description }),
      });
      setTitle("");
      setDescription("");
      loadCourses();
    } catch (err) {
      setError((err as Error).message);
    }
  };

  return (
    <main className="page">
      <div className="page-header">
        <h1>Admin Courses</h1>
        <p className="muted">과정 생성 및 목록 관리</p>
      </div>
      {error && <p className="error">{error}</p>}
      <div className="card">
        <form onSubmit={createCourse} className="form-grid">
          <label>
            Title
            <input value={title} onChange={(e) => setTitle(e.target.value)} />
          </label>
          <label>
            Description
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
            />
          </label>
          <button className="btn btn-primary" type="submit">
            Create
          </button>
        </form>
      </div>
      <ul className="card-grid">
        {courses.map((course) => (
          <li key={course.id}>
            <Link to={`/admin/course/${course.id}`}>{course.title}</Link>
          </li>
        ))}
      </ul>
    </main>
  );
}
