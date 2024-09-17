import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.sql.*;
import java.io.*;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Properties;

public class SignupServlet extends HttpServlet {

  private String dbUrl;
  private String dbUsername;
  private String dbPassword;

  public void init() throws ServletException {
      Properties properties = new Properties();
      try (InputStream input = getServletContext().getResourceAsStream("/WEB-INF/db.properties")) {
          if (input == null) {
              throw new ServletException("Sorry, unable to find db.properties");
          }
          properties.load(input);
          dbUrl = properties.getProperty("db.url");
          dbUsername = properties.getProperty("db.username");
          dbPassword = properties.getProperty("db.password");
      } catch (IOException e) {
          throw new ServletException("Error loading database properties", e);
      }
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    if (session != null && session.getAttribute("username") != null) {
      response.sendRedirect("main");
      return;
    }
    
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<!DOCTYPE html>"
        + "<html lang=\"en\">"
        + "<head>"
        + "<meta charset=\"UTF-8\">"
        + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
        + "<title>Signup</title>"
        + "</head>"
        + "<body>"
        + "<h1>Signup</h1>"
        + "<form action=\"signup\" method=\"POST\">"
        + "<label for=\"username\">Username:</label>"
        + "<input type=\"text\" id=\"username\" name=\"username\" required>"
        + "<br><br>"
        + "<label for=\"password\">Password:</label>"
        + "<input type=\"password\" id=\"password\" name=\"password\" required>"
        + "<br><br>"
        + "<label for=\"confirmPassword\">Confirm Password:</label>"
        + "<input type=\"password\" id=\"confirmPassword\" name=\"confirmPassword\" required>"
        + "<br><br>"
        + "<input type=\"submit\" value=\"Sign up\">"
        + "</form>"
        + "</body>"
        + "</html>");
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();

    String username = request.getParameter("username");
    String password = request.getParameter("password");
    String confirmPassword = request.getParameter("confirmPassword");

    if (!password.equals(confirmPassword)) {
      out.print("Passwords do not match. Please try again.");
      return;
    }

    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
      PreparedStatement ps = con.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
      ps.setString(1, username);
      ps.setString(2, hashedPassword);
      int i = ps.executeUpdate();
      if (i > 0) {
        out.print("You are successfully registered :)");
      } else {
        out.print("Registration failed :(");
      }
    } catch (Exception e) {
      e.printStackTrace();
      out.print("An error occurred: " + e.getMessage());
    }
  }
}
