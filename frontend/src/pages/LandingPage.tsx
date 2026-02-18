import { getUserId } from "../auth";

type LandingPageProps = {
  onLogin: () => void;
  onLogout: () => void;
  onExplore: () => void;
  onPlanner: () => void;
  onProgress: () => void;
  onAiSupport: () => void;
};

const highlights = [
  { label: "과정", value: "48+" },
  { label: "학습자", value: "1,200+" },
  { label: "완료율", value: "88%" },
];

const packages = [
  {
    name: "Starter",
    price: "무료",
    desc: "프로젝트 기본 체험 구성",
    items: ["과정/강의 목록", "수강 신청", "기본 리포트"],
  },
  {
    name: "Pro",
    price: "월 9,000",
    desc: "운영자를 위한 확장 기능",
    items: ["진도 관리", "CSV 리포트", "AI 요약/퀴즈"],
    highlight: true,
  },
  {
    name: "Enterprise",
    price: "문의",
    desc: "맞춤형 운영 환경",
    items: ["외부 시스템 연동", "맞춤 리포트", "전담 지원"],
  },
];

const samples = [
  {
    title: "학습 로드맵",
    desc: "역량 기반 학습 설계를 카드로 시각화",
  },
  {
    title: "관리자 대시보드",
    desc: "수강/진도/성과 데이터를 요약",
  },
  {
    title: "AI 학습 보조",
    desc: "요약과 퀴즈를 자동으로 생성",
  },
  {
    title: "성과 리포트",
    desc: "기간/과정별 성과를 집계",
  },
];

export default function LandingPage({
  onLogin,
  onExplore,
  onPlanner,
  onProgress,
  onAiSupport,
  onLogout,
}: LandingPageProps) {
  const loggedIn = Boolean(getUserId());
  return (
    <main className="landing">
      <section className="hero" id="home">
        <div className="hero-overlay" />
        <div className="hero-inner">
          <div className="hero-copy">
            <p className="hero-eyebrow">Mini LMS Portfolio</p>
            <h1>학습 경험을 설계하는 가장 간단한 방법</h1>
            <p>
              과정 관리, 수강 신청, 진도 추적, 리포트, AI 요약까지.
              실제 서비스 흐름을 그대로 구현한 미니 LMS입니다.
            </p>
            <div className="hero-actions">
              <button className="btn btn-primary" onClick={onExplore}>
                강의 둘러보기
              </button>
              {loggedIn ? (
                <button className="btn" onClick={onLogout}>
                  로그아웃
                </button>
              ) : (
                <button className="btn" onClick={onLogin}>
                  로그인
                </button>
              )}
            </div>
            <div className="hero-highlights">
              {highlights.map((item) => (
                <div key={item.label} className="highlight">
                  <strong>{item.value}</strong>
                  <span>{item.label}</span>
                </div>
              ))}
            </div>
          </div>
          <div className="hero-books">
            <button type="button" className="book-card card-olive" onClick={onPlanner}>
              <span>Course Planner</span>
              <strong>학습 설계 가이드</strong>
            </button>
            <button type="button" className="book-card card-coral" onClick={onProgress}>
              <span>Progress</span>
              <strong>진도 리포트</strong>
            </button>
            <button type="button" className="book-card card-ivory" onClick={onAiSupport}>
              <span>AI Support</span>
              <strong>요약 &amp; 퀴즈</strong>
            </button>
          </div>
        </div>
      </section>

      <section className="split" id="intro">
        <div className="split-media intro-media" />
        <div className="split-content">
          <h2>데이터 흐름을 담는 LMS 구조</h2>
          <p>
            프론트, 백엔드, DB까지 데이터 흐름을 그대로 설계했습니다.
            학습자와 관리자 화면이 동일한 데이터 기반으로 연결됩니다.
          </p>
          <ul className="bullet-list">
            <li>과정, 강의, 수강, 진도 데이터 연결</li>
            <li>관리자 리포트 SQL 증빙 제공</li>
            <li>OpenAI 요약/퀴즈 요청 로그 저장</li>
          </ul>
          <button className="btn btn-primary" onClick={onExplore}>
            강의 둘러보기
          </button>
        </div>
      </section>

      <section className="pricing" id="packages">
        <div className="section-head">
          <h2>패키지</h2>
          <p>운영 목적에 맞는 구성을 선택하세요.</p>
        </div>
        <div className="pricing-grid">
          {packages.map((pack) => (
            <div
              key={pack.name}
              className={`pricing-card ${pack.highlight ? "highlight" : ""}`}
            >
              <div className="pricing-header">
                <h3>{pack.name}</h3>
                <span className="price">{pack.price}</span>
              </div>
              <p className="muted">{pack.desc}</p>
              <ul>
                {pack.items.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
              <button className={`btn ${pack.highlight ? "btn-primary" : ""}`}>
                {pack.highlight ? "추천 선택" : "자세히 보기"}
              </button>
            </div>
          ))}
        </div>
      </section>

      <section className="samples" id="samples">
        <div className="section-head">
          <h2>샘플 섹션</h2>
          <p>실제 화면을 카드 형태로 요약했습니다.</p>
        </div>
        <div className="sample-grid">
          {samples.map((sample, idx) => (
            <div key={sample.title} className="sample-card">
              <div className={`sample-media sample-${idx + 1}`} />
              <h3>{sample.title}</h3>
              <p className="muted">{sample.desc}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="contact" id="contact">
        <div className="contact-inner">
          <div>
            <h2>상담/문의</h2>
            <p>
              LMS 구축 목적과 예산만 적어주세요.
              필요한 범위를 빠르게 정리해 드립니다.
            </p>
          </div>
          <form className="contact-form">
            <label>
              이름
              <input placeholder="홍길동" />
            </label>
            <label>
              이메일
              <input placeholder="email@example.com" />
            </label>
            <label>
              문의 내용
              <textarea rows={4} placeholder="필요한 기능과 일정을 알려주세요." />
            </label>
            <button className="btn btn-primary" type="button">
              문의 제출
            </button>
          </form>
        </div>
      </section>

      <footer className="footer">
        <div>
          <h3>Mini LMS</h3>
          <p>학습 경험을 위한 프론트엔드 포트폴리오</p>
          <p>contact@minilms.dev</p>
        </div>
        <div>
          <h4>고객센터</h4>
          <ul>
            <li>공지사항</li>
            <li>자주 묻는 질문</li>
            <li>상담/문의</li>
          </ul>
        </div>
        <div>
          <h4>회사정보</h4>
          <ul>
            <li>서울특별시 강남구</li>
            <li>사업자등록번호 000-00-00000</li>
            <li>운영시간 10:00 ~ 18:00</li>
          </ul>
        </div>
      </footer>
    </main>
  );
}
