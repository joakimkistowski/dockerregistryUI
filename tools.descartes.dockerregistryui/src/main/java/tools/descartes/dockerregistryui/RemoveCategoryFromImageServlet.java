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
public class RemoveCategoryFromImageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RemoveCategoryFromImageServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String category = request.getParameter("category");
		String image = request.getParameter("image");
		if (category != null && image != null) {
			try {
				ImageDescriptionRepository.REPOSITORY.removeCategoryFromDescription(Long.parseLong(category), Long.parseLong(image));
			} catch (NumberFormatException e) {
				
			}
			
		}
		response.sendRedirect("./#images");
	}

}
