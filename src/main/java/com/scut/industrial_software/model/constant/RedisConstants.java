package com.scut.industrial_software.model.constant;

public class RedisConstants {

    // 用户 token 的 Redis 键前缀
    public static final String USER_TOKEN_KEY_PREFIX = "user:token:";

    // 验证码的 Redis 键前缀
    public static final String VERIFY_CODE_PREFIX = "verifyCode:";

    // 用户登录锁的 Redis 键前缀
    public static final String USER_LOGIN_LOCK_PREFIX = "user:login:lock:";

    // 工具证书生成的 Redis 键前缀
    public static final String TOOL_LICENSE_KEY_PREFIX = "license:generate:";

    // 记录证书申请表上一次过期更新的时间
    public static final String LICENSE_LAST_UPDATE = "license:update";

    // 证书申请状态异步刷新的分布式锁
    public static final String LICENSE_STATUS_REFRESH_LOCK = "license:status:refresh:lock";

    // 任务调度资源锁前缀（按求解器类型限流）
    public static final String TASK_SCHEDULE_LOCK_PREFIX = "task:schedule:solver:";

    // 任务运行实例锁前缀（防止重复启动）
    public static final String TASK_RUN_LOCK_PREFIX = "task:run:";

    // 文件分片上传的索引总数
    public static final String FILE_UPLOAD_CHUNK_PREFIX = "file:upload:chunk:";

    // token 的过期时间（30分钟）
    public static final long TOKEN_TTL = 30L;

    // 黑名单token过期时间
    public static final long BLACKLIST_TOKEN_TTL = 10L;

    // 验证码的过期时间（5分钟）
    public static final long VERIFY_CODE_TTL = 5L; // 验证码有效期为 5 分钟

    // 登录锁的过期时间（10秒）
    public static final long LOGIN_LOCK_TTL = 10L; // 登录锁过期时间，防止死锁
}

