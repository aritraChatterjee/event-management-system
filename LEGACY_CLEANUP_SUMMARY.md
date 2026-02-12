# Legacy Folder Cleanup - Summary

## ✅ Cleanup Complete

Successfully removed legacy deployment configuration folders that are no longer needed with the Maven + Docker deployment strategy.

**Date**: February 12, 2026

---

## 🗑️ Folders Removed

### `/etc/` Directory - ✅ REMOVED

Legacy PaaS deployment configurations for:

#### 1. CloudFoundry (`/etc/cloudfoundry/`)
- **File**: `manifest.yml`
- **Purpose**: CloudFoundry deployment manifest
- **Reason for Removal**: 
  - Referenced old Gradle build paths
  - CloudFoundry is a legacy deployment target
  - Modern deployment uses Docker containers
  - No active CloudFoundry deployments

#### 2. Heroku (`/etc/heroku/`)
- **Files**: `Procfile`, `system.properties`
- **Purpose**: Heroku deployment configuration
- **Reason for Removal**:
  - Generic configuration, easily recreated if needed
  - No active Heroku deployments
  - Docker is the primary deployment method
  - Can be restored in minutes if Heroku deployment needed

#### 3. HAProxy Error Pages (`/etc/haproxy-error-pages/`)
- **File**: `error-503.http`
- **Purpose**: Custom HAProxy 503 error page
- **Reason for Removal**:
  - Generic infrastructure configuration
  - Not application-specific
  - Can use standard HAProxy error pages
  - Easily recreated if custom pages needed

---

## 📁 Folders Kept

### `/clevercloud/` Directory - ✅ KEPT

**Why Kept**:
- ✅ **Active deployment target** - CleverCloud is used for deployments
- ✅ **Required location** - CleverCloud expects config at `/clevercloud/jar.json`
- ✅ **Updated for Maven** - Configuration migrated from Gradle to Maven
- ✅ **Build integration** - Maven copies JAR to `target/clevercloud/`

**File**: `jar.json`
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

---

## 📊 Impact Analysis

### Repository Size
- **Reduced**: Removed ~5 small configuration files
- **Cleaner**: Fewer deployment options to maintain
- **Focused**: Primary deployment via Docker + CleverCloud fallback

### Deployment Options

**BEFORE Cleanup**:
```
✓ Docker (primary)
✓ CleverCloud
? CloudFoundry (legacy, untested)
? Heroku (generic config)
? HAProxy (custom error pages)
```

**AFTER Cleanup**:
```
✓ Docker (primary - GitHub Actions automated)
✓ CleverCloud (alternative - Git push deployment)
✓ Direct JAR (any Java 17+ environment)
```

---

## 🚀 Current Deployment Strategy

### Primary: Docker Containers

**Automated via GitHub Actions**:
```yaml
On tag push → Build → Multi-platform image → Push to:
  - ghcr.io/alfio-event/alf.io
  - alfio/alf.io (Docker Hub)
```

**Platforms**: linux/amd64, linux/arm64

**Usage**:
```bash
docker pull alfio/alf.io:latest
docker run -p 8080:8080 alfio/alf.io:latest
```

### Alternative: CleverCloud

**Git-based deployment**:
```bash
git push clevercloud main
# Auto-detects Maven, builds, deploys
```

**Configuration**: `/clevercloud/jar.json` (Maven-based)

### Fallback: Direct JAR

**Build & Run**:
```bash
mvn clean package
java -jar target/alfio-2.0-M6-SNAPSHOT-boot.jar
```

**Works on**: Any server with Java 17+

---

## 🔄 Restoration Guide

If you need to restore any removed configurations:

### CloudFoundry
1. Create `manifest.yml` at project root:
```yaml
---
applications:
  - name: alfio
    path: target/alfio-2.0-M6-SNAPSHOT-boot.jar
    buildpack: java_buildpack
    memory: 512M
    env:
      JBP_CONFIG_OPEN_JDK_JRE: '{ jre: { version: 17.+ } }'
```

2. Deploy:
```bash
cf push
```

### Heroku

1. Create `Procfile` at project root:
```
web: java -Dserver.port=$PORT $JAVA_OPTS -jar target/alfio-*-boot.jar
```

2. Create `system.properties`:
```
java.runtime.version=17
```

3. Deploy:
```bash
git push heroku main
```

### HAProxy Custom Error Pages

1. Create `error-503.http` (or any location HAProxy config points to)
2. Configure HAProxy:
```
errorfile 503 /path/to/error-503.http
```

---

## 📝 Archive Information

All removed configurations have been documented in:
- **REMOVED_DEPLOYMENT_CONFIGS.md** - Detailed archive with full file contents

This allows quick restoration if any legacy deployment target is needed in the future.

---

## ✅ Verification

**Removed Folders**:
```bash
$ ls etc/
ls: etc/: No such file or directory ✓
```

**Kept Folders**:
```bash
$ ls clevercloud/
jar.json ✓
```

**Deployment Still Works**:
- ✅ Docker build: `mvn clean package` → `target/dockerize/`
- ✅ CleverCloud: Config present at `/clevercloud/jar.json`
- ✅ Direct JAR: Available at `target/alfio-*-boot.jar`

---

## 🎯 Benefits

**Simplified Repository**:
- ✅ Fewer deployment configs to maintain
- ✅ Focus on actively-used deployment methods
- ✅ Clearer project structure
- ✅ Reduced confusion for new developers

**Maintained Flexibility**:
- ✅ Easy to restore any removed config (documented)
- ✅ Primary deployment (Docker) unchanged
- ✅ Alternative deployment (CleverCloud) maintained
- ✅ Generic JAR deployment always available

**Better Alignment**:
- ✅ Matches modern deployment practices
- ✅ Aligns with Maven build structure
- ✅ Supports containerization strategy
- ✅ Reduces legacy platform dependencies

---

## 📈 Related Changes

This cleanup is part of the larger Gradle → Maven migration:

1. ✅ Gradle build files removed
2. ✅ Maven `pom.xml` created
3. ✅ GitHub Actions updated to Maven
4. ✅ CleverCloud config updated to Maven
5. ✅ Legacy deployment configs removed ← **YOU ARE HERE**
6. ✅ DDD documentation created

**Full migration documented in**:
- GRADLE_REMOVAL_SUMMARY.md
- DDD_MAPPING_SUMMARY.md

---

**Cleanup Status**: ✅ COMPLETE  
**Deployment Impact**: ✅ NONE (all active deployments maintained)  
**Restoration Time**: ~5 minutes if needed  
**Documentation**: ✅ COMPLETE

---

**Performed By**: Development Team  
**Date**: February 12, 2026  
**Version**: 1.0

