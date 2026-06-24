package com.itheima.controller;

import com.itheima.pojo.Result;
import com.itheima.utils.AliOssUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileUploadController {

    @PostMapping("/upload")
    public Result upload(MultipartFile file) throws Exception{
        // 获取上传的文件名
        String originalFilename = file.getOriginalFilename();
        // 保证文件名字一致，防止文件覆盖
        String fileName = System.currentTimeMillis() + originalFilename;
        String url = AliOssUtil.uploadFile(fileName, file.getInputStream());
        return Result.success(url);
    }
}
