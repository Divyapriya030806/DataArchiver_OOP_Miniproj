package com.example.archiver;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@MultipartConfig
public class ArchiveServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Part filePart = req.getPart("file");
            Part passPart = req.getPart("passphrase");
            Part algoPart = req.getPart("algorithm");

            if (filePart == null || passPart == null || algoPart == null) {
                resp.sendError(400, "Missing fields");
                return;
            }

            String filename = extractFileName(filePart);
            if (filename == null || filename.isBlank()) {
                filename = "uploaded";
            }

            byte[] fileBytes = readAllBytes(filePart.getInputStream());
            byte[] zipped = zipSingleFile(filename, fileBytes);

            String algoStr = new String(readAllBytes(algoPart.getInputStream()), StandardCharsets.UTF_8).trim();
            Encryptor.Algorithm algorithm = "AES_CBC".equalsIgnoreCase(algoStr) ? Encryptor.Algorithm.AES_CBC : Encryptor.Algorithm.AES_GCM;

            char[] password = new String(readAllBytes(passPart.getInputStream()), StandardCharsets.UTF_8).toCharArray();
            byte[] salt = Encryptor.randomBytes(16);
            byte[] encrypted = Encryptor.encrypt(algorithm, zipped, password, salt);

            String outName = filename + ".sda";
            String encoded = URLEncoder.encode(outName, StandardCharsets.UTF_8);
            resp.setStatus(200);
            resp.setContentType("application/octet-stream");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + encoded + "\"");
            resp.getOutputStream().write(encrypted);
        } catch (Exception e) {
            resp.sendError(500, "Error: " + e.getMessage());
        }
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) != -1) {
            bos.write(buf, 0, r);
        }
        return bos.toByteArray();
    }

    private static byte[] zipSingleFile(String filename, byte[] content) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            ZipEntry entry = new ZipEntry(filename);
            zos.putNextEntry(entry);
            zos.write(content);
            zos.closeEntry();
        }
        return bos.toByteArray();
    }

    private static String extractFileName(Part part) {
        String cd = part.getHeader("content-disposition");
        if (cd == null) return null;
        for (String token : cd.split(";")) {
            token = token.trim();
            if (token.startsWith("filename=")) {
                String name = token.substring(9).trim();
                if (name.startsWith("\"") && name.endsWith("\"")) {
                    name = name.substring(1, name.length() - 1);
                }
                return name;
            }
        }
        return null;
    }
}


