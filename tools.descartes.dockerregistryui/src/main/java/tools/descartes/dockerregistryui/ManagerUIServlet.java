package tools.descartes.dockerregistryui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tools.descartes.dockerregistryui.persistence.ImageCategory;
import tools.descartes.dockerregistryui.persistence.ImageDescriptionRepository;
import tools.descartes.dockerregistryui.persistence.ImageInfoContainer;
import tools.descartes.dockerregistryui.util.RESTClient;
import tools.descartes.dockerregistryui.util.RegistryUISettings;

/**
 * Servlet implementation class ManagerUIServlet
 */
public class ManagerUIServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ManagerUIServlet() {
        super();
    }
    
    private List<ImageInfoContainer> retreiveImageInfos() {
    	List<String> images = RESTClient.CLIENT.getReponseAsJsonListWithName("v2/_catalog", "repositories");
    	if (images == null) {
    		return new ArrayList<>();
    	}
    	List<ImageInfoContainer> imageInfos = new ArrayList<>(images.size());
    	for (String imageName : images) {
    		imageInfos.add(ImageInfoContainer.fromDatabase(imageName,
    				RESTClient.CLIENT.getReponseAsJsonListWithName("v2/" + imageName + "/tags/list", "tags")));
    	}
    	imageInfos.sort((c0, c1) -> c0.getImageName().compareToIgnoreCase(c1.getImageName()));
    	return imageInfos;
    }
    
    private List<ImageInfoContainer> filterForCategory(List<ImageInfoContainer> infos, long categoryId) {
    	if (categoryId <= 0) {
    		return infos;
    	}
    	ImageCategory category = ImageDescriptionRepository.REPOSITORY.getCategoryById(categoryId);
    	if (category == null) {
    		return infos;
    	}
    	List<ImageInfoContainer> filtered = new LinkedList<>();
    	for (ImageInfoContainer info : infos) {
    		if (info.getDescription() != null && info.getDescription().getCategories() != null
    				&& info.getDescription().getCategories().contains(category)) {
    			filtered.add(info);
    		}
    	}
    	return filtered;
    }
    
    public List<ImageCategory> retreiveCategories() {
    	List<ImageCategory> categories = ImageDescriptionRepository.REPOSITORY.getCategories();
    	categories.sort(Comparator.comparing(ImageCategory::getName));
    	return categories;
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String filteredCategory = request.getParameter("category");
		long categoryId = -1;
		if (filteredCategory != null && !filteredCategory.isEmpty()) {
			try {
				categoryId = Long.parseLong(filteredCategory);
			} catch (NumberFormatException e) {
				
			}
		}
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		request.setAttribute("registryHost", RegistryUISettings.SETTINGS.getRegistryHost());
		request.setAttribute("images", filterForCategory(retreiveImageInfos(), categoryId));
		request.setAttribute("categories", retreiveCategories());
		request.getRequestDispatcher("/WEB-INF/pages/managerui.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
