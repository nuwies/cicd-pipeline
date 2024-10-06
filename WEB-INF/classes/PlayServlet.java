import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class PlayServlet extends DbConnectionServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.sendRedirect("login");
        } else {
            session.setAttribute("selectedCategory", null);
            session.setAttribute("questionNumber", null);
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();

            try {
                // use repository to fetch categories
                List<Map<String, Object>> categories = repository.select("SELECT id, name, image FROM categories");

                // check to see if there is category in the first place
                if (categories.isEmpty()) {
                    // if none, redirect
                    response.sendRedirect("main");
                    return;
                }

                out.println("<html>");
                out.println("<head>");
                out.println("<title>Select a Category</title>");
                out.println("<style>");
                out.println("body { font-family: Arial, sans-serif; }");
                out.println(".grid-container { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; padding: 20px; }");
                out.println(".grid-item { position: relative; cursor: pointer; }");
                out.println(".grid-item img { width: 100%; height: auto; border-radius: 8px; }");
                out.println(".grid-item button { position: absolute; bottom: 10px; left: 10px; right: 10px; padding: 10px; background: rgba(0, 0, 0, 0.7); color: white; border: none; border-radius: 5px; }");
                out.println("</style>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Select a Category</h1>");
                out.println("<a href='main'>Back to Main Page</a><br><br>");
                out.println("<div class='grid-container'>");

                // Iterate over the list of categories
                for (Map<String, Object> category : categories) {
                    Integer categoryId = (Integer) category.get("id");
                    String categoryName = (String) category.get("name");
                    byte[] imageBytes = (byte[]) category.get("image");

                    // Display image as Base64 if it exists, otherwise use a placeholder
                    String imageSrc = (imageBytes != null)
                            ? "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(imageBytes)
                            : "images/placeholder.jpg";

                    out.println("<div class='grid-item'>");
                    out.println("<form action='quiz.html?categoryID=" + categoryId + "'>");
                    out.println("<input type='hidden' name='category' value='" + categoryId + "'>");
                    out.println("<img src='" + imageSrc + "' alt='" + categoryName + "'>");
                    out.println("<button type='submit'>" + categoryName + "</button>");
                    out.println("</form>");
                    out.println("</div>");
                }

                out.println("</div>");
                out.println("</body>");
                out.println("</html>");

            } catch (Exception e) {
                e.printStackTrace();
                out.println("<p>Error loading categories: " + e.getMessage() + "</p>");
            } finally {
                out.close();
            }
        }
    }
}
