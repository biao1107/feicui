package com.gaocui.common.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.gaocui.common.api.ResultCode;
import com.gaocui.common.config.properties.OssProperties;
import com.gaocui.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * OSS 文件上传服务. 商品图片统一存到 feicui/yyyy/MM/dd/<uuid>.ext.
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class OssService {

    /** 允许的图片类型 */
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");

    private final OSS oss;
    private final OssProperties props;

    /** 上传图片, 返回可访问 URL */
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAIL, "文件为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORT);
        }

        // 按日期分目录, 文件名用 UUID 防冲突
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String ext = extractExtension(file.getOriginalFilename(), contentType);
        String objectKey = "feicui/" + datePath + "/" + UUID.randomUUID() + ext;

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        meta.setContentType(contentType);

        try (InputStream is = file.getInputStream()) {
            oss.putObject(props.getBucketName(), objectKey, is, meta);
        } catch (Exception e) {
            log.error("[OSS] 上传失败", e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAIL);
        }
        return props.getDomain() + "/" + objectKey;
    }

    private String extractExtension(String filename, String contentType) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.'));
        }
        // 无扩展名时按 contentType 推断
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> "";
        };
    }
}
