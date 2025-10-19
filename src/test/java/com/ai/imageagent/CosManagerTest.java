package com.ai.imageagent;

import com.ai.imageagent.config.CosClientConfig;
import com.ai.imageagent.mamager.CosManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class CosManagerTest {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    @Test
    void testCosConfig() {
        System.out.println("COS配置信息:");
        System.out.println("Host: " + cosClientConfig.getHost());
        System.out.println("Region: " + cosClientConfig.getRegion());
        System.out.println("Bucket: " + cosClientConfig.getBucket());
        System.out.println("Need Signature: " + cosClientConfig.isNeedSignature());
    }

    @Test
    void testUrlGeneration() {
        String testKey = "test/images/sample.jpg";
        
        // 测试公开URL生成
        String publicUrl = cosManager.getPublicFileUrl(testKey);
        System.out.println("公开访问URL: " + publicUrl);
        
        // 测试根据配置的URL生成
        String configUrl = cosManager.getFileUrl(testKey, 3600);
        System.out.println("根据配置生成的URL: " + configUrl);
    }
}
