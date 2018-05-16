package tools.descartes.dockerregistryui;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;

import tools.descartes.dockerregistryui.persistence.ImageDescriptionRepository;

/**
 * Servlet implementation class ImageDescriptionServlet
 */
public class ImageDescriptionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImageDescriptionServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String imageName = request.getParameter("imageName");
		String description = request.getParameter("description");
		String exampleCommand = request.getParameter("exampleCommand");
		if (imageName != null && !imageName.isEmpty() && description != null && exampleCommand != null) {
			ImageDescriptionRepository.REPOSITORY.createOrUpdateImageDescription(
					StringEscapeUtils.escapeHtml4(imageName), StringEscapeUtils.escapeHtml4(description), StringEscapeUtils.escapeHtml4(exampleCommand));
		}
		response.sendRedirect("./#images");
	}

}
