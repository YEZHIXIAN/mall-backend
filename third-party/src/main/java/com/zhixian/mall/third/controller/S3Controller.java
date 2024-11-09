package com.zhixian.mall.third.controller;

import com.zhixian.mall.third.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/third-party/s3")
public class S3Controller {

    @Autowired
    private S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileUrl = s3Service.uploadFile(file);
        return ResponseEntity.ok(fileUrl);
    }

    @GetMapping("/file-url")
    public ResponseEntity<String> getFileUrl(@RequestParam String fileName) {
        return ResponseEntity.ok(s3Service.getFileUrl(fileName).toString());
    }
}
