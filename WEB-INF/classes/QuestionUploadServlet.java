
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;

@MultipartConfig
public class QuestionUploadServlet extends DbConnectionServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.sendRedirect("login");
            return;
        }
      
        Connection con = null;
        ResultSet result = null;
        String categorySelect = "";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.out.println("Message: " + ex.getMessage());
            return;
        }

        try {
            con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            Statement stmt = con.createStatement();
            result = stmt.executeQuery("SELECT * FROM categories");
            while (result.next()) {
                String category = result.getString("name");
                String categoryID = result.getString("id");
                categorySelect += "<option value='" + categoryID + "'>" + category + "</option>";
            }
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Message: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("ErrorCode: " + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println("");
            }
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>"
                + "<head><title>Create A Quiz Question</title></head>"
                + "<body><h1>Create A Quiz Question</h1><br>"
                + "<form method='POST' action='upload-question' enctype='multipart/form-data'>"
                + "<label for='category'>Category</label><select id='category' name='category' required>"
                + categorySelect + "</select><br>"
                + "<label for='question'>Question:</label><input type='text' id='question' name='question' maxlength='256' required><br>"
                + "<label for='correct-answer'>Correct Answer:</label><input type='text' id='correct-answer' name='correct-answer' maxlength='256' required><br>"
                + "<label for='wrong-answer1'>Wrong Answer 1:</label><input type='text' id='wrong-answer1' name='wrong-answer1' maxlength='256' required><br>"
                + "<label for='wrong-answer2'>Wrong Answer 2 (Optional):</label><input type='text' id='wrong-answer2' name='wrong-answer2' maxlength='256'><br>"
                + "<label for='wrong-answer3'>Wrong Answer 3 (Optional):</label><input type='text' id='wrong-answer3' name='wrong-answer3' maxlength='256'><br>"
                + "<input type='File' id='file' name='filename'><br>"
                + "<input type='submit' value='Submit'>"
                + "</form>"
                + "</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Part filePart = request.getPart("filename");
        String categoryID = request.getParameter("category");
        String question = request.getParameter("question");
        String correctAnswer = request.getParameter("correct-answer");
        String wrongAnswer1 = request.getParameter("wrong-answer1");
        String wrongAnswer2 = request.getParameter("wrong-answer2");
        String wrongAnswer3 = request.getParameter("wrong-answer3");
        String fileName = filePart.getSubmittedFileName();
        String filePath = "";
        if(!fileName.trim().isEmpty()) filePath = System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/media/" + fileName;

        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.out.println("Message: " + ex.getMessage());
            return;
        }

        try {
            con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            PreparedStatement preparedStatement = con
                    .prepareStatement(
                            "INSERT INTO questions (category, question, correct_answer, wrong_answer_1, wrong_answer_2, wrong_answer_3, content_path) VALUES (?,?,?,?,?,?,?)");

            preparedStatement.setInt(1, Integer.valueOf(categoryID));
            preparedStatement.setString(2, question);
            preparedStatement.setString(3, correctAnswer);
            preparedStatement.setString(4, wrongAnswer1);
            preparedStatement.setString(5, wrongAnswer2);
            preparedStatement.setString(6, wrongAnswer3);
            if(!fileName.trim().isEmpty()) preparedStatement.setString(7, "media/" + fileName);
            else preparedStatement.setString(7, fileName);
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
        }

        // Save file in server images directory
        try {
            filePart.write(filePath);
        } catch (Exception e) {
            System.out.println("No file was selected!");
        }

        response.sendRedirect("upload-success.html");
    }
}
