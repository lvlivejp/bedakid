package com.lvlivejp.bedakid.domain;


import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;


@Data
@TableName("t_teacher")
public class TeacherInfo {
    private String id;
    private String name;
    private String pic;
    private Integer totalComment;
    private Integer startCount;
    @JSONField(serialize = false,deserialize = false)
    private Date updateTime;
    private Double score;
    private Integer age;
    private Date lastCommetTime;
    @TableField(exist=false)
    private boolean flag;
    @TableField(exist=false)
    private String msg;
}
