import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.sql.*;
import java.io.*;
public class PlayServlet extends HttpServlet {
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      String errMsg = "";
      Connection con = null;
      try {
         try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (Exception ex) { }
         con = DriverManager.getConnection("jdbc:mysql://localhost:3306/assignment1", "system", "mysql1");
         Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery("select id, question, contentpath from trivias");
         while (rs.next()) {
            String content = rs.getString("contentpath");
            
	 }
         stmt.close();
         con.close();
      } catch(SQLException ex) { 
         errMsg = errMsg + "\n--- SQLException caught ---\n"; 
         while (ex != null) { 
            errMsg += "Message: " + ex.getMessage (); 
            errMsg += "SQLState: " + ex.getSQLState (); 
            errMsg += "ErrorCode: " + ex.getErrorCode (); 
            ex = ex.getNextException(); 
            errMsg += "";
         } 
      } 
      String contentPath = "tgbNymZ7vqY";
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println( 
"<!DOCTYPE html>" +
"<meta charset='UTF-8'>" +
"<body>" +
"<div>" +
"<iframe id=\"Video\" width=\"420\" height=\"345\" src=https://www.youtube.com/embed/" + contentPath +"?autoplay=1&mute=1&start=62&end=162>" +
"</iframe>" +
"</div>" +
"<div>" +
"<form action='/trivia/play' method='GET'>" +
"<br>" +
"<div class='button'>" +          
"<button class='button' id='prev'>Prev</button>" +
"<button class='button' id='next'>Next</button>" +
"</div>" +
"<br>" +
"</form>" +
"<div>" +
"<form action='main' method='GET'>" +
"<button class='button' id='main'>Main</button>" +
"</form>" +
"</div>" +
"<br>" +
"</body>" +
"</html>"
      );
   }   
}