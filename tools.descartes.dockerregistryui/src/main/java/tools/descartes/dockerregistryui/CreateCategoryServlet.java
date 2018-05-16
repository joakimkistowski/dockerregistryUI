package tools.descartes.dockerregistryui;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;

import tools.descartes.dockerregistryui.persistence.ImageDescriptionRepository;

/**
 * Servlet implementation class ImageDescriptionServlet
 */
public class CreateCategoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Pattern COLOR_PATTERN = Pattern.compile("#(?:\\d|[a-f]){6}");
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateCategoryServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String name = request.getParameter("name");
		String color = request.getParameter("color");
		if (name != null) {
			ImageDescriptionRepository.REPOSITORY.createImageCategory(StringEscapeUtils.escapeHtml4(name), escapeColor(color));
		}
		response.sendRedirect("./#images");
	}

	
	private String escapeColor(String color) {
		if (color == null || color.isEmpty() || color.length() != 7) {
			return null;
		} else if (COLOR_PATTERN.matcher(color).find()) {
			return color;
		}
		return null;
	}

}
