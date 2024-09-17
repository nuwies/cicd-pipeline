import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.sql.*;
import java.io.*;

public class PlayServlet extends HttpServlet {

   public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      Connection con = null;
      Statement stmt = null;
      ResultSet rs = null;
      String errMsg = "";

      //setup html response
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<html>");
      out.println("<head><title>Select a Category</title></head>");
      out.println("<body>");
      out.println("<h1>Select a Category</h1>");

      try {
         //connect to db, assumes db name is clientserver, username root pw oracle1
          Class.forName("com.mysql.cj.jdbc.Driver");
         con = DriverManager.getConnection("jdbc:mysql://localhost:3306/clientserver", "root", "oracle1");
	Statement stmt2 = con.createStatement();

         //query db to fetch all categories from the 'categories' table
         String query = "SELECT name FROM categories";
          
         rs = stmt2.executeQuery(query);

         //dynamically generate a button for each category
         out.println("<form action='quiz' method='GET'>"); // assuming 'quiz' is the servlet handling quizzes
         while (rs.next()) {
            String categoryName = rs.getString("name");
            out.println("<button type='submit' name='category' value='" + categoryName + "'>" + categoryName + "</button><br>");
         }
         out.println("</form>");

      } catch (SQLException ex) {
         errMsg = "\n--- SQLException caught ---\n"; 
         while (ex != null) { 
            errMsg += "Message: " + ex.getMessage(); 
            errMsg += "SQLState: " + ex.getSQLState(); 
            errMsg += "ErrorCode: " + ex.getErrorCode(); 
            ex = ex.getNextException(); 
         }
         out.println("<p>Error: " + errMsg + "</p>");
      } catch (ClassNotFoundException e) {
         out.println("<p>Database driver not found!</p>");
      } finally {
         try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (con != null) con.close();
         } catch (SQLException e) {
            out.println("<p>Error closing database resources.</p>");
         }
      }

      out.println("</body></html>");
      out.close();
   }
}
