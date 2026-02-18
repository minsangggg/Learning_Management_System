# Infra

## Overview
This folder describes how to run the backend as a WAR on external Tomcat, fronted by Apache reverse proxy.

## 1) Build WAR
1. `cd backend`
2. `mvn clean package`
3. Output: `backend/target/lms-backend-0.1.0-SNAPSHOT.war`

## 2) Tomcat (external)
1. Install Tomcat 10.x (compatible with Spring Boot 3.x / Jakarta).
2. Copy WAR to `TOMCAT_HOME/webapps/`
3. Start Tomcat and verify: `http://localhost:8080/<context>/api/health`
   - Context is the WAR file name without `.war`.
4. See detailed steps in `infra/tomcat/README.md`

## 3) Apache Reverse Proxy
1. Use config sample in `infra/apache/httpd-lms.conf.sample`
2. Enable required modules: `proxy`, `proxy_http`, `headers`
3. Reload Apache after applying the vhost config

## 4) (Optional) Local Docker Compose
1. Use `infra/docker-compose.yml`
2. `docker compose up -d`
3. Services: Oracle XE, Tomcat, Apache
4. See compose notes inside the file
