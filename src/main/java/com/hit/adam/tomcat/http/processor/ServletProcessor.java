package com.hit.adam.tomcat.http.processor;

import com.hit.adam.tomcat.http.facade.RequestFacade;
import com.hit.adam.tomcat.http.facade.ResponseFacade;
import com.hit.adam.tomcat.http.HttpRequest;
import com.hit.adam.tomcat.http.HttpResponse;
import com.hit.adam.tomcat.util.ServerUtil;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("All")
public class ServletProcessor implements Processor{

    private static final URLClassLoader URL_CLASS_LOADER;

    /**
     * 静态代码块 Deprecated
     */
    static {
        /*定位到我们的webroot/servlet/文件夹*/

        /**
         * WEB_ROOT = "D:\\WebDevelopment\\Tomcat\\simple_tomcat\\src\\main\\resources\\"
         * 最终定位到对应的servlet文件夹:
         * "D:\\WebDevelopment\\Tomcat\\simple_tomcat\\src\\main\\resources\\servlet"
         * 根据URLClassLoader的思路，可以使得其文件夹下面的一系列文件被反射获取到
         *
         * 因此我们只需要Servlet的名字即可以获取到
         *
         * 相同的思路，通过直接反射获取到
         */

        String WEB_ROOT = "";
        URL servletClassPath = null;
        try {
            servletClassPath = new File(ServerUtil.SERVLET_ROOT + "servlet").toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        //初始化classloader
        URL_CLASS_LOADER = new URLClassLoader(new URL[]{servletClassPath});
    }

    @Override
    public void process(HttpRequest request, HttpResponse response) throws IOException {
        /**
         * 获得servlet名字
         */
        String servletName = this.parseServletName(request.getPath());
        String totalClassName = "servlet." + servletName;


        //使用URLClassLoader加载这个Servlet并实例化
        try {
            Class servletClass = Class.forName(totalClassName);
            Servlet servlet = (Servlet) servletClass.newInstance();
            //输出响应行
            response.getPlainOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            //调用servlet的service方法
            //涉及到用户写的代码的时候就使用外观者模式
            /**
             * 这里有个点就是HttpServlet中的service方法是通过方法重载的方法
             * 先判断是否是真正的实例，然后因为都实现了对应的HttpServletRequest/HttpServletResponse
             * 并且是线性继承关系，因此可以都通过接口传递
             */
            servlet.service(new RequestFacade(request),new ResponseFacade(response));
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ServletException e) {
            e.printStackTrace();
        }


    }

    public String parseServletName(String path) {
        return path.substring(path.lastIndexOf('/') + 1);

    }
}
