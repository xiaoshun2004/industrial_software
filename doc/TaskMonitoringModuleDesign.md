# 任务管理监控模块设计文档（模板）

> 使用说明：本模板用于规划 `ModTasksServiceImpl` 与监控服务（`IMonitorService` / `MonitorServiceImpl`）相关改造。
> 你只需补充每个小节中的 `[待填写]` 内容，Agent 即可按文档执行设计与开发。

## 0. 文档元信息
- 需求名称: 用户任务管理监控信息的获取与相关参数的设置
- 需求编号/Issue: IDS-001
- 提交人: Ming
- 评审人: Ming
- 目标分支: `master`
- 计划时间窗口: 不填

---

## 1. 背景与目标
### 1.1 背景
- 当前业务场景: 当用户在平台上创建一个任务启动运行后，管理员能够在任务管理界面看到该任务的状态（待执行/执行中/已完成/已失败等）以及执行进度，当然也包括一些任务的基本信息（例如任务名称、开始时间、所属服务器等）
- 现存痛点: 当前管理员的任务管理模块还未能正确获取不同任务的运行情况，后台对任务的启动执行也未起到一个监控查看运行状态与执行进度的作用
- 触发条件与影响范围: 当用户创建一个任务后，后台这里就需要开始记录这个任务的运行状态和进度了，在任务执行过程中尤其重要，并且能够实时将执行状态反馈给客户端界面，告诉这个前端这个任务是待执行还是执行中（执行进度是多少）等等

### 1.2 目标（可验收）
- G1: 功能性目标：对于用户创建的每一个任务能够实时得到后台执行的状态情况
- G2: 性能目标：服务器应当支撑百万级任务的运行，同时同步给前端界面的监控信息时延不能超过1秒
- G3: [待填写]

### 1.3 非目标（本次不做）
- NG1: [待填写]
- NG2: [待填写]

---

## 2. 现状基线（As-Is）
### 2.1 关键调用链
- 任务侧入口: `controller/.../ModTasksController` -> `service/impl/ModTasksServiceImpl`
- 监控侧调用: `startTask/getTaskStatus/stopTask`(用户端)、`controller/.../MonitorTaskController`(管理员端) -> `IMonitorService` -> `MonitorServiceImpl`
- 任务标识规则: `task_` 前缀及解析逻辑（例如 `substring(5)`）

### 2.2 当前状态与约束
- 任务状态集合（示例）: `待执行(pending)` / `执行中(running)` / `已完成(completed)` / `已失败(failed)`
- 分页查询路径: `ModTasksMapper` + `ModTasksMapper.xml`
- 统一返回结构: `ApiResult<T>`

### 2.3 已知问题
- P1: 当前管理员的任务管理模块还未能正确获取不同任务的运行情况
- P2: 后台对任务的启动执行也未起到一个监控查看运行状态与执行进度的作用
- P3: 任务启动后如何分配到不同服务器上运行，如何根据服务器的资源情况进行负载均衡调度，如何实时追踪运行在不同服务器上的不同任务，每一个都是核心问题点
---

## 3. 目标方案（To-Be）
### 3.1 方案概述
- 核心思路: 在用户创建启动一个任务后，后台可以根据`MonitorServiceImpl`中的任务调度模块（新增）和监控模块进行任务的执行并实时记录每个任务的运行状态和执行进度，未执行/已完成/已失败的任务更新完状态进数据库后就不实时监控（被查询就从数据库中获取），对于那些正在执行中的任务就需要通过监控模块来检查这个任务的执行进度以便于能够被`MonitorTaskServiceImpl`中的查询接口获取到，并且能够实时地反馈给前端界面进行展示
- 方案边界: 本次改造主要针对`ModTasksServiceImpl`中的任务启动执行功能进行改造，同时在`MonitorServiceImpl`中新增一个任务调度模块来实现任务的调度执行和监控功能，并且在`MonitorTaskServiceImpl`中修改查询接口来支持获取正在执行中的任务的状态和执行进度等监控信息
- 备选方案与取舍: ...

### 3.2 接口设计
- 受影响接口清单（路径/方法/鉴权要求）:
  1. `PUT /modTasks/start/{taskId}`（`ModTasksController.startTask`）
     - 用途: 启动任务，触发调度与监控链路。
     - 鉴权: 受保护接口，走 `RefreshInterceptor -> AuthInterceptor`。
  2. `GET /modTasks/status/{taskId}`（`ModTasksController.getTaskStatus`）
     - 用途: 用户端轮询任务状态（可用于简化版前端轮询）。
     - 鉴权: 受保护接口，走 `RefreshInterceptor -> AuthInterceptor`。
  3. `GET /monitoring/tasks`（`MonitorTaskController.getTasksPage`）
     - 用途: 管理员分页查询任务列表（支持监控视图）。
     - 鉴权: 受保护接口，走 `RefreshInterceptor -> AuthInterceptor`。
  4. `PUT /monitoring/tasks/{taskId}/priority`（`MonitorTaskController.updateTaskPriority`）
     - 用途: 修改任务优先级（仅未执行任务允许）。
     - 鉴权: 受保护接口，走 `RefreshInterceptor -> AuthInterceptor`。
  5. `POST /monitoring/tasks/allocate`（`MonitorTaskController.allocateResources`）
     - 用途: 资源分配预留接口（当前仅返回成功，占位实现）。
     - 鉴权: 受保护接口，走 `RefreshInterceptor -> AuthInterceptor`。

- 请求参数与校验规则:
  1. 启动任务 `PUT /modTasks/start/{taskId}`
     - `taskId` 必填，格式遵循现有任务标识规则（`task_` 前缀）。
     - 启动前校验任务状态：仅 `pending` 可启动；`running/completed/failed` 重复启动应返回业务异常。
  2. 查询状态 `GET /modTasks/status/{taskId}`
     - `taskId` 必填，格式同上；不存在任务返回业务异常。
  3. 任务分页 `GET /monitoring/tasks`
     - 参数来自 `MonitorTasksPageRequestDTO`（`@ModelAttribute @Validated`）。
     - `pageNum`/`pageSize` 必须为正整数；`keyword` 可空；`status`、`serverId` 为可选筛选条件。
     - `status` 建议限制为 `pending|running|completed|failed`（非法值返回参数错误）。
  4. 修改优先级 `PUT /monitoring/tasks/{taskId}/priority`
     - `taskId` 必填。
     - `priority` 必填，取值 `1|2|3`（高/中/低）。
     - 仅未执行任务可修改优先级；执行中或已结束任务返回业务异常。

- 响应数据结构与错误语义:
  1. 所有 Controller 正常返回统一使用 `ApiResult<T>`。
  2. `GET /monitoring/tasks` 成功返回分页结构（建议包含 `records/list`、`total`、`pageNum`、`pageSize`）。
  3. 任务状态字段统一语义：`pending`（待执行）、`running`（执行中）、`completed`（已完成）、`failed`（已失败）。
  4. 参数不合法（如 `priority` 越界、分页参数非法）返回参数错误语义；业务冲突（如重复启动、非法状态转移）返回业务异常语义。
  5. 鉴权失败由拦截器直接拦截（非 `ApiResult` 体），文档需明确前端按 HTTP 状态码处理登录失效。

- 向后兼容策略:
  1. 保持现有路径与 HTTP 方法不变（`/modTasks/*`、`/monitoring/tasks*`）。
  2. 保持 `taskId` 标识规则不变（`task_` 前缀解析逻辑不变）。
  3. 新增筛选参数均为可选，缺省时行为与当前查询一致。
  4. 状态与优先级枚举采用“向后兼容”策略：旧值继续可读，新值上线前先补充后端映射与前端字典。
  5. `allocateResources` 在实现资源调度前保持占位成功返回，避免影响现有调用链。

### 3.3 状态机设计
- 状态定义: [待执行(pending)] -> [执行中(running)] -> [已完成(completed)] / [已失败(failed)]
- 状态流转规则: 用户新建一个任务后，默认状态为待执行(pending)，当调度模块分配这个任务开始执行后，状态变为执行中(running)，当任务执行完成后，状态变为已完成(completed)，如果任务执行过程中发生错误或用户手动将任务结束导致失败，则状态变为已失败(failed)
- 非法状态转移处理: 如果用户尝试修改一个正在执行中的任务的优先级或者资源需求等参数，这些操作都是不合法的，因为正在执行中的任务已经被调度模块分配到一个服务器上运行了，这时候修改这些参数可能会导致调度模块无法正确地管理这个任务了，所以对于这些非法的状态转移操作，系统应该抛出一个异常来告知用户这个操作是非法的，并且不能被执行
- 幂等要求（重复启动/重复停止/重复查询）: 对于重复启动一个已经在执行中的任务或者已经完成/失败的任务，系统应该抛出一个异常来告知用户这个操作是非法的，并且不能被执行；对于重复停止一个已经停止或者已经完成/失败的任务，系统应该抛出一个异常来告知用户这个操作是非法的，并且不能被执行；对于重复查询一个任务的状态和执行进度等监控信息，这些操作都是合法的，因为查询操作是幂等的，所以系统应该允许用户进行这些操作，并且返回最新的状态和执行进度等监控信息

### 3.4 并发与一致性设计
- 竞态场景清单:
  1. 多个用户同时调用 `PUT /modTasks/start/{taskId}` 启动任务，可能导致同一求解器并行槽位超配。
  2. 同一任务被重复点击启动（并发重复请求），可能产生重复调度、重复落库、重复监控线程。
  3. 任务启动与管理员修改优先级并发发生，可能导致“已入运行队列后仍被改优先级”的不一致。
  4. 调度线程更新运行进度与查询接口同时读取，可能出现前端短时读到过期状态。
  5. 任务完成回写状态与失败补偿并发发生，可能出现最终状态覆盖错误（completed/failed 抖动）。
- 并发控制策略（本地并发/分布式锁/Redisson）: 分布式锁/Redisson
- 锁粒度与超时策略:
  1. 锁实现: 使用 Redisson `RLock`，并按“调度资源”与“任务实例”双层加锁。
  2. 资源锁粒度: `task:schedule:solver:{solverType}`（或 `task:schedule:server:{serverId}:solver:{solverType}`）。
     - 目的: 串行化同一求解器并行槽位分配，确保不超过最大并行数量。
  3. 任务锁粒度: `task:run:{taskId}`。
     - 目的: 保证同一任务仅能被启动一次，防止重复执行。
  4. 锁时序: 先拿资源锁，再拿任务锁；释放顺序反向，避免死锁。
  5. 超时建议:
     - `waitTime`: 2~3 秒（获取不到锁快速失败并提示稍后重试）。
     - `leaseTime`: 30~60 秒（由看门狗续期覆盖长任务场景）。
  6. 并行上限控制:
     - 为每类求解器维护 `maxParallel`（配置/缓存可热更新）。
     - 调度前读取当前运行数 `runningCount`，仅当 `runningCount < maxParallel` 才允许入运行态；否则进入待执行队列。
  7. 调度顺序:
     - 先按 `priority`（1 高 -> 2 中 -> 3 低），同优先级按创建时间 FIFO。
     - 仅 `pending` 状态可入队，`running/completed/failed` 不可重复入队。
- 失败补偿与重试策略:
  1. 启动失败补偿: 任务从 `pending -> running` 失败时，回滚为 `failed` 并记录失败原因；释放槽位。
  2. 执行中异常补偿: 监控线程发现进程异常退出时，写回 `failed`、进度冻结、记录结束时间。
  3. 重试策略: 仅对“可重试的资源竞争/网络抖动”重试（指数退避，最多 3 次）；业务非法状态不重试。
  4. 一致性落库: 状态更新采用条件更新（按旧状态约束）避免并发覆盖，例如仅允许 `pending -> running`、`running -> completed/failed`。
  5. 可观测性: 每次状态流转记录 `taskId`、`fromStatus`、`toStatus`、`serverId`、`errorMsg`，用于审计与回放。

### 3.5 数据层与持久化设计
- 涉及表与字段: 涉及`mod_tasks`表，在当前表的基础上还应当添加以下字段:
  `server_id`: 任务执行所在的服务器ID
  `server_name`: 任务执行所在的服务器名称，可以从`server`表的`name`字段中获取到
  `priority` : 任务的优先级（1为高，2为中，3为低）
  `cpu_core_need` : 任务运行需要的CPU核心数
  `memory_need` : 任务运行需要的内存大小（以GB为单位）
  `progress` : 任务的执行进度（以百分比为单位，例如已经完成了多少百分比）
  `start_time` : 任务开始运行的时间（存放Datetime类型）
- Mapper Java 改动点: 在获取任务列表时支持分页查询、关键词搜索、状态/所属服务器ID筛选
- Mapper XML 改动点: 根据Mapper的接口要求补充相关的SQL编写
- SQL 迁移脚本（如需要）: 如果可以的话，请在更改完数据表后重新生成打包一份.sql文件放置在该项目的文件夹下

---

## 4. 项目约束对齐（必须遵守）
### 4.1 鉴权链路
- 本项目受保护接口依赖拦截器链：`RefreshInterceptor` -> `AuthInterceptor`
- `SecurityConfig` 放行全部请求，不能仅依赖 `@PreAuthorize` 判断受保护性
- 如新增公开接口，需要同步更新 `MvcConfig` 放行列表

### 4.2 数据访问约定
- 本项目是 MyBatis-Plus + XML 混合模式
- XML 位于 `src/main/java/com/scut/industrial_software/mapper/xml/`
- 查询行为改动必须核对 Service 与 XML 两层逻辑

### 4.3 返回与异常约定
- 统一使用 `ApiResult<T>`
- 错误消息和码值策略: 其他具体错误信息按照`ApiException`里的全局异常信息抛异常

---

## 5. 改动清单（按文件）
> 请明确到文件和符号级别，减少 Agent 漏改风险。

1. `src/main/java/com/scut/industrial_software/service/impl/ModTasksServiceImpl.java`
   - 新增点: 根据修改点需求自适应补充新增的功能与函数
   - 修改点: 如下
     1、当创建一个新任务后，默认将它的优先级置于中（2），并且默认将它的CPU核心数和内存需求都置于默认值（1核CPU，4GB内存） 
     2、这里启动一项任务后会调用`monitorService`的`startProgram`方法，启动的是放置在`D:/exe/GETexe4/dist/main/main.exe`的应用程序，这里暂时先启动这个应用程序
2. `src/main/java/com/scut/industrial_software/service/impl/MonitorServiceImpl.java`
   - 新增点: 在执行`startProgram`方法时，这里需要额外开发出一个任务调度模块，这个模块可以新开发一个类来实现，该模块的作用在于启动不同任务后可以根据当前服务器的资源情况为这个任务分配到一个合适的服务器上运行（做到负载均衡），并且在这个服务器上需要按照任务的优先级进行调度运行，优先级高的任务先运行，优先级相同的任务按照先来先服务的原则进行调度运行，并且在这个调度模块中还需要记录每个任务的运行状态和进度情况，以便于后续能够将这些信息反馈给前端界面进行展示
     服务器的资源情况可以通过`MonitorServerServiceImpl`中的实时监控服务器资源的方法来获取到，服务器的资源情况包括CPU核心数、内存大小、当前运行的任务数量等信息，这些信息可以用来判断当前服务器是否有足够的资源来运行一个新的任务，以及在多个服务器都有足够资源的情况下选择一个负载较低的服务器来运行这个新任务
     在任务调度中，如果该任务在本地服务器上运行，那么需要在本地服务器上使用自定义线程池去执行这个任务，如果被分配到其他服务器上运行，那么需要通过RPC调用的方式去远程服务器上执行这个任务
     当一个任务被调度运行后，需要同步更改该任务的`serverId`、`serverName`和`startTime`字段信息，同时需要在调度模块中记录这个任务的运行状态（例如正在运行、已完成、已失败等）和执行进度（例如已经完成了多少百分比），这些信息可以通过定时任务的方式来更新，以便于能够实时地将这些信息反馈给前端界面进行展示
   - 修改点: 这里有个`monitorProcessCompletion`方法用于监控一个任务的完成情况，当任务被调度模块分配开始执行后就必须追踪这个任务，同时添加一个能记录执行进度的选项，并且能够及时返回监控信息
            当任务开始启动后，监控模块会定时检查这个任务的运行状态和执行进度，当任务的状态发生变化时，需要将这些信息（例如status）同步更新到数据库中，直至这个任务完成或者失败为止
3. `src/main/java/com/scut/industrial_software/service/impl/MonitorTaskServiceImpl.java`
   - 新增点: 根据修改点需求自适应补充新增的功能与函数
   - 修改点: 这里更新任务优先级的方法需要修改为更新`mod_tasks`表中的`priority`字段，而不是更新`monitor_tasks`表中的优先级字段了，并且只有未执行的任务才能修改优先级，正在执行中的任务不允许修改优先级
     这里的`getTasksPage`方法需要修改为从`mod_tasks`表中获取任务的状态和执行进度等监控信息，并且支持分页查询、根据任务名称/服务器名称关键字、任务状态和所属服务器ID进行筛选查询
     **关键点**: 这里获取任务列表时对于那些还未开始执行的任务或者已完成/已失败的任务，相关信息可以从`mod_tasks`表中获取到，而对于那些正在执行中的任务，相关信息需要从调度模块中获取到，因为这些信息是实时变化的，所以不能直接从数据库中获取到，而是需要通过调度模块来获取到最新的状态和执行进度等监控信息
     返回的列表结构包括以下字段: 任务ID、任务名称、所属服务器ID、所属服务器名称、任务类型、任务优先级、CPU核心数需求、内存需求、任务状态、执行进度、开始时间等信息，其中任务状态和执行进度等监控信息对于那些正在执行中的任务需要通过调度模块来获取到，并且能够实时地反馈给前端界面进行展示，而对于那些还未开始执行的任务或者已完成/已失败的任务，这些信息可以直接从数据库中获取到
     对于这些正在执行中的任务因为需要实时将执行进度反馈给客户端页面进行展示，为减轻传输大数据块的压力，这里可以只返回一个任务ID和一个执行进度的字段，前端页面可以根据这个任务ID去定时更改这个任务的最新执行进度信息，以便于能够实时地将这些信息反馈给前端界面进行展示
4. `src/main/java/com/scut/industrial_software/service/IMonitorService.java`
   - 新增点: 根据`MonitorServiceImpl`中的新增功能补充接口定义
   - 修改点: 根据`MonitorServiceImpl`中的新增功能补充接口定义
5. `src/main/java/com/scut/industrial_software/mapper/ModTasksMapper.java`
   - 新增点: 能够根据任务名称/服务器名称关键字、任务状态和所属服务器ID进行筛选查询，同时支持分页查询，能够被`MonitorTaskServiceImpl`中的查询接口调用
   - 修改点: 根据`ModTasksServiceImpl`和`MonitorTaskServiceImpl`中的新增功能补充接口定义
---

## 6. 测试与验收
### 6.1 功能验收标准（DoD）
- AC1: 并行上限与优先级调度生效
  - Given: 同一求解器 `maxParallel = N`，同时提交 `N+M` 个 `pending` 任务，且存在高/中/低优先级混合。
  - When: 并发触发启动接口。
  - Then: 任意时刻运行中任务数不超过 `N`；高优先级任务先于低优先级进入 `running`；同优先级按 FIFO。
- AC2: 接口行为与状态机一致
  - Given: 任务状态分别为 `pending/running/completed/failed`。
  - When: 调用启动、查询状态、修改优先级接口。
  - Then: 仅 `pending` 可启动与改优先级；非法状态转移返回业务异常；查询接口可重复调用且幂等。
- AC3: 监控信息可实时获取且最终一致
  - Given: 任务进入 `running` 且执行进度持续变化。
  - When: 管理员通过 `/monitoring/tasks` 分页查询并按条件筛选。
  - Then: 正在执行任务返回最新进度（调度模块来源）；未执行/已结束任务返回数据库快照；任务结束后状态最终一致写回数据库。

### 6.2 回归范围
- 任务分页查询回归:
  1. `GET /monitoring/tasks` 分页参数边界（`pageNum/pageSize` 最小值、超大值）。
  2. 筛选组合回归（`keyword + status + serverId`）及空条件回归。
  3. 运行中任务进度覆盖逻辑回归（调度模块实时值优先于库内旧值）。
- 任务启动/停止/状态回归:
  1. `PUT /modTasks/start/{taskId}` 正常启动、重复启动、并发启动回归。
  2. `GET /modTasks/status/{taskId}` 在四种状态下的响应一致性回归。
  3. `PUT /modTasks/stop/{taskId}` 与运行态任务的状态回写、资源释放回归。
  4. `PUT /monitoring/tasks/{taskId}/priority` 仅 `pending` 可改、其他状态拒绝回归。
- 鉴权回归:
  1. 未携带/携带失效 token 访问上述接口，应由拦截器拒绝（按 HTTP 状态码处理）。
  2. 有效 token 访问接口，业务返回体保持 `ApiResult<T>` 约定。

### 6.3 建议执行命令
```powershell
mvn -Dtest=LicenseApplyQueryIntegrationTest test
mvn test
```

---

## 7. 风险与回滚
- 技术风险:
  1. 锁粒度过粗导致吞吐下降，接口 RT 抖动。
  2. 锁粒度过细导致并行槽位校验失效，出现超配运行。
  3. 调度模块与数据库状态更新时序不当，出现短时“进度与状态不一致”。
  4. 远程服务器 RPC 执行失败时，任务可能长期停留 `running`（僵尸状态）。
  5. 定时监控频率过高导致数据库写放大、Redis 压力升高。
- 数据风险:
  1. `mod_tasks` 新增字段（`server_id/server_name/priority/cpu_core_need/memory_need/start_time`）默认值不当，影响历史数据读取。
  2. 状态回写缺少条件更新时可能发生“后写覆盖先写”，导致最终状态错误。
  3. 进度字段来源切换（数据库 vs 调度缓存）期间可能出现展示跳变。
- 回滚步骤:
  1. 代码回滚: 回退 `ModTasksServiceImpl`、`MonitorServiceImpl`、`MonitorTaskServiceImpl`、`IMonitorService`、`ModTasksMapper`、`ModTasksMapper.xml` 到上一稳定版本。
  2. 功能开关回滚: 关闭“实时调度/实时进度覆盖”开关，查询仅走数据库快照。
  3. 数据回滚: 若已执行 DDL，保留新增字段但停止写入；必要时通过脚本回填默认值保障旧查询。
  4. 验证回滚: 执行核心接口冒烟（启动/状态/分页/鉴权）并确认无 5xx 与死锁等待。
- 应急降级策略:
  1. 调度降级为“单机串行 + 固定并行上限”，暂不做跨服务器负载均衡。
  2. 进度降级为“低频刷新（例如 3~5 秒）”，减少高并发下写入压力。
  3. RPC 不可用时自动回退本地执行或进入待执行队列并提示“资源繁忙”。
  4. Redisson 异常时禁止新任务进入 `running`，仅保留查询能力，防止状态失控。

---

## 8. Agent 执行指令（建议直接复制给 Agent）
1. 先输出“详细设计草案 + 影响文件清单 + 风险点”，等待确认后再编码。
2. 编码时遵循本项目约束：拦截器鉴权、`ApiResult<T>`、MyBatis-Plus + XML 双层一致。
3. 每完成一层（controller/service/mapper/xml）做一次自检。
4. 最终输出：改动说明、测试结果、剩余风险与后续建议。

