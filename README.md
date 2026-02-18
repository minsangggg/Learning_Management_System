## 개요
- 소규모 학원의 학습 흐름(수강 → 진도 → 리포트 → 피드백)을 간단히 체험할 수 있는 미니 LMS입니다.
- 학습자는 레슨 콘텐츠 기반의 요약/퀴즈를 통해 복습하고, 관리자는 진도 리포트를 확인합니다.

## 기술스택
- 프론트엔드: React, TypeScript, Vite
- 백엔드: Java 17, Spring Boot, Spring JDBC
- 데이터베이스: Oracle XE (Docker)
- 마이그레이션: Flyway
- 인프라(옵션): Apache Reverse Proxy, 외부 Tomcat 샘플
- 외부 API: OpenAI Responses API

## 구조
- frontend/: React + TypeScript + Vite
- backend/: Spring Boot (Java 17, WAR packaging)
- infra/: Apache reverse proxy + external Tomcat samples
- docs/: project docs

