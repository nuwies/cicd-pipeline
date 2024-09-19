
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.sql.*;
import java.util.Properties;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

// Go to quiz page with category paramter - GET /quiz
// Redirect to quiz.html with category URL parameter
// quiz.html after loading page will get question values and update html - POST /quiz?
public class QuizServlet extends DbConnectionServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection con;
        ResultSet result;
        HttpSession session = request.getSession();

        String selectedCategory = "";
        String question = "";
        String correctAnswer = "";
        String wrongAnswer1 = "";
        String wrongAnswer2 = "";
        String wrongAnswer3 = "";
        String contentPath = "";

        Integer categoryID = (Integer) session.getAttribute("selectedCategory");
        Integer questionNumber = (Integer) session.getAttribute("questionNumber");

        if (categoryID == null) {
            categoryID = Integer.valueOf(request.getParameter("category"));
        }
        session.setAttribute("selectedCategory", categoryID);

        if (questionNumber == null) {
            questionNumber = 1;
        }

        session.setAttribute("questionNumber", questionNumber);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.out.println("Message: " + ex.getMessage());
            return;
        }

        try {
            con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            Statement stmt = con.createStatement();
            result = stmt.executeQuery("SELECT * FROM questions WHERE category='" + categoryID + "'");

            for (int i = 0; i < questionNumber; i++) {
                boolean rowExists = result.next();
                if(!rowExists && questionNumber == 1) response.sendRedirect("no-questions.html");
                else if (!rowExists && questionNumber > 1) response.sendRedirect("end-of-quiz.html");
            }

            String questionID = result.getString("id");
            question = result.getString("question");
            correctAnswer = result.getString("correct_answer");
            wrongAnswer1 = result.getString("wrong_answer_1");
            wrongAnswer2 = result.getString("wrong_answer_2");
            wrongAnswer3 = result.getString("wrong_answer_3");
            contentPath = result.getString("content_path");

            session.setAttribute("questionID", questionID);

            result = stmt.executeQuery("SELECT name FROM categories WHERE id=" + categoryID);
            result.next();
            selectedCategory = result.getString("name");
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Message: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("ErrorCode: " + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println("");
            }
        }

        ArrayList<String> answers = new ArrayList<>();
        answers.add(correctAnswer);
        answers.add(wrongAnswer1);
        if (!wrongAnswer2.equals("")) {
            answers.add(wrongAnswer2);
        }
        if (!wrongAnswer3.equals("")) {
            answers.add(wrongAnswer3);
        }
        Collections.shuffle(answers);

        String answersHTML = "<form action='quiz' method='post'>";

        for (String answer : answers) {
            answersHTML += "<input type='radio' name='answer' value='" + answer + "'/>" + answer + "<br>";
        }

        answersHTML += "<button type='submit'>Submit</form>";

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>"
                + "<head><title>Quiz Question</title>"
                + "<script src='scripts/quiz.js'></script></head>"
                + "<body><h1>Category: " + selectedCategory + "</h1><br>"
                + "<p>" + question + "</p>"
                + "<iframe src='" + contentPath + "'></iframe><br>"
                + answersHTML
                + "</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        System.out.println(session.getAttribute("questionID"));
        String questionID = (String) session.getAttribute("questionID");
        String answer = request.getParameter("answer");
        String correctAnswer = "";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.out.println("Message: " + ex.getMessage());
            return;
        }

        try {
            Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            Statement stmt = con.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM questions WHERE id =" + questionID);
            result.next();
            correctAnswer = result.getString("correct_answer");
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Message: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("ErrorCode: " + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println("");
            }
        }

        Integer questionNumber = (Integer) session.getAttribute("questionNumber");

        if (answer.equals(correctAnswer)) {
            questionNumber++;
            session.setAttribute("questionNumber", questionNumber);
            response.sendRedirect("quiz");
        } else {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<form method='get' action='quiz'><button type='submit'>Try Again</button></form>"
                    + "<script type='text/javascript'>"
                    + "alert('Incorrect!');"
                    + "</script>"
                    );
        }

    }
}
