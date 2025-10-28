package com.scut.industrial_software.utils;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class WindowFocusUtil {
    private static final Logger logger = LoggerFactory.getLogger(WindowFocusUtil.class);

    // 定义User32接口
    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = Native.load("user32", User32.class);

        boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer userData);
        int GetWindowThreadProcessId(WinDef.HWND hWnd, IntByReference lpdwProcessId);
        boolean IsWindowVisible(WinDef.HWND hWnd);
        boolean ShowWindow(WinDef.HWND hWnd, int nCmdShow);
        boolean SetForegroundWindow(WinDef.HWND hWnd);
        boolean SetWindowPos(WinDef.HWND hWnd, WinDef.HWND hWndInsertAfter,
                             int X, int Y, int cx, int cy, int uFlags);
        boolean BringWindowToTop(WinDef.HWND hWnd);
        WinDef.HWND GetForegroundWindow();
    }

    // 窗口命令常量
    private static final int SW_RESTORE = 9;
    private static final int SW_SHOW = 5;
    private static final int SW_MAXIMIZE = 3;

    // SetWindowPos 标志
    private static final int SWP_NOSIZE = 0x0001;
    private static final int SWP_NOMOVE = 0x0002;
    private static final int SWP_NOZORDER = 0x0004;
    private static final int SWP_SHOWWINDOW = 0x0040;

    // 特殊窗口句柄
    private static final WinDef.HWND HWND_TOP = new WinDef.HWND(Pointer.createConstant(0));
    private static final WinDef.HWND HWND_TOPMOST = new WinDef.HWND(Pointer.createConstant(-1));
    private static final WinDef.HWND HWND_NOTOPMOST = new WinDef.HWND(Pointer.createConstant(-2));

    /**
     * 将指定PID的进程窗口带到前台
     */
    public static boolean bringProcessWindowToFront(long pid, long timeoutMillis) {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        WinDef.HWND targetWindow = null;

        while (System.currentTimeMillis() < deadline && targetWindow == null) {
            targetWindow = findWindowByPid(pid);

            if (targetWindow == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (targetWindow == null) {
            logger.warn("未找到PID为 {} 的可见窗口", pid);
            return false;
        }

        return activateWindow(targetWindow);
    }

    /**
     * 根据进程PID查找窗口
     */
    private static WinDef.HWND findWindowByPid(long pid) {
        final AtomicReference<WinDef.HWND> foundWindow = new AtomicReference<>();

        User32.INSTANCE.EnumWindows((hWnd, data) -> {
            IntByReference processId = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hWnd, processId);

            if (processId.getValue() == (int) pid && User32.INSTANCE.IsWindowVisible(hWnd)) {
                foundWindow.set(hWnd);
                return false; // 停止枚举
            }
            return true; // 继续枚举
        }, null);

        return foundWindow.get();
    }

    /**
     * 激活指定窗口
     */
    private static boolean activateWindow(WinDef.HWND hWnd) {
        try {
            // 1. 先恢复窗口（如果最小化）
            User32.INSTANCE.ShowWindow(hWnd, SW_RESTORE);

            // 2. 使用多种方法尝试激活窗口

            // 方法1: 直接设置前台窗口
            boolean success = User32.INSTANCE.SetForegroundWindow(hWnd);

            if (!success) {
                // 方法2: 先设置为顶层窗口，然后取消顶层状态，最后设置前台
                User32.INSTANCE.SetWindowPos(hWnd, HWND_TOPMOST, 0, 0, 0, 0,
                        SWP_NOSIZE | SWP_NOMOVE);
                User32.INSTANCE.SetWindowPos(hWnd, HWND_NOTOPMOST, 0, 0, 0, 0,
                        SWP_NOSIZE | SWP_NOMOVE);
                success = User32.INSTANCE.SetForegroundWindow(hWnd);
            }

            if (!success) {
                // 方法3: 使用BringWindowToTop
                success = User32.INSTANCE.BringWindowToTop(hWnd);
            }

            if (!success) {
                // 方法4: 强制显示窗口
                User32.INSTANCE.ShowWindow(hWnd, SW_SHOW);
                User32.INSTANCE.SetWindowPos(hWnd, HWND_TOP, 0, 0, 0, 0,
                        SWP_NOSIZE | SWP_NOMOVE | SWP_SHOWWINDOW);
            }

            logger.debug("窗口激活{}成功", success ? "" : "未");
            return success;

        } catch (Exception e) {
            logger.error("激活窗口时发生异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 将窗口设置为始终置顶
     */
    public static boolean setWindowAlwaysOnTop(long pid) {
        WinDef.HWND window = findWindowByPid(pid);
        if (window == null) return false;

        return User32.INSTANCE.SetWindowPos(window, HWND_TOPMOST, 0, 0, 0, 0,
                SWP_NOSIZE | SWP_NOMOVE);
    }

    /**
     * 取消窗口的置顶状态
     */
    public static boolean unsetWindowAlwaysOnTop(long pid) {
        WinDef.HWND window = findWindowByPid(pid);
        if (window == null) return false;

        return User32.INSTANCE.SetWindowPos(window, HWND_NOTOPMOST, 0, 0, 0, 0,
                SWP_NOSIZE | SWP_NOMOVE);
    }
}
