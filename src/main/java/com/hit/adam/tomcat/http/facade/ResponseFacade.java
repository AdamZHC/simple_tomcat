package com.hit.adam.tomcat.http.facade;

import com.hit.adam.tomcat.http.HttpResponse;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

public class ResponseFacade implements HttpServletResponse {
    private HttpResponse response;

    public ResponseFacade(HttpResponse response) {
        this.response = response;
    }
    @Override
    public String getCharacterEncoding() {
        return response.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return response.getContentType();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return response.getOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return response.getWriter();
    }

    @Override
    public void setCharacterEncoding(String s) {
        response.setCharacterEncoding(s);
    }

    @Override
    public void setContentLength(int i) {
        response.setContentLength(i);
    }

    @Override
    public void setContentType(String s) {
        response.setContentType(s);
    }

    @Override
    public void setBufferSize(int i) {
        response.setBufferSize(i);
    }

    @Override
    public int getBufferSize() {
        return response.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        response.flushBuffer();
    }

    @Override
    public void resetBuffer() {
        response.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return response.isCommitted();
    }

    @Override
    public void reset() {
        response.reset();
    }

    @Override
    public void setLocale(Locale locale) {
        response.setLocale(locale);
    }

    @Override
    public Locale getLocale() {
        return response.getLocale();
    }

    @Override
    public void addCookie(Cookie cookie) {
        response.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String s) {
        return response.containsHeader(s);
    }

    @Override
    public String encodeURL(String s) {
        return response.encodeURL(s);
    }

    @Override
    public String encodeRedirectURL(String s) {
        return response.encodeRedirectURL(s);
    }

    @Override
    public String encodeUrl(String s) {
        return response.encodeUrl(s);
    }

    @Override
    public String encodeRedirectUrl(String s) {
        return response.encodeRedirectUrl(s);
    }

    @Override
    public void sendError(int i, String s) throws IOException {
        response.sendError(i, s);
    }

    @Override
    public void sendError(int i) throws IOException {
        response.sendError(i);
    }

    @Override
    public void sendRedirect(String s) throws IOException {
        response.sendRedirect(s);
    }

    @Override
    public void setDateHeader(String s, long l) {
        response.setDateHeader(s, l);
    }

    @Override
    public void addDateHeader(String s, long l) {
        response.addDateHeader(s, l);
    }

    @Override
    public void setHeader(String s, String s1) {
        response.setHeader(s, s1);
    }

    @Override
    public void addHeader(String s, String s1) {
        response.addHeader(s, s1);
    }

    @Override
    public void setIntHeader(String s, int i) {
        response.setIntHeader(s, i);
    }

    @Override
    public void addIntHeader(String s, int i) {
        response.addIntHeader(s, i);
    }

    @Override
    public void setStatus(int i) {
        response.setStatus(i);
    }

    @Override
    public void setStatus(int i, String s) {
        response.setStatus(i, s);
    }

    @Override
    public int getStatus() {
        return response.getStatus();
    }

    @Override
    public String getHeader(String s) {
        return response.getHeader(s);
    }

    @Override
    public Collection<String> getHeaders(String s) {
        return response.getHeaders(s);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return response.getHeaderNames();
    }
}
