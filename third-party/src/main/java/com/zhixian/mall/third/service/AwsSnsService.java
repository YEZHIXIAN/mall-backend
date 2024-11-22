package com.zhixian.mall.third.service;

import com.zhixian.mall.third.config.AwsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Service
public class AwsSnsService implements SmsService {

    private final SnsClient snsClient;
    private final String defaultPhoneNumber;

    @Autowired
    public AwsSnsService(AwsProperties awsProperties) {

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                awsProperties.getCredentials().getAccessKey(),
                awsProperties.getCredentials().getSecretKey()
        );

        this.snsClient = SnsClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .region(Region.of(awsProperties.getRegion()))
                .build();

        this.defaultPhoneNumber = awsProperties.getSns().getDefaultPhoneNumber();
    }

    @Override
    public void sendSms(String toPhoneNumber, String message) {
        try {
            String targetPhoneNumber = toPhoneNumber != null ? toPhoneNumber : defaultPhoneNumber;

            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .phoneNumber(targetPhoneNumber)
                    .build();

            PublishResponse response = snsClient.publish(request);
            System.out.println("Message sent successfully. Message ID: " + response.messageId());
        } catch (Exception e) {
            System.err.println("Error sending SMS: " + e.getMessage());
        }
    }
}
