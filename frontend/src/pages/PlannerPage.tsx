import { useEffect, useState } from "react";
import { apiRequest } from "../api/client";
import { getRole } from "../auth";

type Guide = {
  id: number;
  userId: number;
  userName?: string | null;
  userEmail?: string | null;
  courseId: number;
  courseTitle: string;
  guideText: string;
  createdAt?: string | null;
};

export default function PlannerPage() {
  const role = getRole();
  const [guides, setGuides] = useState<Guide[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!role) {
      return;
    }
    apiRequest<Guide[]>("/api/guides")
      .then((data) => {
        setGuides(data);
        setError(null);
      })
      .catch((err) => {
        setError((err as Error).message);
      });
  }, [role]);

  return (
    <main className="page">
      <div className="page-header">
        <h1>Course Planner · 학습 설계 가이드</h1>
        <p className="muted">
          학습 목표, 기간, 과제, 평가를 한 번에 설계하고 공유하는 영역입니다.
        </p>
      </div>

      <section className="card">
        <h2>학습 설계 가이드</h2>
        {error && <p className="error">{error}</p>}
        {!error && guides.length === 0 && (
          <p className="muted">표시할 학습 설계 가이드가 없습니다.</p>
        )}
        {guides.length > 0 && (
          <ul className="card-grid">
            {guides.map((guide) => (
              <li key={guide.id}>
                <strong>{guide.courseTitle}</strong>
                {role === "ADMIN" && (
                  <p className="muted">
                    {guide.userName || guide.userEmail || `User ${guide.userId}`}
                  </p>
                )}
                <p className="muted">{guide.guideText}</p>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="card">
        <h2>설계 흐름</h2>
        <ul className="bullet-list">
          <li>학습 목표 정의 → 역량/스킬 맵 설정</li>
          <li>주차별 레슨 구성 → 과제/퀴즈 매핑</li>
          <li>중간 점검 → 리포트/피드백 루프</li>
          <li>완료 기준 확정 → 수료 리포트 산출</li>
        </ul>
      </section>

      <section className="section">
        <h2>설계 요소</h2>
        <ul className="card-grid">
          <li>
            <strong>목표</strong>
            <p className="muted">과정 완료 시 달성해야 할 결과를 정의합니다.</p>
          </li>
          <li>
            <strong>주차 설계</strong>
            <p className="muted">주차/레슨별 난이도와 흐름을 설정합니다.</p>
          </li>
          <li>
            <strong>과제/퀴즈</strong>
            <p className="muted">학습 확인을 위한 평가 항목을 배치합니다.</p>
          </li>
          <li>
            <strong>리포트</strong>
            <p className="muted">관리자와 학습자에게 공유할 기준을 정합니다.</p>
          </li>
        </ul>
      </section>

      <section className="section card">
        <h2>샘플 일정</h2>
        <pre>
{`1주차  : 핵심 개념 정리 / 미니 퀴즈
2~3주차: 실습 과제 / 피드백
4주차  : 중간 점검 리포트
5~6주차: 심화 레슨 / 적용 사례
7주차  : 최종 과제 / 수료 리포트`}
        </pre>
        <div className="form-actions">
          <button className="btn btn-primary" type="button">
            설계 시작하기
          </button>
          <button className="btn" type="button">
            템플릿 보기
          </button>
        </div>
      </section>
    </main>
  );
}
