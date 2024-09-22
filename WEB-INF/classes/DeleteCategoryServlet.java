import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;

public class DeleteCategoryServlet extends DbConnectionServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.sendRedirect("edit-category");
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    String categoryId = request.getParameter("category-id");

    if (categoryId == null || categoryId.isEmpty()) {
      out.println("<p>Category ID is required.</p>");
      return;
    }

    try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        PreparedStatement ps = con.prepareStatement("DELETE FROM categories WHERE id = ?")) {

      ps.setInt(1, Integer.parseInt(categoryId));
      int rowsAffected = ps.executeUpdate();
      if (rowsAffected > 0) {
        out.println("<script type='text/javascript'>"
            + "alert('Category deleted successfully');"
            + "location='edit-category';"
            + "</script>");
      } else {
        out.println("<p>Category not found.</p>");
      }

    } catch (SQLException e) {
      e.printStackTrace();
      out.println("<p>Error deleting category.</p>");
    } catch (NumberFormatException e) {
      e.printStackTrace();
      out.println("<p>Invalid category ID format.</p>");
    }
  }

}
