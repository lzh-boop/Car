package com.example.car.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.car.common.result.Result;
import com.example.car.entity.SysDept;
import com.example.car.entity.dto.SysDeptQueryDTO;
import com.example.car.entity.vo.SysDeptVO;
import com.example.car.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "部门管理", description = "部门信息管理相关接口")
@RestController
@RequestMapping("/api/system/dept")
@RequiredArgsConstructor
public class DeptController {
    
    private final SysDeptService sysDeptService;
    
    @Operation(summary = "分页查询部门列表")
    @GetMapping("/list")
    public Result<Page<SysDeptVO>> pageQuery(SysDeptQueryDTO queryDTO) {
        Page<SysDeptVO> page = sysDeptService.pageQuery(queryDTO);
        return Result.success(page);
    }
    
    @Operation(summary = "查询部门详情")
    @GetMapping("/{id}")
    public Result<SysDeptVO> getDetail(@Parameter(description = "部门ID") @PathVariable Long id) {
        SysDeptVO vo = sysDeptService.getDetailById(id);
        return Result.success(vo);
    }
    
    @Operation(summary = "新增部门")
    @PostMapping
    public Result<Void> add(@Validated @RequestBody SysDept sysDept) {
        boolean success = sysDeptService.addDept(sysDept);
        return success ? Result.success() : Result.error("新增部门失败");
    }
    
    @Operation(summary = "更新部门")
    @PutMapping
    public Result<Void> update(@Validated @RequestBody SysDept sysDept) {
        boolean success = sysDeptService.updateDept(sysDept);
        return success ? Result.success() : Result.error("更新部门失败");
    }
    
    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "部门ID") @PathVariable Long id) {
        boolean success = sysDeptService.deleteDept(id);
        return success ? Result.success() : Result.error("删除部门失败");
    }
}
