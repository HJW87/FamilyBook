package com.familyaccount.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 系统启动后的初始化检查
 * 预设数据（类别/成员）现在由 FamilyService.createFamily() 在创建家庭时复制，
 * 不再全局初始化。
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info("系统启动完成。预设类别和成员将在创建家庭时自动生成。");
    }
}
