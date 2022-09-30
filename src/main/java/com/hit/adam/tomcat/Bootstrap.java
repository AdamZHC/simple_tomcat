package com.hit.adam.tomcat;


import com.hit.adam.tomcat.connector.Connector;
import com.hit.adam.tomcat.connector.impl.NioHttpConnector;
import com.hit.adam.tomcat.http.HttpRequest;
import com.hit.adam.tomcat.http.HttpResponse;
import com.hit.adam.tomcat.http.parser.StringParser;
import com.hit.adam.tomcat.http.processor.ServletProcessor;
import com.hit.adam.tomcat.http.processor.StaticResourceProcessor;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@SuppressWarnings("All")
/**
 * 这里的思想就是把模块拆分出来，以便与后面网络库的统一处理
 */
public class Bootstrap {
    public static void main(String[] args) throws IOException {
        //开启端口获取套接字
        //先用简单的实现方式
        startUp(1);
    }
    final static int BASE_BUFFER_SIZE =  1 << 8;
    final static int MULTI_BUFFER_SIZE = 1 << 2;
    static byte[] getBufferByteArray() {
        return new byte[BASE_BUFFER_SIZE * MULTI_BUFFER_SIZE];
    }
    static void startUp(int mark) throws IOException {
        System.out.println("connector initial...");
        Connector connector = new NioHttpConnector();
        connector.initialize();
        connector.start();
        System.in.read();
    }
    static void startUp(boolean f) {

        /**
         * 放到外面实现释放资源
         */
        ServerSocket serverSocket = null;
        Socket acceptSocket = null;
        InputStream input = null;
        OutputStream output = null;
        try {
            /**
             * 这里不太好修改，因为后面需要变化
             */
            serverSocket = new ServerSocket(8080);
            acceptSocket = serverSocket.accept();
            /**
             * 先考虑接受http请求 记住这个用法
             */
            input = acceptSocket.getInputStream();
            output = acceptSocket.getOutputStream();

            HttpRequest httpRequest = new HttpRequest(input);

            HttpResponse httpResponse = new HttpResponse(output, httpRequest);

            /**
             * 处理静态或者动态逻辑
             */
            String path = httpRequest.getPath();

            if(path.startsWith("/servlet")) {
                //这里需要调用到里面的方法，所以这里不需要调用外观者模式
                new ServletProcessor().process(httpRequest, httpResponse);
            } else {
                new StaticResourceProcessor().process(httpRequest, httpResponse);
            }

            acceptSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //释放资源
                if(serverSocket != null) {
                   serverSocket.close();
                }
                if(acceptSocket != null) {
                    acceptSocket.close();
                }
                if(input != null) {
                    input.close();
                }
                if(serverSocket != null) {
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    static void startUp() {

        try {
            System.out.println("服务端开启");
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("=======================");
            Socket acceptSocket = serverSocket.accept();
            /**
             * 先考虑接受http请求 记住这个用法
             */
            InputStream is = acceptSocket.getInputStream();
            OutputStream output = acceptSocket.getOutputStream();
            /**
             * 读取http请求
             * 这个有一定的写法
             */
            StringBuilder sb = new StringBuilder();
            byte[] bufferByteArray = getBufferByteArray();
            int size = is.read(bufferByteArray);
            for(int i = 0; i < size; ++i) {
                sb.append((char) bufferByteArray[i]);
            }
            String s = sb.toString();
            System.out.println(s);
            System.out.println("==========================");
            Map<String, Object> headers = new StringParser(s).getHeaders();
            for(Map.Entry<String, Object> entry : headers.entrySet()) {
                System.out.print(entry.getKey());
                System.out.print("=");
                System.out.println(entry.getValue());
            }
//            HttpResponse resp = new HttpResponse(output, );
//            resp.sendStaticResouce();
            response(output);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static void response(OutputStream output) throws IOException {

        byte[] bytes = getBufferByteArray();

        final String WEB_ROOT = "D:\\WebDevelopment\\Tomcat\\simple_tomcat\\src\\main\\resources\\static\\";
        String filePath = WEB_ROOT + "index.html";


        FileInputStream fis = null;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                fis = new FileInputStream(file);
                //输出响应行
                output.write("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                int ch = fis.read(bytes, 0, BASE_BUFFER_SIZE * MULTI_BUFFER_SIZE);
                while (ch != -1) {
                    output.write(bytes, 0, ch);
                    ch = fis.read(bytes, 0, BASE_BUFFER_SIZE * MULTI_BUFFER_SIZE);
                }
            }
            else {
                // file not found
                String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: 23\r\n" +
                        "\r\n" +
                        "<h1>File Not Found</h1>";
                output.write(errorMessage.getBytes());
            }
        }
        catch (Exception e) {
// thrown if cannot instantiate a File object
            System.out.println(e.toString() );
        }
        finally {
            if (fis!=null)
                fis.close();
        }
    }
}
