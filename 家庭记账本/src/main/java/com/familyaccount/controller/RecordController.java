package com.familyaccount.controller;

import com.familyaccount.common.PageResult;
import com.familyaccount.common.Result;
import com.familyaccount.dto.RecordQueryDTO;
import com.familyaccount.entity.Record;
import com.familyaccount.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "记录管理", description = "收支记录的增删改查")
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @Operation(summary = "分页查询记录")
    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(RecordQueryDTO query) {
        return Result.success(recordService.listRecords(query));
    }

    @Operation(summary = "获取单条记录")
    @GetMapping("/{id}")
    public Result<Record> getById(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return Result.success(recordService.getById(id));
    }

    @Operation(summary = "添加记录")
    @PostMapping
    public Result<Record> add(@RequestBody Record record) {
        return Result.success(recordService.addRecord(record));
    }

    @Operation(summary = "修改记录")
    @PutMapping("/{id}")
    public Result<Record> update(
            @Parameter(description = "记录ID") @PathVariable Long id,
            @RequestBody Record record) {
        return Result.success(recordService.updateRecord(id, record));
    }

    @Operation(summary = "删除记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        recordService.deleteRecord(id);
        return Result.success();
    }
}
