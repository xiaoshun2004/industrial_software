package com.scut.industrial_software;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.ChangePasswordDTO;
import com.scut.industrial_software.model.dto.UserDTO;
import com.scut.industrial_software.service.IModUsersService;
import com.scut.industrial_software.utils.UserHolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;


@SpringBootTest
public class PasswordChangeTest {

    @Autowired
    private IModUsersService modUsersService;

    @Test
    public void testConcurrentPasswordChange() throws InterruptedException, ExecutionException {
        // 构造密码修改请求数据
        ChangePasswordDTO dto1 = new ChangePasswordDTO();
        dto1.setOldPassword("asdfgh");
        dto1.setNewPassword("123456");

        ChangePasswordDTO dto2 = new ChangePasswordDTO();
        dto2.setOldPassword("asdfgh");
        dto2.setNewPassword("123456");

        // 构建线程池
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<String> task1 = () -> {
            UserDTO user = new UserDTO();
            user.setId(1); // 模拟当前登录用户 ID
            UserHolder.saveUser(user); // 设置当前用户上下文

            ApiResult<Object> result = this.modUsersService.changePassword(UserHolder.getUser().getId(),dto1);

            UserHolder.removeUser(); // 清理上下文
            return "线程1修改结果: " + result.getMessage();
        };

        Callable<String> task2 = () -> {
            UserDTO user = new UserDTO();
            user.setId(1); // 同一个用户，模拟并发
            UserHolder.saveUser(user);

            ApiResult<Object> result = this.modUsersService.changePassword(UserHolder.getUser().getId(),dto2);

            UserHolder.removeUser();
            return "线程2修改结果: " + result.getMessage();
        };

        Future<String> future1 = executor.submit(task1);
        Future<String> future2 = executor.submit(task2);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println(future1.get());
        System.out.println(future2.get());
    }
}
