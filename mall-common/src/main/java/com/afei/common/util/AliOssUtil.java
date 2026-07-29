package com.afei.common.util;

import com.afei.common.config.AliOssProperties;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class AliOssUtil {

    @Autowired
    private AliOssProperties ossProp;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");

    /**
     * 上传文件
     * @param file 前端传过来的文件
     * @return 文件完整访问地址
     */
    public String upload(MultipartFile file) {
        validateFile(file);

        OSS ossClient = new OSSClientBuilder().build(
                ossProp.getEndpoint(),
                ossProp.getAccessKeyId(),
                ossProp.getAccessKeySecret()
        );

        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
            String objectName = "avatar/" + fileName;

            try (InputStream inputStream = file.getInputStream()) {
                ossClient.putObject(ossProp.getBucketName(), objectName, inputStream);
            }

            return ossProp.getUrl() + "/" + objectName;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        } finally {
            ossClient.shutdown();
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(suffix)) {
            throw new IllegalArgumentException("不支持的文件类型，仅支持: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    /**
     * 删除文件
     */
    public void delete(String objectName) {
        OSS ossClient = new OSSClientBuilder().build(
                ossProp.getEndpoint(),
                ossProp.getAccessKeyId(),
                ossProp.getAccessKeySecret()
        );
        try {
            ossClient.deleteObject(ossProp.getBucketName(), objectName);
        } finally {
            ossClient.shutdown();
        }
    }
}