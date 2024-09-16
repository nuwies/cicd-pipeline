
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.sql.*;
import java.io.*;

public class PlayServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String errMsg = "";
        Connection con = null;
        String content = "";
        try {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (Exception ex) {
            }
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/assignment1", "root", "");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from questions");
            while (rs.next()) {
               content = rs.getString("ContentName");
               break;
            }
            stmt.close();
            con.close();
        } catch (SQLException ex) {
            errMsg = errMsg + "\n--- SQLException caught ---\n";
            while (ex != null) {
                errMsg += "Message: " + ex.getMessage();
                errMsg += "SQLState: " + ex.getSQLState();
                errMsg += "ErrorCode: " + ex.getErrorCode();
                ex = ex.getNextException();
                errMsg += "";
            }
        }
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println(
                "<!DOCTYPE html>"
                + "<meta charset='UTF-8'>"
                + "<body>"
                + "<div>"
                + "<img src='images/" + content + "'>"
                + "</iframe>"
                + "</div>"
                + "<div>"
                + "<form action='/comp3940-assignment1/play' method='GET'>"
                + "<br>"
                + "<div class='button'>"
                + "<button class='button' id='prev'>Prev</button>"
                + "<button class='button' id='next'>Next</button>"
                + "</div>"
                + "<br>"
                + "</form>"
                + "<div>"
                + "<form action='main' method='GET'>"
                + "<button class='button' id='main'>Main</button>"
                + "</form>"
                + "</div>"
                + "<br>"
                + "</body>"
                + "</html>"
        );
    }
}
