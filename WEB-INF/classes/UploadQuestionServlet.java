
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import org.json.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import netscape.javascript.JSException;

@MultipartConfig
public class UploadQuestionServlet extends DbConnectionServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    if (session == null) {
      response.setStatus(HttpServletResponse.SC_FOUND);
      response.sendRedirect("login");
      return;
    }

    String username = (String) session.getAttribute("username");
    String userType = null;

    try {
      userType = repository.getUserType(username);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (!"admin".equalsIgnoreCase(userType)) {
      response.sendRedirect("main");
      return;
    }

    // JSON
    JSONObject responseJSON = new JSONObject();
    JSONArray categoriesJSON = new JSONArray();

    try {
      List<Map<String, Object>> categories = repository.select("SELECT * FROM categories");
      for (Map<String, Object> category : categories) {
        JSONObject currCategory = new JSONObject();
        currCategory.put("id", category.get("id"));
        currCategory.put("category", category.get("name"));
        categoriesJSON.put(currCategory);
      }

      if (categories.isEmpty()) {
        response.sendRedirect("no-categories.html");
      }
    } catch (Exception ex) {
      System.out.println("Message: " + ex.getMessage());
    }

    responseJSON.put("categories", categoriesJSON);
    response.setContentType("application/json");
    response.getWriter().println(responseJSON);
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
    if (!fileName.trim().isEmpty()) {
      filePath = System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/media/" + fileName;
    }

    try {
      repository.insert(
          "INSERT INTO questions (category, question, correct_answer, wrong_answer_1, wrong_answer_2, wrong_answer_3, content_path) VALUES (?, ?, ?, ?, ?, ?, ?)",
          Integer.valueOf(categoryID), question, correctAnswer, wrongAnswer1, wrongAnswer2, wrongAnswer3,
          !fileName.trim().isEmpty() ? "media/" + fileName : fileName);
    } catch (Exception ex) {
      System.out.println("Uploading error!");
      System.out.println("Message: " + ex.getMessage());
    }

    try {
      filePart.write(filePath);
    } catch (Exception e) {
      System.out.println("No file was selected!");
    }

    response.sendRedirect("upload-success.html");
  }
}
