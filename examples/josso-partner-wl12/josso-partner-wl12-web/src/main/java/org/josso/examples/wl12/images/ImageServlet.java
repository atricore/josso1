package org.josso.examples.wl12.images;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class ImageServlet extends HttpServlet {

    private String imgFolder = "/opt/ora/images";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO : This must render an image:

        String imgName = req.getParameter("img");

        File image = new File("/opt/ora/images/" + imgName);

        if (!(imgName.endsWith(".png") || !imgName.endsWith(".jpg"))) {
            resp.setStatus(404);
            return;
        }

        if (!image.getAbsolutePath().startsWith("/opt/ora/images")) {
            resp.setStatus(404);
            return;
        }


        FileInputStream fis = new FileInputStream(image);
        try {
            resp.setContentType("image/png");
            byte[] buf = new byte[8192];
            int length;
            while ((length = fis.read(buf)) > 0) {
                resp.getOutputStream().write(buf, 0, length);
            }

        } finally {
            fis.close();
        }

    }
}
