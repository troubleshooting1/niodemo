package com.anji.niotest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Description:阻塞体现在这个线程不能干别的了，只能在这里等着。非阻塞体现在这个线程可以去干别的，不需要一直在这等着。
 * 说NIO的非阻塞原理之前，我们需要先说一下传统的io。传统的IO是按字节传输的，即每次传输一个字节。
 * 为了提高数据传输效率，引进了带缓冲区得输入输出模式，这样每次就可以传输大量的字节数，
 * 但是，会导致在读(写)缓冲区没有满的情况下，程序会一直等待，直到满或者关闭流才能读取（写入）。
 * 这样导致程序阻塞，降低了程序的执行效率。
 * author: chenqiang
 * date: 2018/6/21 10:50
 */
public class MyNioServer {
    private Selector selector;      //创建一个选择器
    private final static int port=8686;
    private final static int BUF_SIZE=10240;

    private void initServer() throws IOException{
        this.selector=Selector.open();          //创建通道管理器对象selector

        //创建一个通道对象channel
        ServerSocketChannel channel=ServerSocketChannel.open();
        channel.configureBlocking(false);           //设置通道为非阻塞
        channel.socket().bind(new InetSocketAddress(port));     //将通道绑定在8686端口

        SelectionKey selectionKey=channel.register(selector,SelectionKey.OP_ACCEPT);

        while (true){                       //轮询
            selector.select();              //!!!!!!!!!!!!!!!这是一个阻塞方法，一直等待直到有数据可读，返回值是key的数量（可以有多个）
            Set keys=selector.selectedKeys();           //如果channel有数据了，将生成的key放入Keys集合
            Iterator iterator=keys.iterator();            //得到这个keys的迭代器
            while (iterator.hasNext()){
                SelectionKey key=(SelectionKey)iterator.next();
                iterator.remove();

                if(key.isAcceptable()){
                    doAccept(key);
                }else if(key.isReadable()){
                    doRead(key);
                }else if(key.isWritable() && key.isValid()){
                    doWrite(key);
                }else if(key.isConnectable()){
                    System.out.println("连接成功！");
                }
            }
        }
    }

    public void doAccept(SelectionKey key) throws IOException{
        ServerSocketChannel serverSocketChannel=(ServerSocketChannel)key.channel();
        System.out.println("ServerSocketChannel正在循环监听");
        SocketChannel clientChannel=serverSocketChannel.accept();       // 获得和客户端连接的通道

        clientChannel.configureBlocking(false);
        clientChannel.register(key.selector(),SelectionKey.OP_READ);
    }

    public void doRead(SelectionKey key) throws IOException{
        SocketChannel clientChannel=(SocketChannel)key.channel();
        ByteBuffer byteBuffer=ByteBuffer.allocate(BUF_SIZE);
        long bytesRead=clientChannel.read(byteBuffer);
        while (bytesRead>0){
            byteBuffer.flip();
            byte[] data=byteBuffer.array();
            String info=new String(data).trim();
            System.out.println("从客户端发送过来的消息是："+info);
            byteBuffer.clear();
            bytesRead=clientChannel.read(byteBuffer);           //在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限
        }
        if(bytesRead==1){
            clientChannel.close();
        }
    }

    public void doWrite(SelectionKey key) throws IOException{
        ByteBuffer byteBuffer=ByteBuffer.allocate(BUF_SIZE);
        byteBuffer.flip();
        SocketChannel clientChannel=(SocketChannel)key.channel();
        while (byteBuffer.hasRemaining()){
            clientChannel.write(byteBuffer);
        }
        byteBuffer.compact();
    }

    public static void main(String[] args) throws IOException{
        MyNioServer myNioServer=new MyNioServer();
        myNioServer.initServer();
    }
}
