package com.zhixian.mall.third.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cloud.aws")
@Data
public class AwsProperties {

    private Credentials credentials;
    private String region;
    private Sns sns;

    @Data
    public static class Credentials {
        private String accessKey;
        private String secretKey;
    }

    @Data
    public static class Sns {
        private String defaultPhoneNumber;
    }


}
