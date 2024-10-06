package com.zhixian.mall.third.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class S3Service {

    @Autowired
    private AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    public String uploadFile(MultipartFile file) {
        try {
            String dateFolder = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            // Construct the file name with the folder path
            String fileName = dateFolder + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            s3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);
            return s3Client.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public URL getFileUrl(String fileName) {
        return s3Client.getUrl(bucketName, fileName);
    }
}
