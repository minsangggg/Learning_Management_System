import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { apiRequest } from "../api/client";
import { getUserId } from "../auth";

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
  videoUrl: string | null;
};

type Enrollment = {
  id: number;
  userId: number;
  courseId: number;
  status: string;
  enrolledAt: string;
};

type Progress = {
  id: number;
  enrollmentId: number;
  lessonId: number;
  progressPercent: number;
  completedAt: string | null;
};

export default function LearnerCourseDetail() {
  const { id } = useParams();
  const courseId = Number(id);
  const [course, setCourse] = useState<Course | null>(null);
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [selectedLessonId, setSelectedLessonId] = useState<number | null>(null);
  const [enrollment, setEnrollment] = useState<Enrollment | null>(null);
  const [progressRows, setProgressRows] = useState<Progress[]>([]);
  const [error, setError] = useState<string | null>(null);
  const loggedIn = Boolean(getUserId());

  useEffect(() => {
    if (!courseId) {
      return;
    }
    apiRequest<Course>(`/api/courses/${courseId}`)
      .then((data) => {
        setCourse(data);
        setError(null);
      })
      .catch((err) => setError(err.message));
    apiRequest<Lesson[]>(`/api/lessons/public?courseId=${courseId}`)
      .then((data) => setLessons(data))
      .catch(() => {});
  }, [courseId]);

  const toEmbedUrl = (url: string) => {
    try {
      const parsed = new URL(url);
      if (parsed.hostname.includes("youtu.be")) {
        const videoId = parsed.pathname.replace("/", "");
        return `https://www.youtube.com/embed/${videoId}`;
      }
      if (parsed.hostname.includes("youtube.com")) {
        const videoId = parsed.searchParams.get("v");
        if (videoId) {
          return `https://www.youtube.com/embed/${videoId}`;
        }
      }
    } catch {
      return "";
    }
    return "";
  };

  const handlePlay = (lesson: Lesson) => {
    setSelectedLessonId(lesson.id);
    void recordProgress(1, lesson.id);
  };

  const selectedLesson =
    selectedLessonId == null ? null : lessons.find((lesson) => lesson.id === selectedLessonId) || null;

  const enroll = async () => {
    if (!loggedIn) {
      setError("로그인 후 수강 신청이 가능합니다.");
      return;
    }
    try {
      const data = await apiRequest<Enrollment>("/api/enroll", {
        method: "POST",
        body: JSON.stringify({ courseId }),
      });
      setEnrollment(data);
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    }
  };

  const loadProgress = async (enrollmentId: number) => {
    const rows = await apiRequest<Progress[]>(
      `/api/progress?enrollmentId=${enrollmentId}`
    );
    setProgressRows(rows);
  };

  const recordProgress = async (progressValue: number, lessonId?: number | null) => {
    if (!enrollment) {
      setError("수강 신청 후 재생하면 진도가 기록됩니다.");
      return;
    }
    if (!loggedIn) {
      setError("로그인 후 재생하면 진도가 기록됩니다.");
      return;
    }
    const lessonIdToUse = lessonId ?? selectedLessonId ?? lessons[0]?.id ?? null;
    if (!lessonIdToUse) {
      setError("레슨을 선택해주세요.");
      return;
    }
    const existingProgress = progressRows.find((row) => row.lessonId === lessonIdToUse);
    if (existingProgress && existingProgress.progressPercent >= progressValue) {
      return;
    }
    try {
      const data = await apiRequest<Progress>("/api/progress", {
        method: "POST",
        body: JSON.stringify({
          enrollmentId: enrollment.id,
          lessonId: lessonIdToUse,
          progressPercent: progressValue,
        }),
      });
      setError(null);
      await loadProgress(enrollment.id);
      setProgressRows((prev) => {
        const existing = prev.find((row) => row.id === data.id);
        if (existing) {
          return prev.map((row) => (row.id === data.id ? data : row));
        }
        return [...prev, data];
      });
    } catch (err) {
      setError((err as Error).message);
    }
  };

  return (
    <main className="page">
      <div className="page-header">
        <h1>강의 상세</h1>
        <p className="muted">수강 신청과 진도 업데이트</p>
      </div>
      {error && <p className="error">{error}</p>}
      {course && (
        <section className="card">
          <h2>{course.title}</h2>
          <p>{course.description}</p>
        </section>
      )}
      <section className="card section">
        <h3>강의 재생</h3>
        <div className="player">
          {selectedLesson && selectedLesson.videoUrl ? (
            <iframe
              className="player-frame"
              src={`${toEmbedUrl(selectedLesson.videoUrl)}?autoplay=0`}
              title={selectedLesson.title}
              allow="accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowFullScreen
            />
          ) : (
            <div className="player-screen">
              <span>재생할 레슨을 선택하세요.</span>
            </div>
          )}
          <div className="player-meta">
            <p className="muted">
              {selectedLesson ? selectedLesson.title : "레슨을 선택하면 영상이 표시됩니다."}
            </p>
            <button
              className="btn"
              type="button"
              onClick={() => {
                if (!lessons.length) {
                  return;
                }
                const currentIndex = lessons.findIndex(
                  (lesson) => lesson.id === selectedLessonId
                );
                const next =
                  currentIndex >= 0 && currentIndex < lessons.length - 1
                    ? lessons[currentIndex + 1]
                    : lessons[0];
                handlePlay(next);
              }}
            >
              다음 레슨
            </button>
            <button
              className="btn btn-primary"
              type="button"
              onClick={() => recordProgress(100, selectedLessonId)}
            >
              완료 처리
            </button>
          </div>
        </div>
      </section>
      <section className="card section">
        <h3>레슨 목록</h3>
        <ul className="card-grid">
          {lessons.map((lesson) => (
            <li key={lesson.id}>
              <strong>
                {lesson.orderNo}. {lesson.title}
              </strong>
              <p className="muted">{lesson.content ?? "설명은 준비 중입니다."}</p>
              <button
                className="btn btn-secondary btn-sm"
                type="button"
                onClick={() => handlePlay(lesson)}
              >
                재생
              </button>
            </li>
          ))}
        </ul>
      </section>
      <section className="card section">
        <div className="form-actions">
          <button className="btn btn-primary" onClick={enroll}>
            수강 신청
          </button>
          {enrollment && (
            <button className="btn" onClick={() => loadProgress(enrollment.id)}>
              진도 불러오기
            </button>
          )}
        </div>
        {enrollment && (
          <p className="muted">
            Enrollment ID: {enrollment.id} ({enrollment.status})
          </p>
        )}
      </section>
      <section className="card section">
        <h3>진도 기록</h3>
        <p className="muted">
          레슨을 재생하면 자동으로 1% 기록되고, 완료 처리를 누르면 100%로 기록됩니다.
        </p>
      </section>
      {progressRows.length > 0 && (
        <section className="card section">
          <h3>진도 내역</h3>
          <ul className="card-grid">
            {progressRows.map((row) => (
              <li key={row.id}>
                <p className="muted">
                  {row.progressPercent}%{" "}
                  {row.completedAt ? `(completed ${row.completedAt})` : ""}
                </p>
              </li>
            ))}
          </ul>
        </section>
      )}
    </main>
  );
}

