import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;

public class MainServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    if (session == null) {
      response.setStatus(HttpServletResponse.SC_FOUND); // 302
      response.sendRedirect("login");
      return;
    }

    String username = (String) session.getAttribute("username");
    String title = "Logged in as: " + username;
    response.setContentType("text/html");
    String docType = "<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">";
    String html = docType + "<html>"
        + "<head>"
        + "<title>" + title + "</title>"
        + "</head>"
        + "<body bgcolor=\"#f0f0f0\">"
        + "<h1 align=\"center\">" + title + "</h1>"
        + "<div style=\"text-align: center;\">"
        + "<form action=\"upload-question\" method=\"GET\">"
        + "<input type=\"submit\" value=\"UPLOAD QUESTION\" />"
        + "</form>"
        + "</div>"
        + "<div style=\"text-align: center;\">"
        + "<form action=\"play\" method=\"GET\">"
        + "<input type=\"submit\" value=\"GALLERY\" />"
        + "</form>"
        + "</div>"
        + "<div style=\"text-align: center;\">"
        + "<form action=\"logout\" method=\"GET\">"
        + "<input type=\"submit\" value=\"LOGOUT\" />"
        + "</form>"
        + "</div>"
        + "</body>"
        + "</html>";
    PrintWriter out = response.getWriter();
    out.println(html);
  }
}
