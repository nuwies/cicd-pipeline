import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import java.sql.*;
import java.io.*;
import java.time.LocalDate;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.StringBuilder;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.text.*;
import java.nio.*;

@MultipartConfig
public class QuestionUploadServlet extends HttpServlet {
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
      Part filePart = request.getPart("filename");
      String question = request.getParameter("question");
      String correctAnswer = request.getParameter("correct-answer");
      String wrongAnswer1 = request.getParameter("wrong-answer1");
      String wrongAnswer2 = request.getParameter("wrong-answer2");
      String wrongAnswer3 = request.getParameter("wrong-answer3");
      String fileName = filePart.getSubmittedFileName();
      String filePath = "";
      if (!fileName.isEmpty()) {
         filePath = System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/images/" + fileName;
      }

      Connection con = null;
      try {
         Class.forName("com.mysql.cj.jdbc.Driver");
      } catch (Exception ex) {
         System.out.println("Message: " + ex.getMessage());
         return;
      }

      try {
         con = DriverManager.getConnection("jdbc:mysql://localhost:3306/assignment1", "root", "");
         PreparedStatement preparedStatement = con
               .prepareStatement("INSERT INTO questions (Question, CorrectAnswer, WrongAnswer1, WrongAnswer2, WrongAnswer3, ContentName) VALUES (?,?,?,?,?,?)");
         preparedStatement.setString(1, question);
         preparedStatement.setString(2, correctAnswer);
         preparedStatement.setString(3, wrongAnswer1);
         preparedStatement.setString(4, wrongAnswer2);
         preparedStatement.setString(5, wrongAnswer3);
         preparedStatement.setString(6, fileName);
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

      response.setContentType("text/plain");
      PrintWriter out = response.getWriter();
      String successMessage = "Question has been uploaded successfully!";
      out.println(successMessage);

   }
}
