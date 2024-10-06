import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import org.json.*;

public class DeleteQuestionServlet extends DbConnectionServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.sendRedirect("edit-question");
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    String questionId = request.getParameter("question-id");

    if (questionId == null || questionId.isEmpty()) {
      out.println("<p>Question ID is required.</p>");
      return;
    }

    try {
      String query = "DELETE FROM questions WHERE id = ?";
      int rowsAffected = repository.delete(query, Integer.parseInt(questionId));
      
      if (rowsAffected > 0) {
        out.println("<script type='text/javascript'>"
            + "alert('Question deleted successfully');"
            + "location='edit-question';"
            + "</script>");
      } else {
        out.println("<p>Question not found.</p>");
      }

    } catch (Exception e) {
      out.println("Error message: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
