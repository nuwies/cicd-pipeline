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
public class EditCategoryServlet extends DbConnectionServlet {

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
    StringBuilder categoryOptions = new StringBuilder();
    String selectedCategoryId = request.getParameter("category-id");
    String existingCategoryName = "";

    // connect to the database and retrieve categories
    try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        PreparedStatement stmt = con.prepareStatement("SELECT id, name FROM categories")) {

      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        categoryOptions.append("<option value='").append(id).append("'")
            .append(selectedCategoryId != null && selectedCategoryId.equals(String.valueOf(id))
                ? " selected"
                : "")
            .append(">").append(name).append("</option>");
      }

    } catch (SQLException e) {
      e.printStackTrace();
      out.println("<p>Error loading categories.</p>");
      return;
    }

    // fetch details of the selected category to prepopulate the form fields
    if (selectedCategoryId != null && !selectedCategoryId.isEmpty()) {
      try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
          PreparedStatement ps = con.prepareStatement("SELECT name FROM categories WHERE id = ?")) {

        ps.setInt(1, Integer.parseInt(selectedCategoryId));
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            existingCategoryName = rs.getString("name");
          }
        }

      } catch (SQLException e) {
        e.printStackTrace();
        out.println("<p>Error loading selected category details.</p>");
      }
    }

    // HTML form for editing a category
    out.println("<!DOCTYPE html>"
        + "<html><head><title>Edit Category</title></head>"
        + "<body><h1>Edit Category</h1><br>"
        + "<a href='main'>Back to Main Page</a><br><br>"
        + "<form method='GET' action='edit-category'>"
        + "<label for='category-id'>Select Category:</label>"
        + "<select id='category-id' name='category-id' required onchange='this.form.submit()'>"
        + "<option value=''>Select a category</option>"
        + categoryOptions.toString()
        + "</select><br>"
        + "</form>");

    // display the editing form only if a category is selected
    if (selectedCategoryId != null && !selectedCategoryId.isEmpty()) {
      out.println("<form method='POST' action='edit-category' enctype='multipart/form-data'>"
          + "<input type='hidden' name='category-id' value='" + selectedCategoryId + "'>"
          + "<label for='new-category-name'>New Category Name:</label>"
          + "<input type='text' id='new-category-name' name='new-category-name' value='"
          + existingCategoryName + "' required><br>"
          + "<label for='category-image'>Upload Image:</label>"
          + "<input type='file' id='category-image' name='category-image' accept='image/*' required><br>"
          + "<input type='submit' value='Update Category'><br><br><br>"
          + "<input type='submit' value='Delete Category' formaction='delete-category'>"
          + "</form>");
    }

    out.println("</body></html>");
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // retrieve form data
    String categoryId = request.getParameter("category-id");
    String newCategoryName = request.getParameter("new-category-name");

    // validate inputs
    if (categoryId == null || categoryId.isEmpty()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Category ID is required.");
      return;
    }

    // retrieve the image file part
    Part filePart = request.getPart("category-image");
    InputStream imageStream = null;

    if (filePart != null && filePart.getSize() > 0) {
      imageStream = filePart.getInputStream();
    } else {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Image file is required.");
      return;
    }

    // connect to the database to update the category
    try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        PreparedStatement ps = con.prepareStatement(
            "UPDATE categories SET name = ?, image = ? WHERE id = ?")) {

      // set parameters for the update query
      ps.setString(1, newCategoryName); // Always update to new name
      ps.setBlob(2, imageStream); // Update the image
      ps.setInt(3, Integer.parseInt(categoryId));

      // execute the update
      int rowsUpdated = ps.executeUpdate();
      if (rowsUpdated > 0) {
        response.sendRedirect("edit-success.html");
      } else {
        response.sendRedirect("edit-failure.html");
      }

    } catch (SQLException e) {
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating category.");
    } finally {
      if (imageStream != null) {
        imageStream.close();
      }
    }
  }
}
