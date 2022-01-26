package com.gscsd.smrpc.transport;

import com.gscsd.smrpc.Request;
import com.sun.deploy.net.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class HTTPTransportServer implements TransportServer{

    private RequestHandler handler;

    private Server server;

    @Override
    public void init(int port, RequestHandler requestHandler) {
        this.handler = requestHandler;
        this.server = new Server(port);

        // servlet处理请求
        ServletContextHandler handler = new ServletContextHandler();
        server.setHandler(handler);

        ServletHolder servletHolder = new ServletHolder(new RequestServlet());
        handler.addServlet(servletHolder, "/*");
    }

    @Override
    public void start() {
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            log.error("server start error, {}, {}", e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            log.error("server stop error, {}, {}", e.getMessage(), e);
        }
    }

    class RequestServlet extends HttpServlet {

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws  IOException {
            ServletInputStream inputStream = req.getInputStream();
            ServletOutputStream outputStream = resp.getOutputStream();
            if (handler != null) {
                handler.onRequest(inputStream, outputStream);
            }
            outputStream.flush();
        }
    }
}
