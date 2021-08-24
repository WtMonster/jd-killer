package com.jd.seckill;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RushToPurchase implements Runnable {

    volatile static Integer times = 0;
    static Map<String, List<String>> stringListMap = new HashMap<String, List<String>>();


    public void run() {

            String gate = null;
            try {
                gate = HttpUrlConnectionUtil.get(Start.headers, "https://cart.jd.com/gate.action?pcount="+Start.count+"&ptype=1&pid="+Start.pid);
            } catch (IOException e) {
                e.printStackTrace();
            }
            stringListMap.clear();
            try {
                stringListMap = Start.manager.get(new URI("https://trade.jd.com/shopping/order/getOrderInfo.action"), stringListMap);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            List<String> cookie = stringListMap.get("Cookie");
            Start.headers.put("Cookie", cookie.get(0).toString());
            try {
                String orderInfo = HttpUrlConnectionUtil.get(Start.headers, "https://trade.jd.com/shopping/order/getOrderInfo.action");
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject subData = new JSONObject();
            Start.headers = new JSONObject();
            subData.put("overseaPurchaseCookies", "");
            subData.put("vendorRemarks", "[]");
            subData.put("submitOrderParam.sopNotPutInvoice", "false");
            subData.put("submitOrderParam.ignorePriceChange", "1");
            subData.put("submitOrderParam.btSupport", "0");
            subData.put("submitOrderParam.isBestCoupon", "1");
            subData.put("submitOrderParam.jxj", "1");
            subData.put("submitOrderParam.trackID", Login.ticket);
            subData.put("submitOrderParam.eid", Start.eid);
            subData.put("submitOrderParam.fp", Start.fp);
            subData.put("submitOrderParam.needCheck", "1");
            Start.headers.put("Referer", "http://trade.jd.com/shopping/order/getOrderInfo.action");
            Start.headers.put("origin", "https://trade.jd.com");
            Start.headers.put("Content-Type", "application/json");
            Start.headers.put("x-requested-with", "XMLHttpRequest");
            Start.headers.put("upgrade-insecure-requests", "1");
            Start.headers.put("sec-fetch-user", "?1");
            stringListMap.clear();


            try {
                stringListMap = Start.manager.get(new URI("https://trade.jd.com/shopping/order/getOrderInfo.action"), stringListMap);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            cookie = stringListMap.get("Cookie");
            Start.headers.put("Cookie", cookie.get(0).toString());
            String submitOrder = null;

            try {
                while (times < Start.ok) {

                    while(true){
                        JSONObject jdTime = null;
                        try {
                            jdTime = JSONObject.parseObject(HttpUrlConnectionUtil.get(Start.headers, "https://api.m.jd.com/client.action?functionId=queryMaterialProducts&client=wh5"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Long serverTime = Long.valueOf(jdTime.get("currentTime2").toString());

                        if (Start.realTime - serverTime > 10000){
                            System.out.println("进入倒计时！"+(Start.realTime - serverTime- 5000)/1000+"秒后激活程序");
                            Thread.sleep(Start.realTime - serverTime- 5000);
                            System.out.println("程序激活，距离抢购开始还有"+(Start.realTime-(new Date().getTime()))+"ms");
                        }
                        if (Start.realTime <= serverTime) {
                            break;
                        }
                    }

                    submitOrder = HttpUrlConnectionUtil.post(Start.headers, "https://trade.jd.com/shopping/order/submitOrder.action", null);


                    System.out.println(new Date().getTime());

                    if (submitOrder.contains("刷新太频繁了") || submitOrder.contains("抱歉，您访问的内容不存在")) {
                        System.out.println("刷新太频繁了,您访问的内容不存在");
                        continue;
                    }
                    JSONObject jsonObject = JSONObject.parseObject(submitOrder);
                    String success = null;
                    String message = null;
                    if (jsonObject != null && jsonObject.get("success") != null) {
                        success = jsonObject.get("success").toString();
                    }
                    if (jsonObject != null && jsonObject.get("message") != null) {
                        message = jsonObject.get("message").toString();
                    }

                    if (success == "true") {
                        times++;
                    } else {
                        if (message != null) {
                            System.out.println(message);
                        } else if (submitOrder.contains("很遗憾没有抢到")) {
                            System.out.println("很遗憾没有抢到，再接再厉哦");
                        } else if (submitOrder.contains("抱歉，您提交过快，请稍后再提交订单！")) {
                            System.out.println("抱歉，您提交过快，请稍后再提交订单！");
                        } else if (submitOrder.contains("系统正在开小差，请重试~~")) {
                            System.out.println("系统正在开小差，请重试~~");
                        } else if (submitOrder.contains("您多次提交过快")) {
                            System.out.println("您多次提交过快，请稍后再试");
                        } else {
                            System.out.println("获取用户订单信息失败");
                        }
                    }
                }
                System.out.println("已抢购" + Start.ok + "件，请尽快完成付款");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    }


