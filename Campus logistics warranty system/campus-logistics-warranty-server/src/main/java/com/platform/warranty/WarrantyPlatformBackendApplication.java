package com.platform.warranty;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.platform.warranty.mapper")
public class WarrantyPlatformBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarrantyPlatformBackendApplication.class, args);
    }

}
