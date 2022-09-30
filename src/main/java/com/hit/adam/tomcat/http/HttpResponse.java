package com.hit.adam.tomcat.http;

import com.hit.adam.tomcat.util.ArrayUtil;
import com.hit.adam.tomcat.util.ServerUtil;
import jdk.nashorn.internal.ir.RuntimeNode;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;

@SuppressWarnings("All")
public class HttpResponse implements HttpServletResponse {

    private final int BUF_SIZE = 1 << 10;

    private HttpRequest request;

    private OutputStream outputStream;

    private SocketChannel socketChannel;


    /**
     * 构造方法 得到HttpResponse
     * @param outputStream: 对应的服务端套接字的outputStream
     */
    public HttpResponse(OutputStream outputStream, HttpRequest request) {
        this.outputStream = outputStream;
        this.request = request;
    }

    public HttpResponse(SocketChannel socketChannel, HttpRequest request) {
        this.socketChannel = socketChannel;
        this.request = request;
    }

    /**
     * 设置请求
     * @param request: 待设置的请求
     */
    public void setRequest(HttpRequest request) {
        this.request = request;
    }
    /**
     * 发送静态资源
     */
    public void bioSendStaticResouce() throws IOException {
        byte[] bytes = ArrayUtil.getBufferByteArray();
        int bufferSize = ArrayUtil.getBufferSize();

        String filePath = ServerUtil.WEB_ROOT + "static\\" +request.getPath();

        FileInputStream fis = null;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                fis = new FileInputStream(file);
                //输出响应行
                outputStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                int ch = fis.read(bytes, 0, bufferSize);
                while (ch != -1) {
                    outputStream.write(bytes, 0, ch);
                    ch = fis.read(bytes, 0, bufferSize);
                }
            }
            else {
                String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: 23\r\n" +
                        "\r\n" +
                        "<h1>File Not Found</h1>";
                outputStream.write(errorMessage.getBytes());
            }
        }
        catch (Exception e) {
            System.out.println(e.toString() );
        }
        finally {
            if (fis != null)
                fis.close();
        }
    }
    public void nioSendStaticResouce() throws IOException {

        String filePath = ServerUtil.WEB_ROOT + "static\\" +request.getPath();

        RandomAccessFile aFile = null;
        try {
            aFile = new RandomAccessFile(filePath,"rw");

            FileChannel fileChannel = aFile.getChannel();
            //为缓冲区分配
            ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);
            //输出响应行
            socketChannel.write(ByteBuffer.wrap("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.UTF_8)));


            int bytesRead = fileChannel.read(buf);
            while(bytesRead != -1) {
                //flip()
                //这里改变模式由写buffer转变为读buffer
                buf.flip();
                while(buf.hasRemaining()) {
                    socketChannel.write(buf);
                }
                //compat()
                // 如果此时还有数据未读，但是想要写数据，那就执行该方法
                // 之前未读的数据不会被覆盖掉

                //mark()可以标记数据，reset()会回复数据
                //rewind()设置回0
                buf.compact();
                bytesRead = fileChannel.read(buf);
            }

        }
        catch (FileNotFoundException e) {
            String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: 23\r\n" +
                    "\r\n" +
                    "<h1>File Not Found</h1>";
            socketChannel.write(ByteBuffer.wrap(errorMessage.getBytes()));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (aFile != null)
                aFile.close();
        }
    }

    public void responseSuccess() throws IOException {
        outputStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.UTF_8));
    }
    public Channel getChannel() {
        return this.socketChannel;
    }
    /**
     * ServletResponse的实现方法
     */
    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    public OutputStream getPlainOutputStream() throws IOException {
        return this.outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(this.outputStream);
    }

    @Override
    public void setCharacterEncoding(String s) {

    }

    @Override
    public void setContentLength(int i) {

    }

    @Override
    public void setContentType(String s) {

    }

    @Override
    public void setBufferSize(int i) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale locale) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    /**
     * HttpServletResponse封装的响应实现方法
     */
    @Override
    public void addCookie(Cookie cookie) {

    }

    @Override
    public boolean containsHeader(String s) {
        return false;
    }

    @Override
    public String encodeURL(String s) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String s) {
        return null;
    }

    @Override
    public String encodeUrl(String s) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String s) {
        return null;
    }

    @Override
    public void sendError(int i, String s) throws IOException {

    }

    @Override
    public void sendError(int i) throws IOException {

    }

    @Override
    public void sendRedirect(String s) throws IOException {

    }

    @Override
    public void setDateHeader(String s, long l) {

    }

    @Override
    public void addDateHeader(String s, long l) {

    }

    @Override
    public void setHeader(String s, String s1) {

    }

    @Override
    public void addHeader(String s, String s1) {

    }

    @Override
    public void setIntHeader(String s, int i) {

    }

    @Override
    public void addIntHeader(String s, int i) {

    }

    @Override
    public void setStatus(int i) {

    }

    @Override
    public void setStatus(int i, String s) {

    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getHeader(String s) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String s) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }
}
