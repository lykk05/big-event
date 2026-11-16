package com.itheima.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.PutObjectRequest;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * OSS 客户端单例：避免每次上传都新建并关闭客户端。
 */
@Component
public class AliOssUtil {

    private static final String ENDPOINT = "https://oss-cn-beijing.aliyuncs.com";
    private static final String BUCKET_NAME = "big-big-boss-lan";
    private static final String REGION = "cn-beijing";

    private final OSS ossClient;

    public AliOssUtil() {
        OSS client;
        try {
            EnvironmentVariableCredentialsProvider credentialsProvider =
                    CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
            ClientBuilderConfiguration configuration = new ClientBuilderConfiguration();
            configuration.setSignatureVersion(SignVersion.V4);
            client = OSSClientBuilder.create()
                    .endpoint(ENDPOINT)
                    .credentialsProvider(credentialsProvider)
                    .clientConfiguration(configuration)
                    .region(REGION)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("初始化 OSS 客户端失败", e);
        }
        this.ossClient = client;
    }

    public String uploadFile(String objectName, InputStream inputStream) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, objectName, inputStream);
        ossClient.putObject(putObjectRequest);
        return "https://" + BUCKET_NAME + "." + ENDPOINT.substring(ENDPOINT.lastIndexOf("/") + 1) + "/" + objectName;
    }

    @PreDestroy
    public void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
