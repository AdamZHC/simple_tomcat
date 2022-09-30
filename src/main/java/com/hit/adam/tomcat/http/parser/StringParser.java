package com.hit.adam.tomcat.http.parser;


import javax.servlet.http.Cookie;
import java.util.*;

@SuppressWarnings("All")
/**
 * 还需要把这里优化一波
 */

public class StringParser {

    /**
     * http://www.xxx.com/xxx_app;jsessionid=xxxxxxxxxx?a=x&b=x
     */

    private String originString;

    private Map<String, Object> headerMap;

    private String qureyString;

    private String postData;

    private Set<Cookie> cookies;

    public StringParser(String s) {
        //中间的所有的字符串
        this.originString = s;
        this.headerMap = new HashMap<>();
        this.cookies = new HashSet<>();
        parse();
    }

    private void parse() {
        //对于每个字段进行分割
        String[] lines = originString.split("\\r?\\n");
        //处理为(k:v)的形式
        /**
         * 考虑到这点时间效率不会太受影响
         * 所以说就是先把所有的都获取到然后再进行处理
         *
         * 也就说对于特殊需要处理的比如说method url可以处理一下 其它的都先按照正常的来
         */
        for(int i = 1; i < lines.length; ++i) {
            //获取冒号
            int mark = lines[i].indexOf(':');
            /**
             * 这里需要特殊处理一下Cookie和最后的请求体
             */
            String key = lines[i].substring(0, mark);
            String value = lines[i].substring(mark + 2);
            if(key.equals("Cookie")) {
                handleCookies(value);
                continue;
            }
            /**
             * 如果是空的话说明下一个是请求体
             */
            if(key.equals("")) {
                if(i + 1 < lines.length) {
                    handleData(lines[i + 1]);
                }
                break;
            }
            headerMap.put(key, value);
        }
        //具体的处理
        handleLine(lines[0]);
        handleHost((String) headerMap.get("Host"));
        handleConnection((String) headerMap.get("Connection"));
        /**
         * 这个就没必要留下了 因为对于cookie有特殊处理函数了
         *
         * 下面是测试的代码
         */
        //handleCookie((String) headerMap.get("Cookie"));
        //System.out.println("===============");
        //System.out.println(headerMap);
        //System.out.println("===============");
        //System.out.println(parameterMap);
        //System.out.println("===============");
        //System.out.println(data);
        //System.out.println("===============");
        //for(Cookie cookie : cookies) {
        //System.out.println(cookie.getName());
        //System.out.println(cookie.getValue());
        //}

    }

    private void handleData(String v) {
        postData = v;
    }

    private void handleCookies(String v) {
        /**
         * cookie这里的情况注意一下！！！ 是有空格的
         * Stream.of(cookieListString.split("; "))
         */
        String[] lines = v.split("; ");
        for(String line : lines) {
            int i = line.indexOf('=');
            cookies.add(new Cookie(line.substring(0, i), line.substring(i + 1)));
        }
    }

    private void handleLine(String v) {
        /**
         * 这里分为三部分——请求方法，请求url，请求协议
         */
        String[] lines = v.split("\\s");
        /**
         * 对于方法和协议的处理
         */

        handleMethod(lines[0]);
        handleProtocol(lines[2]);
        /**
         * 关键是下面，对于url的处理
         * 1.
         *      有两个分界点;jsessionid=
         *      ?
         *
         * /xxx_app;jsessionid=xxxxxxxxxx?a=x&b=x
         */
        String url = lines[1];
        int mark = url.indexOf(";jsessionid=");
        int i = url.indexOf('?');
        if(mark == -1) {
            headerMap.put("path", url.substring(0, (i == -1) ? url.length() : i));
        } else {
            headerMap.put("jsessionid", url.substring(url.indexOf('=') + 1, (i == -1) ? url.length() : i));
            headerMap.put("path", url.substring(0, mark));
        }
        /**
         * 处理请求参数
         */
        if(i != -1)
            this.qureyString = url.substring(i + 1);

    }

    @Deprecated
    private void handlePath(String v) {
        int i1 = v.indexOf('/');
        int i2 = v.lastIndexOf(' ');
        headerMap.put("path", v.substring(i1, i2));
    }

    //自己理解的，需要用到的再封装
    //之后的情况，可以最后再优化，先完成
    private void handleMethod(String v) {
        headerMap.put("method", v);
    }

    private void handleProtocol(String v) {
        int i = v.lastIndexOf('/');
        String protocol = v.substring(i - 4, i);
        headerMap.put("protocol",protocol);

        String protocolVersion = v.substring(i + 1);
        headerMap.put("protocolVersion", protocolVersion);
    }

    private void handleHost(String v) {
        int mark = v.indexOf(':');
        headerMap.put("remoteHost", v.substring(0, mark));
        headerMap.put("remotePort", v.substring(mark + 1));
    }

    private void handleConnection(String v) {
        headerMap.put("connection", v);
    }

    @Deprecated
    private void handleCookie(String v) {
       //可以为空的话，这个逻辑可以留下来
        headerMap.put("cookie", v);
    }

    public Map<String, Object> getHeaders() {
        return this.headerMap;
    }

    public String  getQureyString() {
        return this.qureyString;
    }

    public String getPostData() {
        return this.postData;
    }

    public Set<Cookie> getCookies() {
        return cookies;
    }

}
