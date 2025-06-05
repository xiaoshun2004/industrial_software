# 登录并发问题解决方案

## 问题分析

在多用户并发登录场景下，原系统可能存在以下并发问题：

1. **重复登录竞态条件**：同一用户在极短时间内多次发起登录请求
2. **Token状态不一致**：登录过程中Token的生成和存储不是原子操作
3. **会话覆盖问题**：新登录直接覆盖旧Token，没有合适的通知机制

## 解决方案

### 1. 分布式锁机制

**实现类**：`DistributedLockUtil`

**核心功能**：
- 基于Redis的分布式锁实现
- 支持自动获取和释放锁
- 使用Lua脚本确保操作原子性
- 提供简洁的API供业务层使用

**使用示例**：
```java
// 执行带锁的操作，如果获取锁失败抛出异常
distributedLockUtil.executeWithLock(lockKey, expireTime, timeUnit, () -> {
    // 业务逻辑
    return result;
});

// 尝试执行带锁的操作，如果获取锁失败返回默认值
distributedLockUtil.tryExecuteWithLock(lockKey, expireTime, timeUnit, 
    () -> businessLogic(), defaultValue);
```

### 2. 登录流程优化

**实现类**：`AuthServiceImpl`

**关键改进**：
- 使用分布式锁确保同一用户同时只能有一个登录操作
- 检测并清理旧的登录会话
- 原子性更新Token信息
- 记录登录历史

**登录流程**：
```
1. 获取用户级别的分布式锁
2. 验证用户凭据
3. 检查现有会话，如有则将旧token加入黑名单
4. 生成新Token并更新Redis
5. 自动释放锁
```

### 3. 黑名单机制优化

**实现类**：`TokenBlacklistService`

**功能特性**：
- 统一的Token黑名单管理
- 设备踢下线功能（将旧token加入黑名单）
- 完善的异常处理和日志记录

### 4. Token验证增强

**实现位置**：`TokenFilter`

**改进点**：
- 使用Lua脚本原子性验证Token
- 自动刷新Token过期时间
- 集成黑名单检查机制

### 5. Lua脚本管理

**实现类**：`LuaScriptConstants`

**目的**：
- 集中管理Redis Lua脚本
- 确保操作的原子性
- 提高代码可维护性

## Redis键设计

```
user:login:lock:{username}     # 登录锁
user:token:{username}:token    # 用户Token
blacklist:{token}              # Token黑名单
verifyCode:{key}               # 验证码
```

## 并发控制效果

1. **防止重复登录**：同一用户并发登录请求会被串行化处理
2. **Token一致性**：确保Token的生成、存储、验证过程原子化
3. **会话管理**：新登录自动踢出旧会话，提供明确的状态变化通知
4. **异常处理**：优雅处理锁竞争失败的情况，给用户友好提示

## 性能考虑

1. **锁粒度**：使用用户级别的锁，不影响不同用户的并发登录
2. **锁超时**：设置合理的锁超时时间（10秒）防止死锁
3. **Lua脚本**：减少网络往返次数，提高Redis操作效率
4. **异步处理**：登录历史记录等非关键操作可以异步处理

## 架构优势

1. **简洁高效**：利用现有黑名单机制，避免引入额外的服务类
2. **统一管理**：所有Token失效操作都通过黑名单机制处理
3. **扩展性好**：`DistributedLockUtil` 可用于其他需要分布式锁的场景
4. **维护简单**：减少了代码复杂度，降低维护成本

## 踢下线机制

当用户在新设备登录时：
1. 检测到旧设备的token存在
2. 调用 `tokenBlacklistService.kickOutDevice()` 将旧token加入黑名单
3. 旧设备的后续请求会在 `TokenFilter` 中被拒绝
4. 新设备正常使用新token 