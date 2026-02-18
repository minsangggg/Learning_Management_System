# Backend

## Run
- Build WAR: `mvn clean package`
- Run local: `mvn spring-boot:run`

## Notes
- Packaging: WAR (external Tomcat)
- Flyway migrations: `backend/src/main/resources/db/migration`
- Auth headers: `X-User-Id`, `X-Role` (ADMIN/LEARNER)

## API Samples (JSON)

### Courses (Admin create/update/delete; Admin+Learner read)
- `GET /api/courses`
  - Response:
    ```json
    [
      {"id":1,"title":"Java","description":"Intro","createdAt":"2026-01-01T10:00:00Z"}
    ]
    ```
- `GET /api/courses/1`
  - Response:
    ```json
    {"id":1,"title":"Java","description":"Intro","createdAt":"2026-01-01T10:00:00Z"}
    ```
- `POST /api/courses`
  - Body:
    ```json
    {"title":"Java","description":"Intro"}
    ```
  - Response:
    ```json
    {"id":1,"title":"Java","description":"Intro","createdAt":"2026-01-01T10:00:00Z"}
    ```
- `PUT /api/courses/1`
  - Body:
    ```json
    {"title":"Java 2","description":"Updated"}
    ```
- `DELETE /api/courses/1`

### Lessons (Admin)
- `GET /api/lessons?courseId=1`
  - Response:
    ```json
    [{"id":10,"courseId":1,"title":"Lesson 1","content":"...","orderNo":1}]
    ```
- `POST /api/lessons`
  - Body:
    ```json
    {"courseId":1,"title":"Lesson 1","content":"...","orderNo":1}
    ```
- `PUT /api/lessons/10`
  - Body:
    ```json
    {"title":"Lesson 1","content":"...","orderNo":1}
    ```
- `DELETE /api/lessons/10`

### Enroll (Learner)
- `POST /api/enroll`
  - Body:
    ```json
    {"courseId":1}
    ```
  - Response:
    ```json
    {"id":100,"userId":5,"courseId":1,"status":"ENROLLED","enrolledAt":"2026-01-10T09:00:00Z"}
    ```

### Progress (Learner)
- `GET /api/progress?enrollmentId=100`
  - Response:
    ```json
    [{"id":500,"enrollmentId":100,"lessonId":10,"progressPercent":50.0,"completedAt":null}]
    ```
- `POST /api/progress`
  - Body:
    ```json
    {"enrollmentId":100,"lessonId":10,"progressPercent":100}
    ```
  - Response:
    ```json
    {"id":500,"enrollmentId":100,"lessonId":10,"progressPercent":100.0,"completedAt":"2026-01-10T10:00:00Z"}
    ```

### Reports (Admin)
- `GET /api/reports/course-period?from=2026-01-01&to=2026-01-31`
  - Response:
    ```json
    [{"courseId":1,"courseTitle":"Java","learnerCount":10,"avgProgress":65.5}]
    ```
- `GET /api/reports/course-completion?from=2026-01-01&to=2026-01-31`
  - Response:
    ```json
    [{"courseId":1,"courseTitle":"Java","completedLearners":3}]
    ```
- CSV:
  - `GET /api/reports/course-period.csv?...`
  - `GET /api/reports/course-completion.csv?...`

### AI (Admin)
- `POST /api/ai/summary`
  - Body:
    ```json
    {"text":"Input text..."}
    ```
  - Response:
    ```json
    {"output":"Summary text","status":"OK","latencyMs":1234}
    ```
- `POST /api/ai/quiz`
  - Body:
    ```json
    {"text":"Input text..."}
    ```
  - Response:
    ```json
    {"output":"Quiz text","status":"OK","latencyMs":1400}
    ```

## Error Format (Common)
```json
{"code":"VALIDATION_ERROR","message":"Title is required"}
```

## Report SQL (SELECT + JOIN + GROUP BY)

### Period / Course
```sql
SELECT
  c.ID AS COURSE_ID,
  c.TITLE AS COURSE_TITLE,
  COUNT(DISTINCT e.USER_ID) AS LEARNER_COUNT,
  NVL(AVG(p.PROGRESS_PERCENT), 0) AS AVG_PROGRESS
FROM COURSES c
JOIN ENROLLMENTS e ON e.COURSE_ID = c.ID
LEFT JOIN PROGRESS p ON p.ENROLLMENT_ID = e.ID
WHERE e.ENROLLED_AT >= ? AND e.ENROLLED_AT < (? + 1)
GROUP BY c.ID, c.TITLE
ORDER BY c.ID
```

### Completion / Course
```sql
SELECT
  c.ID AS COURSE_ID,
  c.TITLE AS COURSE_TITLE,
  COUNT(DISTINCT CASE WHEN p.PROGRESS_PERCENT = 100 THEN e.USER_ID END) AS COMPLETED_LEARNERS
FROM COURSES c
JOIN ENROLLMENTS e ON e.COURSE_ID = c.ID
LEFT JOIN PROGRESS p ON p.ENROLLMENT_ID = e.ID
WHERE e.ENROLLED_AT >= ? AND e.ENROLLED_AT < (? + 1)
GROUP BY c.ID, c.TITLE
ORDER BY c.ID
```

### Index Notes
- `ENROLLMENTS(COURSE_ID, ENROLLED_AT)` speeds course/date filtering in reports.
- `PROGRESS(ENROLLMENT_ID)` speeds joins from enrollments to progress.
- `PROGRESS(LESSON_ID)` speeds progress lookup per lesson.
- `COURSES(ID)` is PK used in joins.
