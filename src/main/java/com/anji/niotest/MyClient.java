package com.anji.niotest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Description:
 * author: chenqiang
 * date: 2018/6/21 10:16
 */
public class MyClient {
    public static void main(String[] args) throws IOException {
        Socket client=null;
        PrintWriter printWriter=null;
        BufferedReader bufferedReader=null;
        try {
            client=new Socket();
            client.connect(new InetSocketAddress("localhost",8686));
            printWriter=new PrintWriter(client.getOutputStream(),true);
            printWriter.println("hello");
            printWriter.flush();

            bufferedReader=new BufferedReader(new InputStreamReader(client.getInputStream()));
            System.out.println("来自服务器的消息是: "+ bufferedReader.readLine());
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            printWriter.close();
            bufferedReader.close();
            client.close();
        }
    }
}
