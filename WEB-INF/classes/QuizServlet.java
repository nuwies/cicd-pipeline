
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
public class QuizServlet {

    String dbUrl;
    String dbUsername;
    String dbPassword;

    String selectedCategory = "";

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
        Connection con;
        ResultSet result;

        String question = "";
        String correctAnswer = "";
        String wrongAnswer1 = "";
        String wrongAnswer2 = "";
        String wrongAnswer3 = "";
        String contentPath = "";

        selectedCategory = request.getParameter("category");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.out.println("Message: " + ex.getMessage());
            return;
        }

        try {
            con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            Statement stmt = con.createStatement();
            result = stmt.executeQuery("SELECT * FROM questions WHERE category='" + selectedCategory + "'");
            result.next();

            question = result.getString("Question");
            correctAnswer = result.getString("CorrectAnswer");
            wrongAnswer1 = result.getString("WrongAnswer1");
            wrongAnswer2 = result.getString("WrongAnswer2");
            wrongAnswer3 = result.getString("WrongAnswer3");
            contentPath = result.getString("ContentPath");
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
        if (wrongAnswer2.equals("")) answers.add(wrongAnswer2);
        if (wrongAnswer3.equals("")) answers.add(wrongAnswer3);
        Collections.shuffle(answers);

        

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>"
                + "<head><title>Quiz Question</title></head>"
                + "<body><h1>Category: " + selectedCategory + "</h1><br>"
                + "<p>" + question + "</p>"
                + "<iframe src='"  + contentPath + "'></iframe><br>"
                + "" 
                + "</body></html>");
    }

}
