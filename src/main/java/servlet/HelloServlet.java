package servlet;

import com.hit.adam.tomcat.http.HttpRequest;
import com.hit.adam.tomcat.http.HttpResponse;

import javax.servlet.*;
import java.io.IOException;

public class HelloServlet implements Servlet {
    public void init(ServletConfig config) throws ServletException {
    }
    public ServletConfig getServletConfig() {
        return null;
    }
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {

        System.out.println("Start invoke TestServlet ... ");
        res.getWriter().println("Hello Servlet!");
    }
    public String getServletInfo() {
        return null;
    }
    public void destroy() {
    }
}