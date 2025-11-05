package com.example.archiver;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import jakarta.servlet.MultipartConfigElement;

public class App {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        Server server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Serve static content from classpath /static
        ServletHolder defaultServlet = new ServletHolder("default", new org.eclipse.jetty.servlet.DefaultServlet());
        defaultServlet.setInitParameter("resourceBase", App.class.getClassLoader().getResource("static").toExternalForm());
        defaultServlet.setInitParameter("dirAllowed", "false");
        context.addServlet(defaultServlet, "/");

        // Upload/Archive endpoint with multipart config
        ServletHolder archiveHolder = new ServletHolder(new ArchiveServlet());
        context.addServlet(archiveHolder, "/archive");
        MultipartConfigElement multipart = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));
        archiveHolder.getRegistration().setMultipartConfig(multipart);

        // Decrypt endpoint
        ServletHolder decryptHolder = new ServletHolder(new DecryptServlet());
        context.addServlet(decryptHolder, "/decrypt");
        MultipartConfigElement multipart2 = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));
        decryptHolder.getRegistration().setMultipartConfig(multipart2);

        server.setHandler(context);
        server.start();
        server.join();
    }
}


