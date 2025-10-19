package com.ai.imageagent.mamager;

import com.ai.imageagent.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.net.URL;
import java.util.Date;

/**
 * COS对象存储管理器
 *
 * @author chenqj
 */
@Component
@Slf4j
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return 上传结果
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传文件到 COS 并返回访问 URL
     *
     * @param key  COS对象键（完整路径）
     * @param file 要上传的文件
     * @return 文件的访问URL，失败返回null
     */
    public String uploadFile(String key, File file) {
        // 上传文件
        PutObjectResult result = putObject(key, file);
        if (result != null) {
            // 构建访问URL
            String url = getFileUrl(key, 60 * 60 * 24 * 7);
            log.info("文件上传COS成功: {} -> {}", file.getName(), url);
            return url;
        } else {
            log.error("文件上传COS失败，返回结果为空");
            return null;
        }
    }

    /**
     * 获取文件的可访问 URL（根据配置决定是否使用签名）
     *
     * @param key COS对象键（完整路径，例如 "images/test.jpg"）
     * @param expireSeconds URL有效期（秒，仅签名URL有效）
     * @return 可访问的URL
     */
    public String getFileUrl(String key, long expireSeconds) {
        if (cosClientConfig.isNeedSignature()) {
            return getSignedFileUrl(key, expireSeconds);
        } else {
            return getPublicFileUrl(key);
        }
    }

    /**
     * 获取文件的公开访问 URL（无需签名）
     *
     * @param key COS对象键（完整路径，例如 "images/test.jpg"）
     * @return 公开访问的URL
     */
    public String getPublicFileUrl(String key) {
        try {
            // 构建公开访问URL
            String url = String.format("https://%s.cos.%s.myqcloud.com/%s",
                    cosClientConfig.getBucket(),
                    cosClientConfig.getRegion(),
                    key);
            log.info("生成公开访问URL: {}", url);
            return url;
        } catch (Exception e) {
            log.error("生成公开访问URL失败，key={}", key, e);
            return null;
        }
    }

    /**
     * 获取文件的签名访问 URL（带签名，适用于私有读）
     *
     * @param key COS对象键（完整路径，例如 "images/test.jpg"）
     * @param expireSeconds URL有效期（秒）
     * @return 可访问的签名 URL
     */
    public String getSignedFileUrl(String key, long expireSeconds) {
        try {
            // 设置过期时间
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000);

            // 生成带签名的URL
            URL url = cosClient.generatePresignedUrl(
                    cosClientConfig.getBucket(),
                    key,
                    expiration
            );
            log.info("生成签名访问URL: {}", url.toString());
            return url.toString();
        } catch (Exception e) {
            log.error("生成签名访问URL失败，key={}", key, e);
            return null;
        }
    }

}

