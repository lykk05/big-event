package com.itheima.controller;

import com.itheima.exception.BizException;
import com.itheima.pojo.Result;
import com.itheima.utils.AliOssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

@RestController
public class FileUploadController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    public Result upload(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new BizException(400, "上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BizException(400, "仅支持 jpg/jpeg/png/gif/webp 图片");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BizException(400, "文件类型不合法");
        }

        // 忽略原始文件名，避免路径/超长字符等问题
        String objectName = UUID.randomUUID() + "." + extension.toLowerCase();
        try (InputStream inputStream = file.getInputStream()) {
            String url = aliOssUtil.uploadFile(objectName, inputStream);
            return Result.success(url);
        }
    }
}
