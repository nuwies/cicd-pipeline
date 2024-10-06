import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

//maximum file size is 16MB right now
@MultipartConfig(maxFileSize = 16177215)
public class UploadCategoryServlet extends DbConnectionServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.sendRedirect("login");
            return;
        }

        String username = (String) session.getAttribute("username");
        String userType = null;

        try {
          userType = repository.getUserType(username);
        } catch (Exception e) {
          e.printStackTrace();
        }

        if (!"admin".equalsIgnoreCase(userType)) {
            response.sendRedirect("main");
            return;
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>"
            + "<html>"
            + "<head><title>Upload Category</title></head>"
            + "<body><h1>Create A New Category</h1><br>"
            + "<form method='POST' action='upload-category' enctype='multipart/form-data'>"
            + "<label for='category-name'>Category Name:</label>"
            + "<input type='text' id='category-name' name='category-name' required><br><br>"
            + "<label>Auto Play:</label><br>"
            + "<input type='radio' id='auto-play-yes' name='auto-play' value='true'>"
            + "<label for='auto-play-yes'>Yes</label><br>"
            + "<input type='radio' id='auto-play-no' name='auto-play' value='false' checked>"
            + "<label for='auto-play-no'>No</label><br><br>"
            + "<label for='category-image'>Upload Image:</label>"
            + "<input type='file' id='category-image' name='category-image' accept='image/*' required><br><br>"
            + "<input type='submit' value='Submit'>"
            + "</form>"
            + "</body>"
            + "</html>");
     }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String categoryName = request.getParameter("category-name");
        String autoPlayParam = request.getParameter("auto-play");
        boolean autoPlay = "true".equals(autoPlayParam);

        Part filePart = request.getPart("category-image");

        if (filePart != null && filePart.getSize() > 0) {
          try (InputStream imageStream = filePart.getInputStream()) {
              String query = "INSERT INTO categories (name, image, auto_play) VALUES (?, ?, ?)";
              int rowsAffected = repository.insert(query, categoryName, imageStream, autoPlay);

              if (rowsAffected > 0) {
                  response.sendRedirect("upload-success.html");
              } else {
                  response.sendRedirect("upload-fail.html");
              }
          } catch (IOException e) {
              response.getWriter().println("Error reading the image file: " + e.getMessage());
          } catch (Exception e) {
              response.getWriter().println("Error message: " + e.getMessage());
          }
      } else {
          response.getWriter().println("Invalid form data or image not selected.");
      }
  }
}
