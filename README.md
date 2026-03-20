# Industrial Software（工业软件后台）

## 项目简介
Spring Boot 3.x 的工业软件管理平台，提供用户/角色与权限管理、组件与文件分发、许可证验证、系统监控、WebSocket 推送等能力，整合 MyBatis-Plus、Spring Security + JWT、Redis、Aliyun OSS、调度与健康检查。

## 技术栈
- 后端：Spring Boot 3.1.x、Spring Web、Spring Security、Spring Data JPA、MyBatis-Plus
- 数据与缓存：MySQL、Redis
- 通信与工具：JWT（jjwt）、WebSocket、Redisson、Springfox Swagger、Fastjson
- 文件/证书：TrueLicense
- 监控：OSHI、Sigar

## 目录结构
- `src/main/java/com/scut/industrial_software`：业务代码（`controller/`、`service/`、`mapper/`、`config/`、`filter/`、`utils/` 等），主类 `IndustrialSoftwareApplication`。
- `src/main/resources`：`application.yaml` 配置、静态资源、证书/密钥、MyBatis XML 等。
- `SqlHistory/`、`industrial_software_system.sql`：数据库 schema 及历史版本。
- 文档：`CONCURRENT_LOGIN_SOLUTION.md`、`PERMISSION_CONCURRENCY_SOLUTION.md`、`LicenseManageFactory.md`。
- 手工测试用例：`测试用例/`。

## 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8（或兼容版），建议预先创建数据库并导入 `industrial_software_system.sql`（或 `SqlHistory/` 下最新 SQL）
- Redis 6+
- 可选：Aliyun OSS 访问凭证，本地/远程组件安装包及许可证文件路径

## 配置准备
1. 复制 `src/main/resources/application.yaml.template` 为 `src/main/resources/application.yaml`（也可放到外部路径）。
2. 关键项（示例占位请按需填写）：
   - `server.port`: 默认 `8081`。
   - `spring.datasource.*`: JDBC URL/用户名/密码/驱动。
   - `spring.data.redis.*`: host/port/password、pool 配置。
   - `ali.oss.*`: endpoint/bucket/accessKey/secretKey（使用 OSS 时）。
   - `files.upload.path`: 本地上传目录，需读写权限。
   - `component.store.*`: 组件安装包、解压目录路径。
   - `ptc.license.*`、`license.store.*`: 许可证密钥/证书存储及口令。
3. 运行时可用 `--server.port=XXXX` 或环境变量覆盖对应配置。
4. 打包后的运行推荐使用外置配置：将 `target/config/application.yaml.template` 复制为 `config/application.yaml`，填写后与 jar 同级部署。

## 构建与运行
```bash
mvn clean package -DskipTests
java -jar target/industrial_software-0.0.1-SNAPSHOT.jar
# 开发模式
mvn spring-boot:run
```

### 外置配置打包与部署（推荐）
- 打包时不会将 `src/main/resources/application.yaml` 打进 jar，避免把本地环境配置带给他人。
- 打包后会自动生成 `target/config/application.yaml.template`，供部署环境填写。

```bash
# 1) 打包
mvn clean package -DskipTests

# 2) 准备外置配置（Windows PowerShell）
New-Item -ItemType Directory -Force .\target\deploy\config
Copy-Item .\target\industrial_software-0.0.1-SNAPSHOT.jar .\target\deploy\
Copy-Item .\target\config\application.yaml.template .\target\deploy\config\application.yaml

# 3) 编辑 .\target\deploy\config\application.yaml 后运行
java -jar .\target\deploy\industrial_software-0.0.1-SNAPSHOT.jar
```

## 测试
- 测试目录：`src/test/java/com/scut/industrial_software`（含验证码、密码修改、系统信息等测试）。
- 运行：`mvn test`

## 常见问题排查
- 端口占用：修改 `server.port` 或释放端口。
- 数据库连接失败：核对 `spring.datasource.url`/账号/网络，确保已导入 SQL。
- Redis 连接失败：检查 `spring.data.redis.*` 配置与连通性。
- 文件/证书路径异常：确保目录存在且进程有读写权限。

## 部署注意
- 部署时携带所需证书/密钥、许可证、组件包及可写的上传目录。
- JVM 内存可按需调整：`JAVA_OPTS="-Xms512m -Xmx1024m"`。

## 参考文档
- 并发登录方案：`CONCURRENT_LOGIN_SOLUTION.md`
- 权限并发方案：`PERMISSION_CONCURRENCY_SOLUTION.md`
- 许可证管理说明：`LicenseManageFactory.md`

## 许可
仓库未附带开源许可文件，默认视为专有项目，使用前请确认授权。
