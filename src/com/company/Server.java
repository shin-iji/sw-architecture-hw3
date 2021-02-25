package com.company;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Server {

    //private static Map<String, SocketChannel> clients = new HashMap();
    private  static ArrayList<SocketChannel> clients = new ArrayList<>();

    //private static int CLIENT_ID = 0;

    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();

        ServerSocketChannel serverCh = ServerSocketChannel.open();
        serverCh.configureBlocking(false);
        serverCh.bind(new InetSocketAddress(9000));
        serverCh.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Listen for connection");
        while (true) {
            selector.select(); //watching event
            //System.out.println("Got some events");
            Set<SelectionKey> keys = selector.selectedKeys(); //list of events happen
            //do something with keys
            Iterator<SelectionKey> it = keys.iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();

                if (key.isAcceptable()) {
                    System.out.println("Got new client");
                    ServerSocketChannel ch = (ServerSocketChannel) key.channel();
                    SocketChannel clientCh = ch.accept();
                    clientCh.configureBlocking(false);
                    clientCh.register(selector, SelectionKey.OP_READ);
                    //String clientId = "Client[" + CLIENT_ID++ + "]";
                    //clients.put(clientId, clientCh);
                    clients.add(clientCh);
                }

                if (key.isReadable()) {
                   SocketChannel ch = (SocketChannel) key.channel();
                   ByteBuffer buffer = ByteBuffer.allocate(512);
                   ch.configureBlocking(false);
                   buffer.clear();
                   int n = ch.read(buffer);
                   if (n == -1) {
                       ch.close();
                       continue;
                   }
                   buffer.flip();
                   String message = new String(buffer.array());
                   System.out.println("GOT: " + message);

//                    for (Map.Entry<String, SocketChannel> client : clients.entrySet()) {
//                        if (ch == client.getValue()) {
//                            senderKey = client.getKey();
//                            break;
//                        }
//                    }

//                    for (Map.Entry<String, SocketChannel> client : clients.entrySet()) {
//                        SocketChannel value = client.getValue();
//                        ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
//
//                        //writeBuffer.put((senderKey + " : " + message).getBytes());
//                        writeBuffer.put((message).getBytes());
//                        writeBuffer.flip();
//
//                        value.write(writeBuffer);
//                    }

                    for (SocketChannel client : clients) {
                        SocketChannel value = client;
                        ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
                        writeBuffer.put(("Message: " + message).getBytes());
                        writeBuffer.flip();
                        value.write(writeBuffer);
                    }

                }

                it.remove();
            }
        }

    }
}
