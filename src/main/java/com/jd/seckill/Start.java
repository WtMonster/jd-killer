package com.jd.seckill;

import com.alibaba.fastjson.JSONObject;
import com.sun.webkit.network.CookieManager;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Start {

    final static String headerAgent = "User-Agent";
    final static String headerAgentArg = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.135 Safari/537.36";
    final static String Referer = "Referer";
    final static String RefererArg = "https://passport.jd.com/new/login.aspx";
    static String pid;
    static String  beginTime;
    static Integer preMillis;
    public static  Long realTime;
    volatile static Integer ok;
    static String eid = "X";
    static String fp = "X";
    static JSONObject headers;
    static{
        //输入商品编号
        System.out.print("商品编号：");
        pid = new Scanner(System.in).nextLine();
        System.out.print("提前毫秒数");
        preMillis = new Scanner(System.in).nextInt();
        System.out.print("开始时间：");
        beginTime = new Scanner(System.in).nextLine();
        try {
            realTime = HttpUrlConnectionUtil.dateToTime(beginTime)-preMillis;
        } catch (ParseException e) {
            e.printStackTrace();
        }finally {
            System.out.println(realTime+"开始");
        }
        //输入抢购次数
        System.out.println("抢购次数:");
        ok = new Scanner(System.in).nextInt();
        headers = new JSONObject();
        headers.put(Start.headerAgent, Start.headerAgentArg);
        headers.put(Start.Referer, Start.RefererArg);
    }

    static CookieManager manager = new CookieManager();

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException, ParseException {
        CookieHandler.setDefault(manager);
        Login.login();

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 1000, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        new RushToPurchase().run();

    }

    public static void judgePruchase() throws IOException, ParseException, InterruptedException {
        //拼接商品请求
        String str = HttpUrlConnectionUtil.get(headers, "https://item-soa.jd.com/getWareBusiness?skuId=" + pid);
        JSONObject shopDetail = JSONObject.parseObject(str);
        if (shopDetail.get("yuyueInfo") != null) {
            String buyDate = JSONObject.parseObject(shopDetail.get("yuyueInfo").toString()).get("buyTime").toString();
            String startDate = buyDate.split("-202")[0] + ":00";
            System.out.println("抢购时间为：" + startDate);
            Long startTime = HttpUrlConnectionUtil.dateToTime(startDate);
            while (true) {
                JSONObject jdTime = JSONObject.parseObject(HttpUrlConnectionUtil.get(headers, "https://api.m.jd.com/client.action?functionId=queryMaterialProducts&client=wh5"));
                Long serverTime = Long.valueOf(jdTime.get("currentTime2").toString());
                if (startTime >= serverTime) {
                    System.out.println("正在等待抢购时间");
                    Thread.sleep(300);
                } else {
                    break;
                }
            }
        }
    }

}





