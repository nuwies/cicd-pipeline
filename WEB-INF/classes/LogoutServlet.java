import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
