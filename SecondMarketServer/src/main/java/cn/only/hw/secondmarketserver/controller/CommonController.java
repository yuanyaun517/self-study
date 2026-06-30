package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 通用控制器
 * 处理文件上传、下载等通用功能
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${common.path:../school-market-master/img/}")
    private String basePath;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("file must not be empty");
        }

        String suffix = "";
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + suffix;
        Path storageDir = resolveStorageDir();
        Path targetFile = storageDir.resolve(fileName).normalize();

        try {
            Files.createDirectories(storageDir);
            file.transferTo(targetFile.toFile());
            log.info("upload file success, path={}", targetFile);
            return Result.success(fileName);
        } catch (IOException | IllegalStateException e) {
            log.error("upload file failed, target={}", targetFile, e);
            return Result.error("file upload failed");
        }
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        Path filePath = resolveExistingFile(name);
        if (filePath == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            log.warn("download file not found, name={}, configuredPath={}, userDir={}",
                    name, basePath, System.getProperty("user.dir"));
            return;
        }

        response.setContentType(resolveContentType(filePath));
        try (FileInputStream fileInputStream = new FileInputStream(filePath.toFile());
             ServletOutputStream outputStream = response.getOutputStream()) {
            byte[] bytes = new byte[1024];
            int len;
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
            outputStream.flush();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("download file failed, name={}, path={}", name, filePath, e);
        }
    }

    private Path resolveStorageDir() {
        for (Path candidate : getCandidateStorageDirs()) {
            if (Files.exists(candidate) && Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        return resolveConfiguredStorageDir();
    }

    private Path resolveExistingFile(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return null;
        }

        String safeFileName = fileName.trim();
        if (safeFileName.contains("..") || safeFileName.contains("/") || safeFileName.contains("\\")) {
            return null;
        }

        for (Path storageDir : getCandidateStorageDirs()) {
            Path filePath = storageDir.resolve(safeFileName).normalize();
            if (filePath.startsWith(storageDir) && Files.isRegularFile(filePath)) {
                return filePath;
            }
        }
        return null;
    }

    private List<Path> getCandidateStorageDirs() {
        Path userDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Set<Path> candidates = new LinkedHashSet<Path>();
        candidates.add(resolveConfiguredStorageDir());
        candidates.add(userDir.resolve("img").normalize());
        candidates.add(userDir.resolve("school-market-master").resolve("img").normalize());
        candidates.add(userDir.resolve("..").resolve("school-market-master").resolve("img").normalize());
        return new ArrayList<Path>(candidates);
    }

    private Path resolveConfiguredStorageDir() {
        Path userDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        if (!StringUtils.hasText(basePath)) {
            return userDir.resolve("img").normalize();
        }

        try {
            Path configuredPath = Paths.get(basePath.trim());
            if (configuredPath.isAbsolute()) {
                return configuredPath.normalize();
            }
            return userDir.resolve(configuredPath).normalize();
        } catch (InvalidPathException e) {
            log.warn("invalid common.path={}, fallback to default storage dir", basePath, e);
            return userDir.resolve("img").normalize();
        }
    }

    private String resolveContentType(Path filePath) {
        try {
            String contentType = Files.probeContentType(filePath);
            if (StringUtils.hasText(contentType)) {
                return contentType;
            }
        } catch (IOException e) {
            log.debug("probe content type failed, path={}", filePath, e);
        }

        String lowerName = filePath.getFileName().toString().toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".png")) {
            return MediaType.IMAGE_PNG_VALUE;
        }
        if (lowerName.endsWith(".gif")) {
            return MediaType.IMAGE_GIF_VALUE;
        }
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG_VALUE;
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
