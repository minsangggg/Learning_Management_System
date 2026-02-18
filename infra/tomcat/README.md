## Tomcat external deployment

### Server folder structure (example)
- `TOMCAT_HOME/`
  - `bin/` (startup scripts)
  - `conf/` (server.xml, context.xml)
  - `lib/` (shared libs)
  - `webapps/` (WAR deployments)
  - `logs/`

### Deploy steps (WAR)
1. Build WAR: `cd backend` → `mvn clean package`
2. Copy WAR to `TOMCAT_HOME/webapps/`
   - Example: `lms-backend-0.1.0-SNAPSHOT.war`
3. Start Tomcat:
   - Windows: `TOMCAT_HOME/bin/startup.bat`
   - Linux/macOS: `TOMCAT_HOME/bin/startup.sh`
4. Verify:
   - `http://localhost:8080/lms-backend-0.1.0-SNAPSHOT/api/health`

### Notes
- Spring Boot 3.x uses Jakarta (Tomcat 10+).
- DB config is in `backend/src/main/resources/application.yml`
  - For production, prefer environment variables or externalized config.
