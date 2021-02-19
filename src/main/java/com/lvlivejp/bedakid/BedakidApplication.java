package com.lvlivejp.bedakid;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lvlivejp.bedakid.utils.HttpClientResult;
import com.lvlivejp.bedakid.utils.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootApplication
@Slf4j
public class BedakidApplication  implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${mobile}")
    private String moblie;
    @Value("${password}")
    private String password;
    @Value("${teachers}")
    private String teachers;
    @Value("${times}")
    private String times;
    @Value("${avgScore}")
    private Double avgScore;
    @Value("${weekDay}")
    private String weekDay;
    @Value("${sex}")
    private String sex;
    @Value("${ageStart}")
    private String ageStart;
    @Value("${ageEnd}")
    private String ageEnd;
    @Value("${isOrder}")
    private boolean isOrder;
    @Value("${followCount}")
    private Integer followCount;
    public static void main(String[] args) throws IOException {
        SpringApplication.run(BedakidApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        System.out.println("你的手机号码：" + moblie);
        System.out.println("你的密码：" + password);
        System.out.println("你选择的老师：" + teachers);
        System.out.println("你选择的星期：" + weekDay);
        System.out.println("你选择的时间：" + times);
        System.out.println("你选择的性别：" + getSex(sex));
        System.out.println("你选择的年龄：" + ageStart + "~" + ageEnd);
        System.out.println("你选择的最低评价分数：" + avgScore);
        System.out.println("你选择的最低关注数：" + followCount);
        System.out.println("是否预约：" + isOrder);
        System.out.println("正确请输入y");
        Scanner scanner = new Scanner(System.in);
        scanner.hasNext();
        String next = scanner.next();
        if("y".equals(next)){
            Map map = new HashMap();
            map.put("code","071Pnx0w322UOV2BHo0w3B51Si4Pnx0k");
            map.put("input",moblie);
            map.put("password",password);
            try {
                String token = null;
                HttpClientResult httpClientResult = HttpClientUtils.doPost("https://service.bedakid.com/visitor/wxminiapp_bind", map, null);
                if(httpClientResult.getCode()!=200) {
                    return;
                }

                JSONObject jsonObject = JSONObject.parseObject(httpClientResult.getContent());
                String code = jsonObject.getString("code");
                if(!"0".equals(code)){
                    System.out.println("登录出错：" + jsonObject.getString("msg"));
                    return;
                }

                token = jsonObject.getJSONObject("data").getString("token");
                Map<String, String> headMap = new HashMap<>();
                headMap.put("Authorization",token);

                Integer pageNum=1;
                String teacherId="";
                String teacherName="";
                Double teacherScore=0D;

                Set teacherSet = new HashSet(Arrays.asList(teachers.split(",")));
                Set weekDaySet = new HashSet();
                if(StringUtils.hasText(weekDay)){
                    weekDaySet = new HashSet(Arrays.asList(weekDay.split(",")));
                }
                Set timesSet = new HashSet();
                if(StringUtils.hasText(times)){
                    timesSet = new HashSet(Arrays.asList(times.split(",")));
                }

                boolean keepSelect = true;
                while(keepSelect){
                    boolean selected = false;
                    while(!selected){
                        log.info("查询老师列表时间：" + DateFormatUtils.format(new Date(),"yyyy/MM/dd HH:mm:ss"));
                        pageNum=1;
                        teacherId="";
                        teacherName="";
                        teacherScore=0D;
                        while(true){
                            map = new HashMap();
                            map.put("spare","1");
                            map.put("pageNum",pageNum.toString());
                            map.put("book_series_id","1");
                            map.put("week_tmp","0");
                            if(StringUtils.hasText(weekDay)) {
                                map.put("week_day", weekDay);
                            }
                            if(StringUtils.hasText(sex)) {
                                map.put("sex", sex);
                            }
                            if(StringUtils.hasText(ageStart)) {
                                map.put("age_start", ageStart);
                            }
                            if(StringUtils.hasText(ageEnd)) {
                                map.put("age_end", ageEnd);
                            }
                            if(StringUtils.hasText(times)) {
                                map.put("class_time", times);
                            }

                            httpClientResult = HttpClientUtils.doGet("https://service.bedakid.com/api/student/datebook/searchtutor/week", headMap, map, null);
                            if(httpClientResult.getCode()!=200) {
                                return;
                            }
                            jsonObject = JSONObject.parseObject(httpClientResult.getContent());
                            code = jsonObject.getString("code");
                            if(!"0".equals(code)){
                                log.info("获取老师列表出错：" + jsonObject.getString("msg"));
                                return;
                            }

                            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("tutors");

                            if(jsonArray.size()==0){
                                break;
                            }
                            for (Object o : jsonArray) {
                                JSONObject teacherJson = (JSONObject) o;
                                log.info("老师：" + teacherJson.getString("name") + "，评分：" + teacherJson.getDouble("avg_score"));
                                if(StringUtils.hasText(teachers)){
                                    if(teacherSet.contains(teacherJson.get("name"))){
                                        teacherId = teacherJson.getString("id");
                                        teacherName = teacherJson.getString("name");
                                    }else{
                                        continue;
                                    }
                                }else{
                                    if(teacherJson.getDouble("avg_score")<avgScore || teacherJson.getDouble("avg_score")==5.0D){
                                        continue;
                                    }else{
                                        if(teacherJson.getDouble("avg_score") > teacherScore){
                                            //获取关注数
                                            map = new HashMap();
                                            map.put("id",teacherJson.getString("id"));
                                            httpClientResult = HttpClientUtils.doGet("https://service.bedakid.com/api/student/tutor/detail", headMap, map, null);
                                            if(httpClientResult.getCode()!=200) {
                                                return;
                                            }
                                            jsonObject = JSONObject.parseObject(httpClientResult.getContent());
                                            code = jsonObject.getString("code");
                                            if(!"0".equals(code)){
                                                log.info("获取老师关注数出错：" + jsonObject.getString("msg"));
                                                return;
                                            }
                                            jsonArray = jsonObject.getJSONObject("data").getJSONArray("tutors");
                                            if(jsonArray.size()==0){
                                                break;
                                            }
                                            Integer teacherfollowCount=((JSONObject)jsonArray.get(0)).getInteger("follower_count");
                                            log.info("#####################################老师：" + teacherJson.getString("name") + "，关注数：" + teacherfollowCount);
                                            if(teacherfollowCount>=followCount){
                                                teacherScore = teacherJson.getDouble("avg_score");
                                                teacherId = teacherJson.getString("id");
                                                teacherName = teacherJson.getString("name");
                                            }
                                        }
                                    }
                                }

                            }
                            pageNum++;
                        }
                        if(StringUtils.hasText(teacherId)){
                            selected=true;
                        }
                        if(!selected){
                            log.info("无合适老师，等待下一次查询");
                            Thread.sleep(6100);
                        }
                    }
                    log.info("######################################################选中的老师：" + teacherName + "，评分：" + teacherScore);

                    map = new HashMap();
                    map.put("tutor_id",teacherId);
                    map.put("week_tmp","0");
                    httpClientResult = HttpClientUtils.doGet("https://service.bedakid.com/api/student/datebook/week/tutortimes", headMap, map, null);
                    if(httpClientResult.getCode()!=200) {
                        return;
                    }
                    jsonObject = JSONObject.parseObject(httpClientResult.getContent());
                    code = jsonObject.getString("code");
                    if (!"0".equals(code)) {
                        log.info("获取老师授课时间出错：" + jsonObject.getString("msg"));
                        return;
                    }
                    List<String> canSelectList = new ArrayList<>();
                    JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("times");
                    String canSelectTimes="";
                    for (Object o : jsonArray) {
                        JSONObject timeJson = (JSONObject) o;
                        if(timeJson.getInteger("tick") == 1){
                            log.info("合适的时间段：" + timeJson.getString("cd") + timeJson.getString("ct"));
                            if(weekDaySet.size() > 0){
                                if(weekDaySet.contains(getWeek(timeJson.getDate("cd")))){
                                    if(timesSet.size()>0 ){
                                        if(timesSet.contains(timeJson.getString("ct"))){
                                            canSelectTimes+= timeJson.getString("cd")+getWeekStr(timeJson.getDate("cd")) + " " +timeJson.getString("ct") + ";";
                                            canSelectList.add(timeJson.getString("cd") + " " +timeJson.getString("ct"));
                                        }
                                    }else{
                                        canSelectTimes+= timeJson.getString("cd")+getWeekStr(timeJson.getDate("cd")) + " " +timeJson.getString("ct") + ";";
                                        canSelectList.add(timeJson.getString("cd") + " " +timeJson.getString("ct"));
                                    }
                                }
                            }else{
                                if(timesSet.size()>0 ){
                                    if(timesSet.contains(timeJson.getString("ct"))){
                                        canSelectTimes+= timeJson.getString("cd")+getWeekStr(timeJson.getDate("cd")) + " " +timeJson.getString("ct") + ";";
                                        canSelectList.add(timeJson.getString("cd") + " " +timeJson.getString("ct"));
                                    }
                                }else{
                                    canSelectTimes+= timeJson.getString("cd")+getWeekStr(timeJson.getDate("cd")) + " " +timeJson.getString("ct") + ";";
                                    canSelectList.add(timeJson.getString("cd") + " " +timeJson.getString("ct"));
                                }
                            }
                        }
                    }
                    log.info("合适的时间段：" + canSelectTimes + "，默认获取第一个时间段");
                    if(isOrder){
                        map = new HashMap();
                        map.put("bs_id","1");
                        map.put("tutor_id",teacherId);
                        map.put("dates",canSelectList.get(0));
                        httpClientResult = HttpClientUtils.doPost("https://service.bedakid.com/api/student/datebook/week/submitTimes", headMap, map, null);
                        System.out.println("预约课程报文：" + JSONObject.toJSONString(httpClientResult));
                        if(httpClientResult.getCode()!=200) {
                            return;
                        }
                        jsonObject = JSONObject.parseObject(httpClientResult.getContent());
                        code = jsonObject.getString("code");
                        if (!"0".equals(code)) {
                            log.info("预约课程出错：" + jsonObject.getString("msg"));
                        }else{
                            log.info("预约课程成功，恭喜你！！！下节课时间为：" + canSelectList.get(0));
                        }
                    }
                    Thread.sleep(6100);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private String getSex(String sex) {
        if("0".equals(sex)){
            return "女";
        }else if("1".equals(sex)){
            return "男";
        }
        return "";
    }

    //根据日期取得星期几
    public static String getWeekStr(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        String week = sdf.format(date);
        return week;
    }

    //根据日期取得星期几
    public static String getWeek(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if(week_index == 0){
            week_index=7;
        }
        return String.valueOf(week_index);
    }
}
