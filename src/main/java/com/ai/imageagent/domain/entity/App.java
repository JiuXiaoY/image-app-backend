package com.ai.imageagent.domain.entity;

import com.ai.imageagent.common.ErrorCode;
import com.ai.imageagent.exception.BusinessException;
import com.mybatisflex.annotation.*;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用 实体类。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "app")
public class App implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    @Column("appName")
    private String appName;

    @Column("cover")
    private String cover;

    @Column("initPrompt")
    private String initPrompt;

    @Column("codeGenType")
    private String codeGenType;

    @Column("deployKey")
    private String deployKey;

    @Column("deployedTime")
    private LocalDateTime deployedTime;

    @Column("priority")
    private Integer priority;

    @Column("userId")
    private Long userId;

    @Column("editTime")
    private LocalDateTime editTime;

    @Column("createTime")
    private LocalDateTime createTime;

    @Column("updateTime")
    private LocalDateTime updateTime;

    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;
}


