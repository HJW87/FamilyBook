package com.familyaccount;

import com.familyaccount.config.AiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AiProperties.class)
public class FamilyAccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(FamilyAccountApplication.class, args);
        System.out.println("========================================");
        System.out.println("  家庭记账本启动成功！");
        System.out.println("  主页: http://localhost:8080");
        System.out.println("  API文档: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}
