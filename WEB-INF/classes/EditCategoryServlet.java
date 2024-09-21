import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;

public class EditCategoryServlet extends DbConnectionServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        StringBuilder categoryOptions = new StringBuilder();
        String selectedCategoryId = request.getParameter("category-id");
        String existingCategoryName = "";
        String existingContentPath = "";

        // connect to the database and retrieve categories
        try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM categories")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                categoryOptions.append("<option value='").append(id).append("'")
                        .append(selectedCategoryId != null && selectedCategoryId.equals(String.valueOf(id)) ? " selected" : "")
                        .append(">").append(name).append("</option>");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            out.println("<p>Error loading categories.</p>");
            return;
        }

        // fetch details of the selected category to prepopulate the form fields
        if (selectedCategoryId != null && !selectedCategoryId.isEmpty()) {
            try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                 PreparedStatement ps = con.prepareStatement("SELECT name, content_path FROM categories WHERE id = ?")) {

                ps.setInt(1, Integer.parseInt(selectedCategoryId));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        existingCategoryName = rs.getString("name");
                        existingContentPath = rs.getString("content_path");
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                out.println("<p>Error loading selected category details.</p>");
            }
        }

        // HTML form for editing a category
        out.println("<!DOCTYPE html>"
                + "<html><head><title>Edit Category</title></head>"
                + "<body><h1>Edit Category</h1><br>"
                + "<form method='GET' action='edit-category'>"
                + "<label for='category-id'>Select Category:</label>"
                + "<select id='category-id' name='category-id' required onchange='this.form.submit()'>"
                + "<option value=''>Select a category</option>"
                + categoryOptions.toString()
                + "</select><br>"
                + "</form>");

        // display the editing form only if a category is selected
        if (selectedCategoryId != null && !selectedCategoryId.isEmpty()) {
            out.println("<form method='POST' action='edit-category'>"
                    + "<input type='hidden' name='category-id' value='" + selectedCategoryId + "'>"
                    + "<label for='new-category-name'>New Category Name:</label>"
                    + "<input type='text' id='new-category-name' name='new-category-name' value='" + existingCategoryName + "' required><br>"
                    + "<label for='new-content-path'>New Content Path: (Optional)</label>"
                    + "<input type='text' id='new-content-path' name='new-content-path' value='" + existingContentPath + "'><br>"
                    + "<input type='submit' value='Update Category'>"
                    + "</form>");
        }

        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // retrieve form data
        String categoryId = request.getParameter("category-id");
        String newCategoryName = request.getParameter("new-category-name");
        String newContentPath = request.getParameter("new-content-path");

        // validate inputs
        if (categoryId == null || categoryId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Category ID is required.");
            return;
        }

        //connect to the database to update the category
        try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE categories SET name = ?, content_path = ? WHERE id = ?")) {

            //set parameters for the update query
            ps.setString(1, newCategoryName); // Always update to new name
            ps.setString(2, newContentPath.isEmpty() ? "" : newContentPath); // Update to empty string if blank
            ps.setInt(3, Integer.parseInt(categoryId));

            //execute the update
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                response.sendRedirect("edit-success.html");
            } else {
                response.sendRedirect("edit-failure.html");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating category.");
        }
    }
}
