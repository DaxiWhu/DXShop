package com.daxi.util;

import cn.hutool.crypto.SecureUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.daxi.domain.dto.OssPolicyDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.TimeZone;

@Component
@RequiredArgsConstructor
public class OssUtil {

    private @NonNull final OSS ossClient;

    private @NonNull final OssProperties ossProperties;

    // ====================== 1. 前端直传私有Bucket：获取STS临时凭证 ======================
    public OssPolicyDTO getOssPolicy(String dir) {
        if (dir == null || dir.isEmpty()) {
            throw new IllegalArgumentException("上传目录不能为空");
        }
        
        DefaultAcsClient client = null;
        try {
            DefaultProfile profile = DefaultProfile.getProfile(
                    ossProperties.getRegion(),
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret()
            );
            client = new DefaultAcsClient(profile);

            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setRoleArn(ossProperties.getRoleArn());
            request.setRoleSessionName("oss-upload-session");
            AssumeRoleResponse response = client.getAcsResponse(request);

            // 临时密钥
            String tempKeyId = response.getCredentials().getAccessKeyId();
            String tempSecret = response.getCredentials().getAccessKeySecret();
            String token = response.getCredentials().getSecurityToken();

            // 生成Policy
            long expireEnd = System.currentTimeMillis() + ossProperties.getExpireTime() * 1000;
            Date expiration = new Date(expireEnd);
            String policy = "{\n" +
                    "    \"expiration\": \"" + getIsoTime(expiration) + "\",\n" +
                    "    \"conditions\": [\n" +
                    "        [\"content-length-range\", 0, " + ossProperties.getMaxSize() + "],\n" +
                    "        [\"starts-with\", \"$key\", \"" + dir + "\"]\n" +
                    "    ]\n" +
                    "}";

            String policyBase64 = BinaryUtil.toBase64String(policy.getBytes(StandardCharsets.UTF_8));
            // 使用AccessKeySecret进行签名，而不是临时密钥
            byte[] signBytes = SecureUtil.hmacSha1(ossProperties.getAccessKeySecret())
                    .digest(policyBase64.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getEncoder().encodeToString(signBytes);

            return new OssPolicyDTO(
                    tempKeyId, policyBase64, signature,
                    "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint(),
                    dir, ossProperties.getExpireTime(), token
            );
        } catch (Exception e) {
            throw new RuntimeException("获取上传凭证失败", e);
        } finally {
            if (client != null) {
                try {
                    client.shutdown();
                } catch (Exception e) {
                    // 忽略关闭异常
                }
            }
        }
    }

    // ====================== 2. 核心！私有Bucket 生成临时访问URL（安全无盗链） ======================
    /**
     * 根据OSS文件key 生成临时URL
     * @param key 例如 temp/comment/xxx.png
     * @return 30分钟有效临时URL
     */
    public String getTempAccessUrl(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("文件key不能为空");
        }
        
        try {
            // 过期时间：30分钟
            Date expire = new Date(System.currentTimeMillis() + (long) ossProperties.getTempUrlExpire() * 60 * 1000);
            // 生成私有文件临时URL
            URL url = ossClient.generatePresignedUrl(
                    ossProperties.getBucketName(),
                    key,
                    expire
            );
            return url.toString();
        } catch (Exception e) {
            throw new RuntimeException("生成临时URL失败", e);
        }
    }

    /**
     * 批量生成临时访问URL
     * @param keys OSS文件key列表，例如 ["temp/comment/xxx.png", "temp/comment/yyy.jpg"]
     * @return 临时URL列表，30分钟有效，顺序与输入keys一致
     */
    public List<String> getTempAccessUrls(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> result = new ArrayList<>();
        try {
            // 过期时间：30分钟
            Date expire = new Date(System.currentTimeMillis() + (long) ossProperties.getTempUrlExpire() * 60 * 1000);
            
            for (String key : keys) {
                if (key != null && !key.isEmpty()) {
                    try {
                        URL url = ossClient.generatePresignedUrl(
                                ossProperties.getBucketName(),
                                key,
                                expire
                        );
                        result.add(url.toString());
                    } catch (Exception e) {
                        // 单个key失败不影响其他key，记录日志后继续处理
                        System.err.println("生成临时URL失败，key: " + key + ", 错误: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("批量生成临时URL失败", e);
        }
        
        return result;
    }

    // ====================== 3. 临时目录复制到正式目录（业务提交成功用） ======================
    public String copyTempToFormal(String tempKey, String formalDir) {
        if (tempKey == null || tempKey.isEmpty()) {
            throw new IllegalArgumentException("临时文件key不能为空");
        }
        if (formalDir == null || formalDir.isEmpty()) {
            throw new IllegalArgumentException("正式目录不能为空");
        }
        
        try {
            String fileName = tempKey.substring(tempKey.lastIndexOf("/") + 1);
            String formalKey = formalDir + fileName;
            ossClient.copyObject(
                    ossProperties.getBucketName(), tempKey,
                    ossProperties.getBucketName(), formalKey
            );
            return formalKey;
        } catch (Exception e) {
            throw new RuntimeException("图片转正失败", e);
        }
    }

    // 时间格式化
    private String getIsoTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }
}
