package com.familyaccount.controller;

import com.familyaccount.common.Result;
import com.familyaccount.mapper.CategoryMapper;
import com.familyaccount.mapper.FamilyMemberMapper;
import com.familyaccount.mapper.RecordMapper;
import com.familyaccount.service.DataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Tag(name = "数据管理", description = "数据导出/导入/清空")
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {

    private final DataService dataService;
    private final CategoryMapper categoryMapper;
    private final RecordMapper recordMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final ObjectMapper objectMapper;

    @Operation(summary = "导出全部数据为JSON文件")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportData() throws Exception {
        Map<String, Object> data = dataService.exportData();
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        String filename = "家庭记账本_备份_" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".json";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + filename)
                .contentType(MediaType.APPLICATION_JSON)
                .body(bytes);
    }

    @Operation(summary = "导入JSON数据文件")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, Object>> importData(
            @RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return Result.badRequest("请选择要导入的JSON文件");
        }
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = objectMapper.readValue(content, Map.class);
        Map<String, Object> result = dataService.importData(data);
        return Result.success("导入成功", result);
    }

    @Operation(summary = "清空所有数据")
    @DeleteMapping("/clear")
    public Result<Void> clearAll() {
        recordMapper.delete(null);  // 先删记录（有外键）
        categoryMapper.delete(null);
        familyMemberMapper.delete(null);
        log.warn("所有数据已被清空！");
        return Result.success();
    }
}
