import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import java.sql.*;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

@MultipartConfig
public class QuestionUploadServlet extends HttpServlet {
   String dbUrl;
   String dbUsername;
   String dbPassword;

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

   @Override
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
         result = stmt.executeQuery("SELECT name FROM categories");
         while(result.next()) {
            String category = result.getString("name");
            categorySelect += "<option value='" + category + "'>" + category + "</option>"; 
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
      out.println("<!DOCTYPE html>" +
      "<head><title>Create A Quiz Question</title></head>" +
      "<body><h1>Create A Quiz Question</h1><br>" +
         "<form method='POST' action='uploadquestion' enctype='multipart/form-data'>" +
              "<label for='category'>Category</label><select id='category' name='category' required>" +
              categorySelect + "</select><br>" +
              "<label for='question'>Question:</label><input type='text' id='question' name='question' maxlength='256' required><br>" +
              "<label for='correct-answer'>Correct Answer:</label><input type='text' id='correct-answer' name='correct-answer' maxlength='256' required><br>" +
              "<label for='wrong-answer1'>Wrong Answer 1:</label><input type='text' id='wrong-answer1' name='wrong-answer1' maxlength='256' required><br>" +
              "<label for='wrong-answer2'>Wrong Answer 2 (Optional):</label><input type='text' id='wrong-answer2' name='wrong-answer2' maxlength='256'><br>" +
              "<label for='wrong-answer3'>Wrong Answer 3 (Optional):</label><input type='text' id='wrong-answer3' name='wrong-answer3' maxlength='256'><br>" +
              "<input type='File' id='file' name='filename'><br>" +
              "<input type='submit' value='Submit'>" +
          "</form>" +
      "</body></html>");
   }

   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
      Part filePart = request.getPart("filename");
      String category = request.getParameter("category");
      String question = request.getParameter("question");
      String correctAnswer = request.getParameter("correct-answer");
      String wrongAnswer1 = request.getParameter("wrong-answer1");
      String wrongAnswer2 = request.getParameter("wrong-answer2");
      String wrongAnswer3 = request.getParameter("wrong-answer3");
      String fileName = filePart.getSubmittedFileName();
      String filePath = "";
      if (!fileName.isEmpty()) {
         filePath = System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/media/" + fileName;
      }

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
                     "INSERT INTO questions (Category, Question, CorrectAnswer, WrongAnswer1, WrongAnswer2, WrongAnswer3, ContentName) VALUES (?,?,?,?,?,?,?)");
         preparedStatement.setString(1, category);
         preparedStatement.setString(2, question);
         preparedStatement.setString(3, correctAnswer);
         preparedStatement.setString(4, wrongAnswer1);
         preparedStatement.setString(5, wrongAnswer2);
         preparedStatement.setString(6, wrongAnswer3);
         preparedStatement.setString(7, "media/" + fileName);
         int row = preparedStatement.executeUpdate();
         preparedStatement.close();
      } catch (SQLException ex) {
         while (ex != null) {
            System.out.println("Message: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("ErrorCode: " + ex.getErrorCode());
            ex = ex.getNextException();
            System.out.println("");
         }
      }

      // Save file in server images directory
      filePart.write(filePath);

      // 
      response.sendRedirect("upload-success.html");
   }
}
