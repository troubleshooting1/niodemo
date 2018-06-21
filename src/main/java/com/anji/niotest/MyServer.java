package com.anji.niotest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Description:
 * author: chenqiang
 * date: 2018/6/21 10:06
 */
public class MyServer {
    private static ExecutorService executorService = Executors.newCachedThreadPool();        //创建一个线程池

    private static class HandleMsg implements Runnable {
        Socket client;

        public HandleMsg(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            BufferedReader bufferedReader = null;         //创建字符缓存输入流
            PrintWriter printWriter = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                printWriter = new PrintWriter(client.getOutputStream(), true);            //获取客户端的输出流,true为随时刷新
                String inputLine = null;
                long a = System.currentTimeMillis();
                while ((inputLine = bufferedReader.readLine()) != null) {
                    printWriter.println(inputLine);
                }
                Thread.sleep(5000);
                long b = System.currentTimeMillis();
                System.out.println("此线程花了：" + (b - a) + "秒！");

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    bufferedReader.close();
                    printWriter.close();
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static void main(String[] args) throws IOException{          //服务端的主线程是用来循环监听客户端请求
        ServerSocket server=new ServerSocket(8686);         //创建一个服务端且端口为8686
        Socket client=null;

        while (true){                       //循环监听
            client=server.accept();            //服务端监听到一个客户端请求
            System.out.println(client.getRemoteSocketAddress()+"地址的客户端连接成功");
            executorService.submit(new HandleMsg(client));          //将客户端请求通过线程池放入HandlMsg线程中进行处理
        }
    }
}
