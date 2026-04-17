package com.platform.warranty;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

public class CodeGenerator {
    public static void main(String[] args) {

//        当前工作目录：D:\productivity projects\Curriculum learning platform\curriculum-learning-platform-backend
        // src\main\java\com\platform\curriculum
        String currentWorkingDir = System.getProperty("user.dir");
    // 使用 FastAutoGenerator 快速配置代码生成器
FastAutoGenerator.create("jdbc:mysql://123.57.221.79:3306/logistics_warranty?serverTimezone=GMT%2B8",
                "root", "Lcp20010801..")
    .globalConfig(builder -> {
        builder
            .outputDir("D:\\Productivity Project\\Campus logistics warranty system\\campus-logistics-warranty-server"+"\\src\\main\\java"); // 输出目录
    })
    .packageConfig(builder -> {
        builder.parent("com.platform.warranty") // 设置父包名
            .entity("domain.entity") // 设置实体类包名
            .mapper("mapper") // 设置 Mapper 接口包名
            .service("service") // 设置 Service 接口包名
            .serviceImpl("service.impl"); // 设置 Service 实现类包名
    })
    .strategyConfig(builder -> {
        builder.addInclude("dorm_facility") // 设置需要生成的表名
            .entityBuilder()
            .enableLombok() // 启用 Lombok
            .enableTableFieldAnnotation() // 启用字段注解
            .controllerBuilder().enableRestStyle()
            .enableRestStyle(); // 启用 REST 风格

    })
    .templateEngine(new FreemarkerTemplateEngine()) // 使用 Freemarker 模板引擎
    .execute(); // 执行生成
}}