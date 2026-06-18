package com.muGood.web.controller;

import com.muGood.common.api.Result;
import com.muGood.common.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class UploadController {
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    @Value("${fresh-rabbit.upload.dir:uploads}")
    private String uploadDir;

    @Value("${fresh-rabbit.upload.url-prefix:/uploads}")
    private String uploadUrlPrefix;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, Object>> upload(@RequestPart("file") MultipartFile file, HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            throw new BizException("UPLOAD_001", "请选择要上传的图片");
        }
        String extension = extension(file.getOriginalFilename());
        if (!IMAGE_EXTENSIONS.contains(extension)) {
            throw new BizException("UPLOAD_002", "仅支持 jpg、png、gif、webp、bmp 图片");
        }

        String datePath = LocalDate.now().toString();
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path targetDir = Path.of(uploadDir).toAbsolutePath().normalize().resolve(datePath);
        Path targetFile = targetDir.resolve(filename).normalize();

        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetFile);
        } catch (IOException exception) {
            throw new BizException("UPLOAD_003", "图片保存失败");
        }

        String path = uploadUrlPrefix + "/" + datePath + "/" + filename;
        String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path;
        return Result.ok(Map.of("url", url, "path", path, "filename", filename));
    }

    private String extension(String filename) {
        String cleaned = StringUtils.cleanPath(filename == null ? "" : filename);
        int dotIndex = cleaned.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == cleaned.length() - 1) {
            throw new BizException("UPLOAD_002", "无法识别图片格式");
        }
        return cleaned.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
