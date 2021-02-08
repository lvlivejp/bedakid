package com.lvlivejp.bedakid;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lvlivejp.bedakid.utils.HttpClientResult;
import com.lvlivejp.bedakid.utils.HttpClientUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootApplication
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
    public static void main(String[] args) throws IOException {
        SpringApplication.run(BedakidApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        System.out.println("你的手机号码：" + moblie);
        System.out.println("你的密码：" + password);
        System.out.println("你选择的老师：" + teachers);
        System.out.println("你选择的时间：" + times);
        System.out.println("你选择的最低评价分数：" + avgScore);
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
                if(httpClientResult.getCode()==200){
                    JSONObject jsonObject = JSONObject.parseObject(httpClientResult.getContent());
                    String code = jsonObject.getString("code");
                    if(!"0".equals(code)){
                        System.out.println("登录出错：" + jsonObject.getString("msg"));
                        return;
                    }
                    token = jsonObject.getJSONObject("data").getString("token");
                }
                Map<String, String> headMap = new HashMap<>();
                headMap.put("Authorization",token);

                Integer pageNum=1;
                String teacherId="";
                String teacherName="";
                boolean selected = false;
                while(!selected){
                    System.out.println("查询老师列表时间：" + DateFormatUtils.format(new Date(),"yyyy/MM/dd HH:mm:ss"));
                    while(true){
                        map = new HashMap();
                        map.put("spare","1");
                        map.put("pageNum",pageNum.toString());
                        map.put("book_series_id","1");
                        map.put("week_tmp","0");
                        if(StringUtils.hasText(times)) {
                            map.put("class_time", times);
                        }

                        httpClientResult = HttpClientUtils.doGet("https://service.bedakid.com/api/student/datebook/searchtutor/week", headMap, map, null);
                        if(httpClientResult.getCode()==200) {
                            JSONObject jsonObject = JSONObject.parseObject(httpClientResult.getContent());
                            String code = jsonObject.getString("code");
                            if(!"0".equals(code)){
                                System.out.println("获取老师列表出错：" + jsonObject.getString("msg"));
                                return;
                            }
                            Set teacherSet = new HashSet(Arrays.asList(teachers.split(",")));
                            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("tutors");

                            if(jsonArray.size()==0){
                                break;
                            }
                            for (Object o : jsonArray) {
                                JSONObject teacherJson = (JSONObject) o;
                                if(StringUtils.hasText(teachers)){
                                    if(teacherSet.contains(teacherJson.get("name"))){
                                        teacherId = teacherJson.getString("id");
                                        teacherName = teacherJson.getString("name");
                                    }else{
                                        continue;
                                    }
                                }else{
                                    if(teacherJson.getDouble("avg_score")<avgScore){
                                        continue;
                                    }else{
                                        teacherId = teacherJson.getString("id");
                                        teacherName = teacherJson.getString("name");
                                    }
                                }

                            }
                            if(StringUtils.hasText(teacherId)){
                                selected=true;
                                break;
                            }else{
                                pageNum++;
                            }
                        }
                    }
                    if(!selected){
                        System.out.println("无合适老师，等待下一次查询");
                        Thread.sleep(6100);
                    }
                }
                System.out.println("选中的老师：" + teacherName);

                map = new HashMap();
                map.put("tutor_id",teacherId);
                map.put("week_tmp","0");
                httpClientResult = HttpClientUtils.doGet("https://service.bedakid.com/api/student/datebook/week/tutortimes", headMap, map, null);
                if(httpClientResult.getCode()==200) {
                    JSONObject jsonObject = JSONObject.parseObject(httpClientResult.getContent());
                    String code = jsonObject.getString("code");
                    if (!"0".equals(code)) {
                        System.out.println("获取老师授课时间出错：" + jsonObject.getString("msg"));
                        return;
                    }
                    JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("times");
                    String canSelectTimes="";
                    for (Object o : jsonArray) {
                        JSONObject timeJson = (JSONObject) o;
                        if(timeJson.getInteger("tick") == 1){
                            canSelectTimes+=getWeek(timeJson.getDate("cd")) + " " +timeJson.getString("ct") + ";";
                        }
                    }
                    System.out.println("合适的时间段：" + canSelectTimes);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    //根据日期取得星期几
    public static String getWeek(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        String week = sdf.format(date);
        return week;
    }
}
