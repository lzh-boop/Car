package com.example.car.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * 用于自动填充创建时间和更新时间
 * 
 * 规则：
 * - 新增时：同时填充 createTime 和 updateTime（都显示当前时间）
 * - 更新时：只填充 updateTime，不修改 createTime（保持原有值）
 */
@Slf4j
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {
    
    /**
     * 插入时自动填充
     * 同时填充 createTime 和 updateTime
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");
        LocalDateTime now = LocalDateTime.now();
        
        // 填充创建时间（如果字段存在且值为null）
        if (metaObject.hasGetter("createTime")) {
            Object createTime = metaObject.getValue("createTime");
            if (createTime == null) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
                log.debug("填充创建时间: {}", now);
            }
        }
        
        // 填充更新时间（如果字段存在且值为null）
        if (metaObject.hasGetter("updateTime")) {
            Object updateTime = metaObject.getValue("updateTime");
            if (updateTime == null) {
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
                log.debug("填充更新时间: {}", now);
            }
        }
    }
    
    /**
     * 更新时自动填充
     * 只填充 updateTime，不修改 createTime
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");
        
        // 强制填充更新时间（无论原值是否为null）
        if (metaObject.hasGetter("updateTime")) {
            LocalDateTime now = LocalDateTime.now();
            this.setFieldValByName("updateTime", now, metaObject);
            log.debug("强制填充更新时间: {}", now);
        }
        
        // 注意：更新时不修改createTime，保持原有值
        // MyBatis-Plus的updateFill不会自动修改INSERT类型的字段
    }
}
