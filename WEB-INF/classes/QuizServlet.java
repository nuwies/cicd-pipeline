
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.json.*;

public class QuizServlet extends DbConnectionServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // check if user is logged in
    HttpSession session = request.getSession(false);
    if (session == null) {
      response.setStatus(HttpServletResponse.SC_FOUND);
      response.sendRedirect("login");
      return;
    }

    JSONObject responseJSON = new JSONObject();
    JSONArray questionsJSON = new JSONArray();

    Integer categoryID = Integer.valueOf(request.getParameter("category"));

    try {
      List<Map<String, Object>> questions = repository.select("SELECT * FROM questions WHERE category = ?", categoryID);

      for (Map<String, Object> question : questions) {
        // Get answers from the question map and shuffle them
        List<String> answers = new ArrayList<>();
        answers.add((String) question.get("correct_answer"));
        answers.add((String) question.get("wrong_answer_1"));
        String wrongAnswer2 = (String) question.get("wrong_answer_2");
        if (wrongAnswer2 != null && !wrongAnswer2.isEmpty()) {
          answers.add(wrongAnswer2);
        }
        String wrongAnswer3 = (String) question.get("wrong_answer_3");
        if (wrongAnswer3 != null && !wrongAnswer3.isEmpty()) {
          answers.add(wrongAnswer3);
        }
        Collections.shuffle(answers);

        JSONObject currentQuestion = new JSONObject();
        currentQuestion.put("id", question.get("id"));
        currentQuestion.put("question", question.get("question"));
        currentQuestion.put("content_path", question.get("content_path"));
        JSONArray currentAnswers = new JSONArray();
        answers.forEach((n) -> currentAnswers.put(n));
        currentQuestion.put("answers", currentAnswers);
        questionsJSON.put(currentQuestion);
      }
    } catch (SQLException ex) {
      System.out.println("Error fetching questions: " + ex.getMessage());
    }

    responseJSON.put("questions", questionsJSON);
    response.setContentType("application/json");
    response.getWriter().println(responseJSON);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession();
    String questionID = request.getParameter("questionID");
    String answer = request.getParameter("answer");
    String correctAnswer = "";

    JSONObject responseJSON = new JSONObject();

    try {
      List<Map<String, Object>> results = repository.select("SELECT correct_answer FROM questions WHERE id = ?",
          questionID);
      if (!results.isEmpty()) {
        correctAnswer = (String) results.get(0).get("correct_answer");
      }
    } catch (SQLException ex) {
      System.out.println("Error fetching correct answer: " + ex.getMessage());
    }

    responseJSON.put("correct_answer_given", answer.equals(correctAnswer));
    responseJSON.put("correct_answer", correctAnswer);
    response.setContentType("application/json");
    response.getWriter().println(responseJSON);
  }
}
