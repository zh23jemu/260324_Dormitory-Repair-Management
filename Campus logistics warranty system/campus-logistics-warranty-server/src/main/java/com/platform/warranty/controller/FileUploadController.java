package com.platform.warranty.controller;

import com.platform.warranty.utils.AjaxResult;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/file")
public class FileUploadController {

    public static final String UPLOAD_FILE =  System.getProperty("user.dir")
            // D:\Productivity Project\Lost and found on campus
            +"\\campus-logistics-warranty-server\\src\\main\\resources\\upload\\";
    @SneakyThrows
    @PostMapping("/upload")
    public AjaxResult<String> uploadImage(@RequestParam("file") MultipartFile file) {
    try {
        // 获取原始文件名
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            return AjaxResult.error("无法获取文件名！");
        }

        // 获取文件扩展名
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf(".");
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex); // 包含点号，例如 ".jpg"
        }

        // 使用时间戳生成新的文件名
        String timestamp = String.valueOf(System.currentTimeMillis());
        String newFileName = timestamp + fileExtension;

        // 确保上传目录存在
        File dir = new File(UPLOAD_FILE);
        if (!dir.exists()) {
            dir.mkdirs(); // 创建目录
        }

        // 保存文件到指定路径
        Path path = Paths.get(UPLOAD_FILE + newFileName);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        // 构建完整的访问 URL（假设项目运行在 localhost:8080）
        String fileUrl = "http://localhost:8080/upload/" + newFileName;

        return AjaxResult.success(fileUrl);
    } catch (IOException e) {
        e.printStackTrace();
        return AjaxResult.error("文件上传失败");
    }
}

}