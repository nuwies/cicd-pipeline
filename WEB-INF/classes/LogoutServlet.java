import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;

public class LogoutServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Set response content type

		HttpSession session = request.getSession(false);
		if (session != null && request.isRequestedSessionIdValid()) {
			session.invalidate();
		}

		response.sendRedirect("login");

	}

// Method to handle POST method request.
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Set response content type
		HttpSession session = request.getSession(false);
		if (session != null && request.isRequestedSessionIdValid()) {
			session.invalidate();
		}
		response.sendRedirect("login");

	}
}
