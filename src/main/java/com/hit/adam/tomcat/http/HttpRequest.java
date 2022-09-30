package com.hit.adam.tomcat.http;

import com.alibaba.fastjson.JSONObject;
import com.hit.adam.tomcat.http.parser.StringParser;
import com.hit.adam.tomcat.http.reinforce.ReinforceJsonObjectWrapper;
import com.hit.adam.tomcat.http.reinforce.ReinforceMap;
import com.hit.adam.tomcat.util.ArrayUtil;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.Principal;
import java.util.*;

@SuppressWarnings("All")
public class HttpRequest implements HttpServletRequest {
    private Map<String, Object> headerMap;

    private String queryString;

    private ReinforceMap parameterMap;

    private String postData;

    private JSONObject postDataMap;

    private Set<Cookie> cookies;

    private String method;

    private String path;

    private String protocol;

    private String protocolVersion;

    private String remoteHost;

    private Integer remotePort;

    private String connection;

    private Cookie[] cookieArr;

    private JSONObject data;

    private boolean parsed;

    public HttpRequest(InputStream inputStream) {
        try {
            StringBuilder sb = new StringBuilder();
            byte[] bufferByteArray = ArrayUtil.getBufferByteArray();
            int size = 0;
            size = inputStream.read(bufferByteArray);
            for(int i = 0; i < size; ++i) {
                sb.append((char) bufferByteArray[i]);
            }
            String s = sb.toString();

            // 获取对应的map
            // 然后开始包装
            wrap(s);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HttpRequest(SocketChannel socketChannel, ByteBuffer byteBuffer) {
        try {
            /**
             * 读取的时候先有数据的是channel, 之后是读取channel到buffer中
             *
             * 其实不用while处理可以直接读取就像上面那样但是最好还是改一下
             */
            StringBuilder sb = new StringBuilder();
            int bytesRead = socketChannel.read(byteBuffer);
            while(bytesRead != 0) {
                //flip()
                //这里改变模式由写buffer转变为读buffer
                byteBuffer.flip();
                while(byteBuffer.hasRemaining())
                    sb.append((char) byteBuffer.get());

                //compat()
                // 如果此时还有数据未读，但是想要写数据，那就执行该方法
                // 之前未读的数据不会被覆盖掉

                //mark()可以标记数据，reset()会回复数据
                //rewind()设置回0
                bytesRead = socketChannel.read(byteBuffer);
            }

            String s = sb.toString();
            // 获取对应的map
            // 然后开始包装
            wrap(s);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * 包装这个http请求
     */
    private void wrap(String s) {

        /**
         * 这里是正确的，不需要修改
         */
        StringParser sp = new StringParser(s);
        this.headerMap = sp.getHeaders();

        this.queryString = sp.getQureyString();

        this.cookies = sp.getCookies();

        this.postData = sp.getPostData();

        this.parsed = false;

        this.method = (String) headerMap.get("method");

        this.path = (String) headerMap.get("path");

        this.protocol = (String) headerMap.get("protocol");

        this.protocolVersion = (String) headerMap.get("protocolVersion");

        this.remoteHost = (String) headerMap.get("remoteHost");

        this.remotePort = Integer.parseInt((String) headerMap.get("remotePort"));

        this.cookieArr = this.cookies.toArray(new Cookie[this.cookies.size()]);

        this.connection = (String) headerMap.get("connection");
    }

    /**
     *
     * 以下是ServletRequest实现方法
     */

    public String getPath() {
        return path;
    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getParameter(String s) {
        /**
         * 注意这里parse的处理——就是懒加载
         */
        if(parsed)
            return parameterMap.get(s);
        parsed = true;
        parameterMap.parseParameterMap();
        return parameterMap.get(s);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return null;
    }
    public Set<String> getPlainParameterNames() {
        if(parsed)
            return parameterMap.keySet();
        parameterMap.parseParameterMap();
        parsed = true;
        return parameterMap.keySet();
    }

    @Override
    public String[] getParameterValues(String s) {
        return new String[0];
    }

    public String[] getPlainParameterValues(String s) {
        if(parsed)
            return parameterMap.values().toArray(new String[parameterMap.size()]);
        parameterMap.parseParameterMap();
        parsed = true;
        return parameterMap.values().toArray(new String[parameterMap.size()]);
    }


    @Override
    public Map<String, String[]> getParameterMap() {
        return null;
    }

    public Map<String, String> getPlainParameterMap() {
        if(parsed)
            return parameterMap;
        parameterMap.parseParameterMap();
        parsed = true;
        return parameterMap;
    }

    /**
     * 对于请求体data的封装
     * 只有parsed来控制这个懒加载
     */
    public JSONObject getPostDataMap() {
        if(!parsed) {
            this.postDataMap = new ReinforceJsonObjectWrapper().parsePostData(postData);
            parsed = true;
        }
        return postDataMap;
    }

    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return this.remoteHost;
    }

    @Override
    public void setAttribute(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public String getRealPath(String s) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return this.remotePort;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * 内部类实现枚举的实现
     * 似乎是不行这个实现以后再说
     */
    private class MapEnumeration implements Enumeration<String> {

        private Map<String, Object> innerMap;

        public MapEnumeration(Map<String, Object> map) {
            this.innerMap = map;
        }

        @Override
        public boolean hasMoreElements() {
            return innerMap.containsKey("");
        }

        @Override
        public String nextElement() {
            return (String) innerMap.get("");
        }
    }
    /**
     *以下是HttpServlet实现方法
     */
    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return cookies.toArray(new Cookie[cookies.size()]);
    }

    @Override
    public long getDateHeader(String s) {
        return 0;
    }

    public String getHeader(String headerName) {
        return (String) headerMap.get(headerName);
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String s) {
        return 0;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return null;
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean b) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String s, String s1) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return null;
    }
    @Override
    public String toString() {
        return "Request{" +
                "headerMap=" + headerMap +
                ", method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", protocol='" + protocol + '\'' +
                ", protocolVersion='" + protocolVersion + '\'' +
                ", remotePort='" + remoteHost + '\'' +
                ", remotePort=" + remotePort +
                ", connection='" + connection + '\'' +
                '}';
    }
}
