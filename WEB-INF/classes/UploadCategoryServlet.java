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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        PreparedStatement ps = con
            .prepareStatement("SELECT user_type FROM users WHERE username = ?")) {
      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          userType = rs.getString("user_type");
        }
      }
    } catch (SQLException e) {
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

    Part filePart = request.getPart("category-image");

    if (filePart != null && filePart.getSize() > 0) {
      try (InputStream imageStream = filePart.getInputStream()) {

        try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            PreparedStatement preparedStatement = con.prepareStatement(
                "INSERT INTO categories (name, image) VALUES (?, ?)")) {

          preparedStatement.setString(1, categoryName);
          preparedStatement.setBlob(2, imageStream);

          int rowsAffected = preparedStatement.executeUpdate();

          if (rowsAffected > 0) {
            response.sendRedirect("upload-success.html");
          } else {
            response.sendRedirect("upload-fail.html");
          }
        } catch (SQLException ex) {
          handleSQLException(ex, response);
        }
      } catch (IOException e) {
        response.getWriter().println("Error reading the image file: " + e.getMessage());
      }
    } else {
      response.getWriter().println("Invalid form data or image not selected.");
    }
  }

  private void handleSQLException(SQLException ex, HttpServletResponse response) throws IOException {
    StringBuilder errorMessage = new StringBuilder("Error uploading category:<br>");
    while (ex != null) {
      errorMessage.append("Message: ").append(ex.getMessage()).append("<br>")
          .append("SQLState: ").append(ex.getSQLState()).append("<br>")
          .append("ErrorCode: ").append(ex.getErrorCode()).append("<br>");
      ex = ex.getNextException();
    }
    response.getWriter().println(errorMessage.toString());
  }
}
