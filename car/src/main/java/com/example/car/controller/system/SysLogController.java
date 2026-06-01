package com.example.car.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.car.common.result.Result;
import com.example.car.entity.dto.SysLogQueryDTO;
import com.example.car.entity.vo.SysLogVO;
import com.example.car.service.SysLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "操作日志", description = "系统操作日志查询相关接口")
@RestController
@RequestMapping("/api/system/log")
@RequiredArgsConstructor
public class SysLogController {
    
    private final SysLogService sysLogService;
    
    @Operation(summary = "分页查询操作日志")
    @GetMapping("/list")
    public Result<Page<SysLogVO>> pageQuery(SysLogQueryDTO queryDTO) {
        Page<SysLogVO> page = sysLogService.pageQuery(queryDTO);
        return Result.success(page);
    }
    
    @Operation(summary = "查询日志详情")
    @GetMapping("/{id}")
    public Result<SysLogVO> getDetail(@Parameter(description = "日志ID") @PathVariable Long id) {
        SysLogVO vo = sysLogService.getDetailById(id);
        return Result.success(vo);
    }
}
