package com.company;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();

        SocketChannel clientCh = SocketChannel.open();
        clientCh.configureBlocking(false);
        clientCh.register(selector, SelectionKey.OP_CONNECT);

        clientCh.connect(new InetSocketAddress("127.0.0.1", 9000));


        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                //check or do something it event
                if (key.isConnectable()) {
                    SocketChannel ch = (SocketChannel) key.channel();

                    if (ch.isConnectionPending()) {
                        ch.finishConnect();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        buffer.put(("Connected successfully").getBytes());
                        buffer.flip();
                        ch.write(buffer);

                        // Create a new thread to monitor input
                        ExecutorService executorService = Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
                        executorService.submit(() -> {
                            while (true) {
                                buffer.clear();
                                Scanner scanner = new Scanner(System.in);
                                System.out.print("");
                                String msg = scanner.nextLine();
                                buffer.put(msg.getBytes());
                                buffer.flip();
                                ch.write(buffer);
                            }
                        });
                    }
                    ch.register(selector, SelectionKey.OP_READ);
                }

                if (key.isReadable()) {
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    SocketChannel ch = (SocketChannel) key.channel();
                    int n = ch.read(buffer);
                    if (n == -1) {
                        ch.close();
                        continue;
                    }
                    buffer.flip();
                    String msg = new String(buffer.array());
                    System.out.println(msg);
                }
                it.remove();
            }
         }
    }
}
