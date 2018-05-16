package tools.descartes.dockerregistryui;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tools.descartes.dockerregistryui.persistence.ImageDescriptionRepository;

/**
 * Servlet implementation class ImageDescriptionServlet
 */
public class RemoveCategoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RemoveCategoryServlet() {
        super();
    }
    
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String id = request.getParameter("id");
		if (id != null && !id.isEmpty()) {
			try {
				ImageDescriptionRepository.REPOSITORY.removeCategory(Long.parseLong(id));
			} catch (NumberFormatException e) {
				
			}
		}
		response.sendRedirect("./#images");
	}
}
