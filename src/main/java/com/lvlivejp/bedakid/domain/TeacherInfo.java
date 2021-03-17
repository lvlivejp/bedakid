package com.lvlivejp.bedakid.domain;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;


@Data
@TableName("t_teacher")
public class TeacherInfo {
    private String id;
    private String name;
    private Integer totalComment;
    private Integer startCount;
    private Date updateTime;

}
