package com.littlehow.tool.sentinel.test;

import com.littlehow.tool.sentinel.proxy.SentinelAopUtils;

public class Run {
    public static void main(String[] args) {
        //testFlow();
        testDegrade();
    }

    private static void testFlow() {
        TestService service = SentinelAopUtils.getProxyObject(TestService.class);
        for (int i = 0; i < 15; i++) {
            try {
                System.out.println(service.getUUID());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void testDegrade() {
        IService service = SentinelAopUtils.getProxyObject(TestService.class);
        for (int i = 0; i < 45; i ++) {
            try {
                System.out.println(service.getHello("littlehow", 18));
            } catch (IllegalArgumentException e) {
                System.out.println("error ------> " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}