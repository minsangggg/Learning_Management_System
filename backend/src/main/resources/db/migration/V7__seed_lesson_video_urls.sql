UPDATE LESSONS l
SET l.VIDEO_URL = 'https://www.youtube.com/watch?v=rSS5yM74zeo'
WHERE l.COURSE_ID = (SELECT ID FROM COURSES WHERE TITLE = 'AI시대 창의력 훈련법')
  AND (l.VIDEO_URL IS NULL OR l.VIDEO_URL <> 'https://www.youtube.com/watch?v=rSS5yM74zeo');

UPDATE LESSONS l
SET l.VIDEO_URL = 'https://www.youtube.com/watch?v=Am1i8c1bZto'
WHERE l.COURSE_ID = (SELECT ID FROM COURSES WHERE TITLE = '창의력을 높이는 테크닉')
  AND (l.VIDEO_URL IS NULL OR l.VIDEO_URL <> 'https://www.youtube.com/watch?v=Am1i8c1bZto');

UPDATE LESSONS l
SET l.VIDEO_URL = 'https://www.youtube.com/watch?v=aasrgF5LWdU'
WHERE l.COURSE_ID = (SELECT ID FROM COURSES WHERE TITLE = '데이터 분석의 힘')
  AND (l.VIDEO_URL IS NULL OR l.VIDEO_URL <> 'https://www.youtube.com/watch?v=aasrgF5LWdU');
