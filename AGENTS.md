# AGENTS.md

## Project snapshot
- Java 17 / Spring Boot 3.1 backend for industrial software management: users/permissions, license workflows, file distribution/upload, component delivery, and process monitoring.
- Start from `src/main/java/com/scut/industrial_software/IndustrialSoftwareApplication.java`; packages are organized by domain under `controller/`, `service/`, `mapper/`, `model/`, `config/`, `filter/`, `utils/`.

## Architecture that matters before editing
- Authentication is **not enforced by Spring Security rules**: `config/SecurityConfig.java` permits all requests, while real auth happens in MVC interceptors from `config/MvcConfig.java`.
- Request flow for protected APIs is: `RefreshInterceptor` -> `AuthInterceptor` -> controller. `filter/RefreshInterceptor.java` reads `Authorization`, checks blacklist, parses JWT, refreshes Redis TTL atomically via `LuaScriptConstants.VALIDATE_AND_REFRESH_TOKEN_SCRIPT`, then stores the current user in `utils/UserHolder`.
- `filter/AuthInterceptor.java` only checks `UserHolder`; if you add a protected endpoint, do not rely on `@PreAuthorize` alone.
- Public endpoints are hard-coded in `MvcConfig.java` (`/auth/jsonLogin`, `/auth/verifyCode`, `/modUsers/register`). Update that list when adding unauthenticated APIs.

## Data layer conventions
- This codebase mixes **MyBatis-Plus base mappers** with **handwritten XML SQL**.
- Mapper XML lives in `src/main/java/com/scut/industrial_software/mapper/xml/`, not `src/main/resources`; this is intentional and supported by `pom.xml` resource copying plus `application.yaml.template` mapper-locations.
- When changing query behavior, check both the Java service and the XML mapper. Example: license apply filtering spans `service/impl/LicenseServiceImpl.java` and `mapper/xml/LicenseApplyMapper.xml`.
- Entities/DTOs/VOs are split under `model/entity`, `model/dto`, `model/vo`. Controllers usually return `common/api/ApiResult<T>`; binary downloads are returned as `ResponseEntity<byte[]>` (see `LicenseCreatorController`).

## Concurrency and stateful patterns
- Login flow is concurrency-sensitive: `service/impl/AuthServiceImpl.java` uses `DistributedLockUtil` with Redis key `user:login:lock:{username}`, replaces the stored token at `user:token:{username}:token`, and blacklists the old token through `TokenBlacklistService`.
- Permission updates are also serialized: `service/impl/PermissionServiceImpl.java` locks on `permission:change:{userId}` and invalidates the user token after changing permissions.
- License generation uses **Redisson locks**, not the simple Redis lock helper, because generation may run longer (`service/impl/LicenseServiceImpl.java`, key prefix `license:generate:`).
- License request status is partly maintained asynchronously: `service/impl/LicenseApplyStatusAsyncService.java` stores a refresh cursor in Redis (`license:update`) and works with `LicenseApplyMapper.xml`. If you touch VALID/OVERDUE behavior, review both pieces together.
- `model/entity/ModUsers.java` has `@Version`; concurrent updates on `mod_users` should follow the existing `updateById(...)` optimistic-locking pattern (see password change in `ModUsersServiceImpl.java`).

## External integrations / config gotchas
- Runtime config comes from `src/main/resources/application.yaml`; use `application.yaml.template` as the safe reference. Avoid overwriting real credentials/paths unless the task is explicitly config-related.
- Important integrations: MySQL, Redis, Redisson, Aliyun OSS (`utils/AliOSSUtils.java`), TrueLicense keystores/certs, filesystem-based upload/license/component paths, OSHI/Sigar monitoring.
- Many paths are Windows-oriented in SQL/config examples. Follow the safer `Path`/`Files` normalization style already used in `LicenseServiceImpl.java` for uploads/downloads.
- Treat `src/main/resources/keystore.p12`, `src/main/resources/license/`, `src/main/resources/output/`, and generated `target/` artifacts as sensitive/non-routine edit targets.

## Build / test / debug workflow
- Build jar: `mvn clean package -DskipTests`
- Run app locally: `mvn spring-boot:run` or `java -jar target/industrial_software-0.0.1-SNAPSHOT.jar`
- Run tests: `mvn test`
- Best targeted regression for recent license-query logic: `mvn -Dtest=LicenseApplyQueryIntegrationTest test`
- `LicenseApplyQueryIntegrationTest` uses H2 in MySQL mode and mocks Redis/Redisson; prefer that style when testing mapper/service logic without external infrastructure.
- `VerifyCodeControllerTest` writes an image to `target/verifyCode.jpg`; avoid mistaking that output for source assets.

## Practical editing advice for agents
- Before changing auth/session code, trace the Redis key usage in `RedisConstants.java`, the Lua scripts in `LuaScriptConstants.java`, and both interceptors.
- Before moving mapper XML files, remember the build relies on `pom.xml` copying `src/main/java/**/*.xml`; changing location requires updating build + mapper config.
- Prefer small, domain-local changes: controller -> service -> mapper/XML -> tests. This repo has several cross-file workflows where changing only one layer creates subtle regressions.

