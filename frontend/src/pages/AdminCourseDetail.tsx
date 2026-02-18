import { FormEvent, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { apiRequest } from "../api/client";

type Course = {
  id: number;
  title: string;
  description: string | null;
  createdAt: string;
};

type Lesson = {
  id: number;
  courseId: number;
  title: string;
  content: string | null;
  orderNo: number;
};

export default function AdminCourseDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const courseId = Number(id);
  const [course, setCourse] = useState<Course | null>(null);
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [lessonTitle, setLessonTitle] = useState("");
  const [lessonContent, setLessonContent] = useState("");
  const [lessonOrder, setLessonOrder] = useState("1");
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    try {
      const courseData = await apiRequest<Course>(`/api/courses/${courseId}`);
      setCourse(courseData);
      setTitle(courseData.title);
      setDescription(courseData.description || "");
      const lessonData = await apiRequest<Lesson[]>(
        `/api/lessons?courseId=${courseId}`
      );
      setLessons(lessonData);
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    }
  };

  useEffect(() => {
    if (!courseId) {
      return;
    }
    load();
  }, [courseId]);

  const updateCourse = async (event: FormEvent) => {
    event.preventDefault();
    try {
      const updated = await apiRequest<Course>(`/api/courses/${courseId}`, {
        method: "PUT",
        body: JSON.stringify({ title, description }),
      });
      setCourse(updated);
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    }
  };

  const deleteCourse = async () => {
    try {
      await apiRequest<void>(`/api/courses/${courseId}`, { method: "DELETE" });
      navigate("/admin/courses");
    } catch (err) {
      setError((err as Error).message);
    }
  };

  const addLesson = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await apiRequest<Lesson>("/api/lessons", {
        method: "POST",
        body: JSON.stringify({
          courseId,
          title: lessonTitle,
          content: lessonContent,
          orderNo: Number(lessonOrder),
        }),
      });
      setLessonTitle("");
      setLessonContent("");
      setLessonOrder("1");
      await load();
    } catch (err) {
      setError((err as Error).message);
    }
  };

  const updateLesson = async (lesson: Lesson) => {
    try {
      await apiRequest<Lesson>(`/api/lessons/${lesson.id}`, {
        method: "PUT",
        body: JSON.stringify({
          title: lesson.title,
          content: lesson.content,
          orderNo: lesson.orderNo,
        }),
      });
      await load();
    } catch (err) {
      setError((err as Error).message);
    }
  };

  const deleteLesson = async (lessonId: number) => {
    try {
      await apiRequest<void>(`/api/lessons/${lessonId}`, { method: "DELETE" });
      await load();
    } catch (err) {
      setError((err as Error).message);
    }
  };

  const updateLessonField = (
    lessonId: number,
    field: keyof Lesson,
    value: string
  ) => {
    setLessons((prev) =>
      prev.map((lesson) =>
        lesson.id === lessonId
          ? {
              ...lesson,
              [field]:
                field === "orderNo" ? Number(value) : (value as string),
            }
          : lesson
      )
    );
  };

  return (
    <main className="page">
      <div className="page-header">
        <h1>Admin Course Detail</h1>
        <p className="muted">과정 정보 및 강의 목록 관리</p>
      </div>
      {error && <p className="error">{error}</p>}
      {course && (
        <section className="card">
          <h2>Course</h2>
          <form onSubmit={updateCourse} className="form-grid">
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
            <div className="form-actions">
              <button className="btn btn-primary" type="submit">
                Update
              </button>
              <button className="btn btn-danger" type="button" onClick={deleteCourse}>
                Delete
              </button>
            </div>
          </form>
        </section>
      )}
      <section className="card section">
        <h2>Lessons</h2>
        <form onSubmit={addLesson} className="form-grid">
          <label>
            Title
            <input value={lessonTitle} onChange={(e) => setLessonTitle(e.target.value)} />
          </label>
          <label>
            Content
            <textarea
              value={lessonContent}
              onChange={(e) => setLessonContent(e.target.value)}
              rows={3}
            />
          </label>
          <label>
            Order No
            <input value={lessonOrder} onChange={(e) => setLessonOrder(e.target.value)} />
          </label>
          <button className="btn btn-primary" type="submit">
            Add Lesson
          </button>
        </form>
        <div className="section">
          {lessons.map((lesson) => (
            <div key={lesson.id} className="card">
              <div className="form-grid">
                <label>
                  Title
                  <input
                    value={lesson.title}
                    onChange={(e) => updateLessonField(lesson.id, "title", e.target.value)}
                  />
                </label>
                <label>
                  Content
                  <textarea
                    value={lesson.content || ""}
                    onChange={(e) => updateLessonField(lesson.id, "content", e.target.value)}
                    rows={2}
                  />
                </label>
                <label>
                  Order No
                  <input
                    value={lesson.orderNo}
                    onChange={(e) => updateLessonField(lesson.id, "orderNo", e.target.value)}
                  />
                </label>
                <div className="form-actions">
                  <button className="btn btn-primary" type="button" onClick={() => updateLesson(lesson)}>
                    Update
                  </button>
                  <button className="btn btn-danger" type="button" onClick={() => deleteLesson(lesson.id)}>
                    Delete
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>
    </main>
  );
}
