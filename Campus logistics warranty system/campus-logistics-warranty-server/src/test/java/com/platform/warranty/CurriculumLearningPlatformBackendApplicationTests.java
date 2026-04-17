package com.platform.warranty;

import com.platform.warranty.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CurriculumLearningPlatformBackendApplicationTests {

    @Autowired
    private JwtUtils jwtUtils;
    @Test
    void contextLoads() {
        System.out.println(jwtUtils.createToken(1L));
        // 获取当前工作目录
        String currentWorkingDir = System.getProperty("user.dir");
        System.out.println("当前工作目录：" + currentWorkingDir);
    }

}
