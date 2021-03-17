package com.lvlivejp.bedakid.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lvlivejp.bedakid.domain.TeacherInfo;
import com.lvlivejp.bedakid.mapper.TeacherInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeacherService {

    @Autowired
    TeacherInfoMapper teacherInfoMapper;

    public TeacherInfo getTeacherInfo(String id){
        return teacherInfoMapper.selectOne(new QueryWrapper<TeacherInfo>().lambda().eq(TeacherInfo::getId,id));
    }

    public int insertTeacherInfo(TeacherInfo teacherInfo){
        return teacherInfoMapper.insert(teacherInfo);
    }

    public int updateTeacherInfo(TeacherInfo teacherInfo){
        return teacherInfoMapper.updateById(teacherInfo);
    }

}
