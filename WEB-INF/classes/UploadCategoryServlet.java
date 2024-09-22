import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;

public class UploadCategoryServlet extends DbConnectionServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.sendRedirect("login");
            return;
        }
      
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>"
                + "<head><title>Upload Category</title></head>"
                + "<body><h1>Create A New Category</h1><br>"
                + "<form method='POST' action='upload-category'>"
                + "<label for='category-name'>Category Name:</label>"
                + "<input type='text' id='category-name' name='category-name' required><br>"
                + "<input type='submit' value='Submit'>"
                + "</form>"
                + "</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String categoryName = request.getParameter("category-name");

        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

            // Create the content path based on the category name
            String contentPath = "path/to/" + categoryName + "/content";

            PreparedStatement preparedStatement = con
                    .prepareStatement("INSERT INTO categories (name, content_path) VALUES (?, ?)");
            preparedStatement.setString(1, categoryName);
            preparedStatement.setString(2, contentPath); // Use the constructed content path
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Uploading error!");
                System.out.println("Message: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("ErrorCode: " + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println("");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        response.sendRedirect("upload-success.html");
    }
}
