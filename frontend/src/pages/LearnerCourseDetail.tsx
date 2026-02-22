import { useEffect, useRef, useState } from "react";
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
  startSec?: number | null;
  endSec?: number | null;
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
  lastPositionSec?: number | null;
};

declare global {
  interface Window {
    YT?: any;
    onYouTubeIframeAPIReady?: () => void;
  }
}

export default function LearnerCourseDetail() {
  const { id } = useParams();
  const courseId = Number(id);
  const [course, setCourse] = useState<Course | null>(null);
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [selectedLessonId, setSelectedLessonId] = useState<number | null>(null);
  const [enrollment, setEnrollment] = useState<Enrollment | null>(null);
  const [progressRows, setProgressRows] = useState<Progress[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [showEnrollCard, setShowEnrollCard] = useState(false);
  const loggedIn = Boolean(getUserId());
  const playerRef = useRef<any>(null);
  const playerReadyRef = useRef(false);
  const saveTimerRef = useRef<number | null>(null);
  const activeLessonRef = useRef<Lesson | null>(null);
  const lastSavedPositionRef = useRef<number | null>(null);
  const resumeAppliedRef = useRef(false);
  const pendingResumeRef = useRef<number | null>(null);

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

  const getVideoId = (url: string) => {
    try {
      const parsed = new URL(url);
      if (parsed.hostname.includes("youtu.be")) {
        const videoId = parsed.pathname.replace("/", "");
        return videoId;
      }
      if (parsed.hostname.includes("youtube.com")) {
        const videoId = parsed.searchParams.get("v");
        if (videoId) {
          return videoId;
        }
      }
    } catch {
      return "";
    }
    return "";
  };

  const clampPosition = (lesson: Lesson, position: number) => {
    let result = position;
    if (typeof lesson.startSec === "number") {
      result = Math.max(result, lesson.startSec);
    }
    if (typeof lesson.endSec === "number") {
      result = Math.min(result, lesson.endSec);
    }
    return result;
  };

  const getResumeSeconds = (lesson: Lesson, rows: Progress[] = progressRows) => {
    const row = rows.find((progress) => progress.lessonId === lesson.id);
    const fallback = typeof lesson.startSec === "number" ? lesson.startSec : 0;
    if (!row || row.lastPositionSec == null) {
      return fallback;
    }
    const resume = clampPosition(lesson, row.lastPositionSec);
    if (typeof lesson.endSec === "number" && resume >= lesson.endSec) {
      return Math.max(fallback, lesson.endSec - 1);
    }
    return resume;
  };

  const handlePlay = (lesson: Lesson) => {
    if (!loggedIn) {
      setError("로그인 후 재생할 수 있습니다.");
      return;
    }
    if (!enrollment) {
      setError("수강 신청 후 재생할 수 있습니다.");
      return;
    }
    if (!isApprovedEnrollment(enrollment)) {
      setError("관리자 승인 대기 중입니다.");
      return;
    }
    setSelectedLessonId(lesson.id);
    void recordProgress(1, lesson.id);
  };

  const selectedLesson =
    selectedLessonId == null ? null : lessons.find((lesson) => lesson.id === selectedLessonId) || null;
  const completedLessonIds = new Set(
    progressRows
      .filter((row) => row.progressPercent >= 100 || row.completedAt)
      .map((row) => row.lessonId)
  );
  const isApprovedEnrollment = (current: Enrollment | null) => {
    if (!current) {
      return false;
    }
    return current.status?.toUpperCase() !== "PENDING";
  };
  const canPlay = isApprovedEnrollment(enrollment);
  const playerMessage = () => {
    if (!selectedLesson) {
      return "재생할 레슨을 선택하세요.";
    }
    if (!loggedIn) {
      return "로그인 후 재생할 수 있습니다.";
    }
    if (!enrollment) {
      return "수강 신청 후 재생할 수 있습니다.";
    }
    if (!canPlay) {
      return "관리자 승인 대기 중입니다.";
    }
    return "재생할 레슨을 선택하세요.";
  };

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
      setShowEnrollCard(true);
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    }
  };

  const fetchProgress = async (enrollmentId: number) => {
    const rows = await apiRequest<Progress[]>(
      `/api/progress?enrollmentId=${enrollmentId}`
    );
    setProgressRows(rows);
    return rows;
  };

  useEffect(() => {
    if (!courseId || !loggedIn) {
      return;
    }
    apiRequest<Enrollment>(`/api/enroll?courseId=${courseId}`)
      .then((data) => {
        setEnrollment(data);
        void fetchProgress(data.id);
      })
      .catch(() => {});
  }, [courseId, loggedIn]);

  const recordProgress = async (
    progressValue: number,
    lessonId?: number | null,
    lastPositionSec?: number | null,
    shouldReload = true
  ) => {
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
    if (existingProgress && existingProgress.progressPercent >= progressValue && lastPositionSec == null) {
      return;
    }
    try {
      const data = await apiRequest<Progress>("/api/progress", {
        method: "POST",
        body: JSON.stringify({
          enrollmentId: enrollment.id,
          lessonId: lessonIdToUse,
          progressPercent: progressValue,
          lastPositionSec: lastPositionSec ?? null,
        }),
      });
      setError(null);
      if (shouldReload) {
        await fetchProgress(enrollment.id);
      }
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

  const PLAYER_STATE = { ENDED: 0, PLAYING: 1, PAUSED: 2 };

  const stopSaveTimer = () => {
    if (saveTimerRef.current != null) {
      window.clearInterval(saveTimerRef.current);
      saveTimerRef.current = null;
    }
  };

  const saveLastPosition = async (lesson: Lesson, position: number, force = false) => {
    if (!loggedIn || !enrollment) {
      return;
    }
    const normalized = Math.max(0, Math.floor(clampPosition(lesson, position)));
    if (!force && lastSavedPositionRef.current != null) {
      if (Math.abs(normalized - lastSavedPositionRef.current) < 5) {
        return;
      }
    }
    lastSavedPositionRef.current = normalized;
    const existing = progressRows.find((row) => row.lessonId === lesson.id);
    const progressValue = existing ? existing.progressPercent : 1;
    await recordProgress(progressValue, lesson.id, normalized, false);
  };

  const persistCurrentPosition = () => {
    const lesson = activeLessonRef.current;
    if (!lesson || !playerRef.current) {
      return;
    }
    const currentTime = playerRef.current.getCurrentTime?.();
    if (typeof currentTime === "number") {
      void saveLastPosition(lesson, currentTime, true);
    }
  };

  const handlePlayerState = (event: { data: number }) => {
    const lesson = activeLessonRef.current;
    if (!lesson || !playerRef.current) {
      return;
    }
    if (event.data === PLAYER_STATE.PLAYING) {
      if (saveTimerRef.current == null) {
        saveTimerRef.current = window.setInterval(() => {
          const currentLesson = activeLessonRef.current;
          if (!currentLesson || !playerRef.current) {
            return;
          }
          const currentTime = playerRef.current.getCurrentTime?.();
          if (typeof currentTime === "number") {
            void saveLastPosition(currentLesson, currentTime);
          }
        }, 10000);
      }
      return;
    }
    if (event.data === PLAYER_STATE.PAUSED) {
      const currentTime = playerRef.current.getCurrentTime?.();
      if (typeof currentTime === "number") {
        void saveLastPosition(lesson, currentTime, true);
      }
      stopSaveTimer();
      return;
    }
    if (event.data === PLAYER_STATE.ENDED) {
      const currentTime = playerRef.current.getCurrentTime?.();
      if (typeof currentTime === "number") {
        void saveLastPosition(lesson, currentTime, true);
      }
      stopSaveTimer();
      void recordProgress(100, lesson.id, currentTime ?? null);
    }
  };

  const loadLessonIntoPlayer = (lesson: Lesson) => {
    if (!lesson.videoUrl) {
      return;
    }
    const videoId = getVideoId(lesson.videoUrl);
    if (!videoId) {
      return;
    }
    const pendingResume = pendingResumeRef.current;
    const defaultStart = Math.max(0, Math.floor(getResumeSeconds(lesson)));
    const startSeconds =
      typeof pendingResume === "number" && pendingResume > 0 ? pendingResume : defaultStart;
    const endSeconds =
      typeof lesson.endSec === "number" ? Math.max(0, Math.floor(lesson.endSec)) : undefined;

    const init = () => {
      stopSaveTimer();
      playerReadyRef.current = false;
      if (!playerRef.current) {
        playerRef.current = new window.YT.Player("lesson-player", {
          videoId,
          playerVars: {
            start: startSeconds,
            end: endSeconds,
            rel: 0,
            modestbranding: 1,
          },
          events: {
            onReady: () => {
              if (!playerRef.current) {
                return;
              }
              playerReadyRef.current = true;
              if (pendingResumeRef.current != null) {
                const resumeSeconds = pendingResumeRef.current;
                pendingResumeRef.current = null;
                playerRef.current.seekTo?.(resumeSeconds, true);
                playerRef.current.playVideo?.();
                resumeAppliedRef.current = true;
                return;
              }
            },
            onStateChange: handlePlayerState,
          },
        });
      } else {
        playerRef.current.loadVideoById({ videoId, startSeconds, endSeconds });
        playerReadyRef.current = true;
        if (pendingResumeRef.current != null) {
          const resumeSeconds = pendingResumeRef.current;
          pendingResumeRef.current = null;
          playerRef.current.seekTo?.(resumeSeconds, true);
          playerRef.current.playVideo?.();
          resumeAppliedRef.current = true;
        }
      }
      if (pendingResumeRef.current != null && startSeconds > 0) {
        pendingResumeRef.current = null;
        resumeAppliedRef.current = true;
      } else {
        resumeAppliedRef.current = startSeconds > 0;
      }
    };

    if (window.YT && window.YT.Player) {
      init();
      return;
    }

    const previous = window.onYouTubeIframeAPIReady;
    window.onYouTubeIframeAPIReady = () => {
      if (previous) {
        previous();
      }
      init();
    };
  };

  useEffect(() => {
    activeLessonRef.current = selectedLesson;
  }, [selectedLesson]);

  useEffect(() => {
    resumeAppliedRef.current = false;
    lastSavedPositionRef.current = null;
    playerReadyRef.current = false;
    stopSaveTimer();
  }, [selectedLessonId]);

  useEffect(() => {
    if (typeof document === "undefined") {
      return;
    }
    if (document.getElementById("youtube-iframe-api")) {
      return;
    }
    const tag = document.createElement("script");
    tag.src = "https://www.youtube.com/iframe_api";
    tag.id = "youtube-iframe-api";
    document.body.appendChild(tag);
  }, []);

  useEffect(() => {
    const handleVisibility = () => {
      if (document.visibilityState === "hidden") {
        persistCurrentPosition();
      }
    };
    const handlePageHide = () => {
      persistCurrentPosition();
    };
    document.addEventListener("visibilitychange", handleVisibility);
    window.addEventListener("pagehide", handlePageHide);
    return () => {
      document.removeEventListener("visibilitychange", handleVisibility);
      window.removeEventListener("pagehide", handlePageHide);
    };
  }, [loggedIn, enrollment]);

  useEffect(() => {
    if (!selectedLesson || !selectedLesson.videoUrl) {
      return;
    }
    loadLessonIntoPlayer(selectedLesson);
  }, [selectedLessonId]);

  useEffect(() => {
    if (!selectedLesson || !playerRef.current) {
      return;
    }
    if (resumeAppliedRef.current) {
      return;
    }
    const resumeSeconds = getResumeSeconds(selectedLesson);
    if (resumeSeconds <= 0) {
      return;
    }
    const currentTime = playerRef.current.getCurrentTime?.();
    if (typeof currentTime === "number" && currentTime > 1) {
      return;
    }
    playerRef.current.seekTo?.(resumeSeconds, true);
    resumeAppliedRef.current = true;
  }, [progressRows, selectedLessonId]);

  useEffect(() => () => stopSaveTimer(), []);

  return (
    <main className="page">
      <div className="page-header">
        <h1>강의 상세</h1>
        <p className="muted">수강 신청과 진도 업데이트</p>
      </div>
      {error && <p className="error">{error}</p>}
      {course && (
        <section className="card">
          <div style={{ display: "flex", justifyContent: "space-between", gap: "1.5rem" }}>
            <div>
              <h2>{course.title}</h2>
              <p>{course.description}</p>
            </div>
            <div>
              <button className="btn btn-primary" onClick={enroll}>
                수강 신청
              </button>
            </div>
          </div>
        </section>
      )}
      <section className="card section">
        <h3>강의 재생</h3>
        <div className="player">
          {selectedLesson && selectedLesson.videoUrl && canPlay ? (
            <div id="lesson-player" className="player-frame" />
          ) : (
            <div className="player-screen">
              <span>{playerMessage()}</span>
            </div>
          )}
          <div className="player-meta">
            <p className="muted">
              {selectedLesson ? selectedLesson.title : "레슨을 선택하면 영상이 표시됩니다."}
            </p>
            <button
              className="btn"
              type="button"
              disabled={!canPlay}
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
              className="btn btn-secondary"
              type="button"
              disabled={!selectedLesson || !canPlay}
              onClick={async () => {
                if (!selectedLesson || !playerRef.current) {
                  if (selectedLesson) {
                    if (!enrollment) {
                      setError("수강 신청 후 이어보기가 가능합니다.");
                      return;
                    }
                    let rows: Progress[] = [];
                    try {
                      rows = await fetchProgress(enrollment.id);
                    } catch (err) {
                      setError((err as Error).message);
                      return;
                    }
                    const resumeSeconds = getResumeSeconds(selectedLesson, rows);
                    if (resumeSeconds > 0) {
                      pendingResumeRef.current = resumeSeconds;
                      loadLessonIntoPlayer(selectedLesson);
                      return;
                    }
                    setError("저장된 시점이 없습니다.");
                  }
                  return;
                }
                if (!enrollment) {
                  setError("수강 신청 후 이어보기가 가능합니다.");
                  return;
                }
                let rows: Progress[] = [];
                try {
                  rows = await fetchProgress(enrollment.id);
                } catch (err) {
                  setError((err as Error).message);
                  return;
                }
                const resumeSeconds = getResumeSeconds(selectedLesson, rows);
                if (resumeSeconds <= 0) {
                  setError("저장된 시점이 없습니다.");
                  return;
                }
                if (!playerReadyRef.current) {
                  pendingResumeRef.current = resumeSeconds;
                  loadLessonIntoPlayer(selectedLesson);
                  return;
                }
                playerRef.current.seekTo?.(resumeSeconds, true);
                playerRef.current.playVideo?.();
                resumeAppliedRef.current = true;
              }}
            >
              이어보기
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
                {completedLessonIds.has(lesson.id) ? " (완료)" : ""}
              </strong>
              <p className="muted">{lesson.content ?? "설명은 준비 중입니다."}</p>
              <button
                className="btn btn-secondary btn-sm"
                type="button"
                disabled={!canPlay}
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
          {enrollment && (
            <button className="btn" onClick={() => fetchProgress(enrollment.id)}>
              진도 불러오기
            </button>
          )}
        </div>
        {showEnrollCard && (
          <div className="card">
            <p>수강 신청이 완료되었습니다.</p>
            <button
              className="btn btn-secondary"
              type="button"
              onClick={() => setShowEnrollCard(false)}
            >
              닫기
            </button>
          </div>
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


