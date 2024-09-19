import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DbConnectionServlet extends HttpServlet {
  protected String dbUrl;
  protected String dbUsername;
  protected String dbPassword;

  @Override
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
}
