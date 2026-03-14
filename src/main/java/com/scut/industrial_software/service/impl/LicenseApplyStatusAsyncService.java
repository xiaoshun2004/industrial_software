package com.scut.industrial_software.service.impl;

import com.scut.industrial_software.mapper.LicenseApplyMapper;
import com.scut.industrial_software.model.constant.RedisConstants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class LicenseApplyStatusAsyncService {

    private static final Logger log = LoggerFactory.getLogger(LicenseApplyStatusAsyncService.class);

    private static final DateTimeFormatter CURSOR_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final LicenseApplyMapper licenseApplyMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    public LicenseApplyStatusAsyncService(LicenseApplyMapper licenseApplyMapper,
                                          StringRedisTemplate stringRedisTemplate,
                                          RedissonClient redissonClient) {
        this.licenseApplyMapper = licenseApplyMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
    }

    @Async("taskExecutor")
    public void markExpiredValidAsOverdue() {
        LocalDateTime current = LocalDateTime.now();
        RLock lock = redissonClient.getLock(RedisConstants.LICENSE_STATUS_REFRESH_LOCK);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, 30, TimeUnit.SECONDS);
            if (!locked) {
                return;
            }

            LocalDateTime lastCursor = readLastCursor();
            if (lastCursor != null && lastCursor.isAfter(current)) {
                return;
            }

            int affectedRows = licenseApplyMapper.markExpiredValidAsOverdue(lastCursor, current);
            LocalDateTime nextCursor = licenseApplyMapper.selectNextValidToAfter(current);
            writeCursor(nextCursor != null ? nextCursor : current);

            if (affectedRows > 0) {
                log.info("Refreshed {} expired VALID license apply records to OVERDUE. nextCursor={}", affectedRows, nextCursor);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while refreshing expired license apply status", ex);
        } catch (Exception ex) {
            log.error("Failed to refresh expired VALID license apply records", ex);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void markExpiredValidAsOverdue(String ignored) {
        markExpiredValidAsOverdue();
    }

    public void alignCursorForValidApply(LocalDateTime validTo) {
        if (validTo == null) {
            return;
        }
        RLock lock = redissonClient.getLock(RedisConstants.LICENSE_STATUS_REFRESH_LOCK);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, 10, TimeUnit.SECONDS);
            if (!locked) {
                return;
            }

            LocalDateTime currentCursor = readLastCursor();
            if (currentCursor == null || validTo.isBefore(currentCursor)) {
                writeCursor(validTo);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while aligning license status refresh cursor", ex);
        } catch (Exception ex) {
            log.error("Failed to align license status refresh cursor. validTo={}", validTo, ex);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private LocalDateTime readLastCursor() {
        String cursorValue = stringRedisTemplate.opsForValue().get(RedisConstants.LICENSE_LAST_UPDATE);
        if (cursorValue == null || cursorValue.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(cursorValue, CURSOR_FORMATTER);
        } catch (DateTimeParseException ex) {
            log.warn("Invalid license status refresh cursor in redis: {}", cursorValue);
            return null;
        }
    }

    private void writeCursor(LocalDateTime cursor) {
        if (cursor == null) {
            return;
        }
        stringRedisTemplate.opsForValue().set(RedisConstants.LICENSE_LAST_UPDATE, cursor.format(CURSOR_FORMATTER));
    }
}