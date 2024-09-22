import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;

public class EditQuestionServlet extends DbConnectionServlet {

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
        StringBuilder questionOptions = new StringBuilder();
        String selectedQuestionId = request.getParameter("question-id");
        String errorMessage = request.getParameter("error"); //retrieve the error message if there are any
        String questionText = "";
        String correctAnswer = "";
        String wrongAnswer1 = "";
        String wrongAnswer2 = "";
        String wrongAnswer3 = "";
        String contentPath = "";

        //display the error message, if there are any
        if (errorMessage != null && !errorMessage.isEmpty()) {
            out.println("<p style='color: red;'>" + errorMessage + "</p>");
        }

        // connect to the database and retrieve questions for the dropdown
        try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, question FROM questions")) {

            // build the dropdown options for questions
            while (rs.next()) {
                int id = rs.getInt("id");
                String question = rs.getString("question");
                questionOptions.append("<option value='").append(id).append("'")
                        .append(selectedQuestionId != null && !selectedQuestionId.isEmpty() && id == Integer.parseInt(selectedQuestionId) ? " selected" : "")
                        .append(">").append(question).append("</option>");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            out.println("<p>Error loading questions.</p>");
            return;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            out.println("<p>Invalid question ID format.</p>");
            return;
        }

        // fetch details of the selected question to prepopulate the form fields
        if (selectedQuestionId != null && !selectedQuestionId.isEmpty()) {
            try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                 PreparedStatement ps = con.prepareStatement("SELECT question, correct_answer, wrong_answer_1, wrong_answer_2, wrong_answer_3, content_path FROM questions WHERE id = ?")) {

                ps.setInt(1, Integer.parseInt(selectedQuestionId));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        questionText = rs.getString("question");
                        correctAnswer = rs.getString("correct_answer");
                        wrongAnswer1 = rs.getString("wrong_answer_1");
                        wrongAnswer2 = rs.getString("wrong_answer_2");
                        wrongAnswer3 = rs.getString("wrong_answer_3");
                        contentPath = rs.getString("content_path");
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                out.println("<p>Error loading selected question details.</p>");
            }
        }

        // render the main HTML page with the form
        out.println("<!DOCTYPE html>"
                + "<html><head><title>Edit Question</title></head>"
                + "<body><h1>Edit Question</h1><br>"
                + "<body><h2>If you leave optional fields blank, it won't show up when playing the quiz.</h2><br>"
                + "<a href='main'>Back to Main Page</a><br><br>"
                + "<form method='GET' action='edit-question'>"
                + "<label for='question-id'>Select Question:</label>"
                + "<select id='question-id' name='question-id' required onchange='this.form.submit()'>"
                + "<option value=''>Select a question</option>"
                + questionOptions.toString()
                + "</select><br>"
                + "</form>");

        // display the form to edit the selected question only if a question is selected
        if (selectedQuestionId != null && !selectedQuestionId.isEmpty()) {
            out.println("<form method='POST' action='edit-question'>"
                    + "<input type='hidden' name='question-id' value='" + selectedQuestionId + "'>"
                    + "<label for='new-question'>New Question Text:</label>"
                    + "<input type='text' id='new-question' name='new-question' maxlength='256' value='" + (questionText != null ? questionText : "") + "' required><br>"
                    + "<label for='new-correct-answer'>New Correct Answer:</label>"
                    + "<input type='text' id='new-correct-answer' name='new-correct-answer' maxlength='256' value='" + (correctAnswer != null ? correctAnswer : "") + "' required><br>"
                    + "<label for='new-wrong-answer1'>New Wrong Answer 1:</label>"
                    + "<input type='text' id='new-wrong-answer1' name='new-wrong-answer1' maxlength='256' value='" + (wrongAnswer1 != null ? wrongAnswer1 : "") + "' required><br>"
                    + "<label for='new-wrong-answer2'>New Wrong Answer 2 (Optional):</label>"
                    + "<input type='text' id='new-wrong-answer2' name='new-wrong-answer2' maxlength='256' value='" + (wrongAnswer2 != null ? wrongAnswer2 : "") + "'><br>"
                    + "<label for='new-wrong-answer3'>New Wrong Answer 3 (Optional):</label>"
                    + "<input type='text' id='new-wrong-answer3' name='new-wrong-answer3' maxlength='256' value='" + (wrongAnswer3 != null ? wrongAnswer3 : "") + "'><br>"
                    + "<label for='new-content-path'>New Content Path (Optional):</label>"
                    + "<input type='text' id='new-content-path' name='new-content-path' maxlength='256' value='" + (contentPath != null ? contentPath : "") + "'><br>"
                    + "<input type='submit' value='Update Question'><br>"
                    + "<input type='submit' formaction='delete-question' value='Delete Question'>"
                    + "</form>");
        }

        out.println("</body></html>");
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // retrieve form data
        String questionId = request.getParameter("question-id");
        String newQuestion = request.getParameter("new-question");
        String newCorrectAnswer = request.getParameter("new-correct-answer");
        String newWrongAnswer1 = request.getParameter("new-wrong-answer1");
        String newWrongAnswer2 = request.getParameter("new-wrong-answer2");
        String newWrongAnswer3 = request.getParameter("new-wrong-answer3");
        String newContentPath = request.getParameter("new-content-path");

        // validate required inputs
        if (questionId == null || questionId.isEmpty() ||
                newQuestion == null || newQuestion.isEmpty() ||
                newCorrectAnswer == null || newCorrectAnswer.isEmpty() ||
                newWrongAnswer1 == null || newWrongAnswer1.isEmpty()) {
            // redirect back to the form with an error message
            response.sendRedirect("edit-question?question-id=" + questionId
                    + "&error=Required fields cannot be left blank.");
            return;
        }

        // connect to the database to update the question
        try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE questions SET "
                             + "question = ?, "
                             + "correct_answer = ?, "
                             + "wrong_answer_1 = ?, "
                             + "wrong_answer_2 = ?, "
                             + "wrong_answer_3 = ?, "
                             + "content_path = ? "
                             + "WHERE id = ?")) {

            // set parameters for the update query
            ps.setString(1, newQuestion);
            ps.setString(2, newCorrectAnswer);
            ps.setString(3, newWrongAnswer1);
            ps.setString(4, newWrongAnswer2); // Will be empty string if left blank
            ps.setString(5, newWrongAnswer3); // Will be empty string if left blank
            ps.setString(6, newContentPath);   // Will be empty string if left blank
            ps.setInt(7, Integer.parseInt(questionId));

            //execute the update
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                response.sendRedirect("edit-success.html");
            } else {
                response.sendRedirect("edit-failure.html");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating question.");
        }
    }


}
