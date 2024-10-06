import jakarta.servlet.http.*;
import jakarta.servlet.*;
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

    try {
      String query = "DELETE FROM categories WHERE id = ?";
      int rowsAffected = repository.delete(query, Integer.parseInt(categoryId));
      
      if (rowsAffected > 0) {
        out.println("<script type='text/javascript'>"
            + "alert('Category deleted successfully');"
            + "location='edit-category';"
            + "</script>");
      } else {
        out.println("<p>Category not found.</p>");
      }

    } catch (Exception e) {
      out.println("Error message: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
