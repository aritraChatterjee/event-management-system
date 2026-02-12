# Removed Legacy Deployment Configurations

This document records the deployment configurations that were removed during the Maven migration cleanup.

**Date Removed**: February 12, 2026  
**Reason**: Legacy PaaS configurations for Gradle-based builds, no longer needed with Maven + Docker deployment strategy

---

## Folders Removed

### 1. `/etc/` Directory

Legacy deployment configurations for various PaaS platforms.

#### `/etc/cloudfoundry/`
**Contents**: CloudFoundry deployment manifest

**File**: `manifest.yml`
```yaml
---
applications:
    - name: alfio
      path: alf.io-boot.jar
      instances: 1
      buildpack: https://github.com/cloudfoundry/java-buildpack.git
      memory: 256m
      env:
          JBP_CONFIG_OPEN_JDK_JRE: '{ jre: { version: 17.+ } }'
```

**Notes**: 
- Referenced old Gradle JAR naming convention
- CloudFoundry is a legacy deployment target
- Modern deployment uses Docker containers

---

#### `/etc/heroku/`
**Contents**: Heroku deployment configuration

**File**: `Procfile`
```
web: java -Dserver.port=$PORT $JAVA_OPTS -jar alfio-*-boot.jar
```

**File**: `system.properties`
```
java.runtime.version=17
```

**Notes**:
- Generic Java deployment for Heroku
- Works with both Gradle and Maven builds
- Can be recreated if Heroku deployment is needed

---

#### `/etc/haproxy-error-pages/`
**Contents**: Custom HAProxy error page

**File**: `error-503.http`
```http
HTTP/1.0 503 Service Unavailable
Cache-Control: no-cache
Connection: close
Content-Type: text/html

<!DOCTYPE html>
<html>
<head>
    <title>Service Temporarily Unavailable</title>
    <style>
        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
        h1 { color: #333; }
    </style>
</head>
<body>
    <h1>Service Temporarily Unavailable</h1>
    <p>We're currently performing maintenance. Please try again in a few minutes.</p>
</body>
</html>
```

**Notes**:
- Generic infrastructure configuration
- Not application-specific
- Can be recreated if needed for HAProxy deployments

---

### 2. `/clevercloud/` Directory - **KEPT** ✅

**Status**: This folder is **NOT removed** - it's required for CleverCloud deployment.

**File**: `jar.json` - **UPDATED FOR MAVEN**
```json
{
    "build": {
        "type": "maven",
        "goal": "package -DskipTests"
    },
    "deploy": {
        "jarName": "target/clevercloud/alfio-boot.jar"
    }
}
```

**Why Kept**: 
- ✅ CleverCloud requires config in `/clevercloud/jar.json`
- ✅ Configuration updated for Maven builds
- ✅ Maven build copies JAR to `target/clevercloud/` directory
- ✅ Active deployment target for the project

---

## Current Deployment Strategy

After cleanup, the project supports:

### ✅ Primary: Docker Deployment
- `Dockerfile` in `src/main/dist/`
- Docker Compose for local development
- GitHub Actions builds and pushes to:
  - GitHub Container Registry (`ghcr.io/alfio-event/alf.io`)
  - Docker Hub (`alfio/alf.io`)

### ✅ Alternative: CleverCloud
- Configuration in `/clevercloud/jar.json` (or can be at root)
- Maven-based build
- Automatic deployment on push

### ✅ Generic: Direct JAR Deployment
- Build with: `mvn clean package`
- Run JAR: `target/alfio-2.0-M6-SNAPSHOT-boot.jar`
- Works on any Java 17+ environment

---

## Restoration Instructions

If you need to restore any of these configurations:

### CloudFoundry
1. Create `manifest.yml` with updated JAR path:
   ```yaml
   applications:
     - name: alfio
       path: target/alfio-*-boot.jar
       buildpack: java_buildpack
       memory: 512m
   ```

### Heroku
1. Create `Procfile`:
   ```
   web: java -Dserver.port=$PORT $JAVA_OPTS -jar target/alfio-*-boot.jar
   ```
2. Create `system.properties`:
   ```
   java.runtime.version=17
   ```

### HAProxy Error Pages
- Generic HTTP error pages
- Can use standard HAProxy error page templates
- Or recreate custom page as needed

---

## Migration Impact

**Before Removal**:
- ❌ Multiple deployment configs for legacy platforms
- ❌ References to old Gradle build paths
- ❌ Unused configuration files

**After Removal**:
- ✅ Clean repository focused on Docker deployment
- ✅ Single primary deployment method (Docker)
- ✅ Reduced maintenance overhead
- ✅ Clearer project structure

---

## Recommended Deployment

**Production**: Docker containers via GitHub Actions
- Automated builds on tag push
- Multi-platform support (amd64, arm64)
- Published to Docker Hub and GitHub Container Registry

**Development**: Docker Compose
```bash
docker-compose up
```

**Alternative**: CleverCloud
- Git push deployment
- Maven-based builds
- Configuration in `/clevercloud/jar.json`

---

**Document Version**: 1.0  
**Last Updated**: February 12, 2026  
**Archived By**: Development Team

