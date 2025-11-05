package com.example.archiver;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@MultipartConfig
public class DecryptServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Part filePart = req.getPart("file");
            Part passPart = req.getPart("passphrase");
            if (filePart == null || passPart == null) {
                resp.sendError(400, "Missing fields");
                return;
            }
            byte[] payload = readAllBytes(filePart.getInputStream());
            char[] password = new String(readAllBytes(passPart.getInputStream()), StandardCharsets.UTF_8).toCharArray();

            byte[] zipBytes;
            try {
                zipBytes = Encryptor.decrypt(payload, password);
            } catch (Exception e) {
                resp.sendError(400, "Decryption failed: " + e.getMessage());
                return;
            }

            // Unzip first entry
            String outName;
            byte[] outBytes;
            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    resp.sendError(400, "Archive is empty");
                    return;
                }
                outName = entry.getName();
                outBytes = readAllBytes(zis);
                zis.closeEntry();
            }

            String encoded = URLEncoder.encode(outName, StandardCharsets.UTF_8);
            resp.setStatus(200);
            resp.setContentType("application/octet-stream");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + encoded + "\"");
            resp.getOutputStream().write(outBytes);
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
}


