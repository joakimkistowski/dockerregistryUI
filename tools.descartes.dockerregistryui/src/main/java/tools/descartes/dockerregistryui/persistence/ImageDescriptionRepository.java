/**
 * Copyright 2018 Joakim von Kistowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.descartes.dockerregistryui.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

/**
 * Abstract Repository for CRUD Operations for persistence Entities.
 * Already provides lookup and delete operations.
 * @author Joakim von Kistowski
 *
 */
public class ImageDescriptionRepository {
	
	private static HashMap<String, String> persistenceProperties = null;
	private static EntityManagerFactory emf = null; 
	
	public static ImageDescriptionRepository REPOSITORY = new ImageDescriptionRepository();
	
	static EntityManagerFactory getEMF() {
		if (emf == null) {
			if (persistenceProperties == null) {
				emf = Persistence.createEntityManagerFactory("tools.descartes.dockerregistryui.persistence");
			} else {
				emf = Persistence.createEntityManagerFactory("tools.descartes.dockerregistryui.persistence",
					persistenceProperties);
			}
		}
		return emf;
	}
	
	static void configureEMFWProperties(HashMap<String, String> persistenceProperties) {
		ImageDescriptionRepository.persistenceProperties = persistenceProperties;
	}	
	
	public long createOrUpdateImageDescription(String imageName, String description, String exampleCommand) {
		ImageDescription imageDescription = null;
		EntityManager em = getEMF().createEntityManager();
	    try {
	        em.getTransaction().begin();
	        imageDescription = getImageDescriptionForNameInternal(imageName, em);
	        if (imageDescription == null) {
	        	imageDescription = new ImageDescription();
	    		imageDescription.setImageName(imageName);
	    		imageDescription.setDescription(description);
	    		imageDescription.setExampleCommand(exampleCommand);
	        	em.persist(imageDescription);
	        } else {
	    		imageDescription.setImageName(imageName);
	    		imageDescription.setDescription(description);
	    		imageDescription.setExampleCommand(exampleCommand);
	        }
	        em.getTransaction().commit();
	    } finally {
	        em.close();
	    }
	    return imageDescription.getId();
	}
	
	public ImageDescription getImageDescriptionForName(String imageName) {
		EntityManager em = getEMF().createEntityManager();
		ImageDescription description = null;
	    try {
			description = getImageDescriptionForNameInternal(imageName, em);
	    } finally {
	        em.close();
	    }
	    return description;
	}
	
	//Transaction must have been started already
	private  ImageDescription getImageDescriptionForNameInternal(String imageName, EntityManager em) {
		ImageDescription description = null;
    	TypedQuery<ImageDescription> query = em.createQuery(
		"SELECT i FROM ImageDescription i WHERE i.imageName = :name", ImageDescription.class);
    	List<ImageDescription> descriptions = query.setParameter("name", imageName).getResultList();
    	if (descriptions.isEmpty()) {
    		description = null;
    	} else {
    		description = descriptions.get(0);
    	}
	    return description;
	}
	
	/**
	 * Retrieve the entity with the given ID.
	 * @param id ID of the entity to retrieve.
	 * @return The entity. Null, if none was found.
	 */
	public ImageDescription getImageDescriptionById(long id) {
		ImageDescription instance = null;
		EntityManager em = getEMF().createEntityManager();
	    try {
	        instance = em.find(ImageDescription.class, id);
	    } finally {
	        em.close();
	    }
		return instance;
	}
	
	/**
	 * Removes the entity with the id from database.
	 * @param id The id of the entity to remove.
	 * @return True, if delete succeded. False, if it failed (entity with id not found).
	 */
	public boolean removeDescription(long id) {
		boolean found = false;
		EntityManager em = getEMF().createEntityManager();
	    try {
	        em.getTransaction().begin();
	        ImageDescription entity = em.find(ImageDescription.class, id);
	        if (entity != null) {
	        	for (ImageCategory category : entity.getCategories()) {
	        		category.getDescriptions().remove(entity);
	        	}
	        	em.remove(entity);
	        	found = true;
	        }
	        em.getTransaction().commit();
	    } finally {
	        em.close();
	    }
	    return found;
	}
	
	/**
	 * Retrieve the entity with the given ID.
	 * @param id ID of the entity to retrieve.
	 * @return The entity. Null, if none was found.
	 */
	public ImageCategory getCategoryById(long id) {
		ImageCategory instance = null;
		EntityManager em = getEMF().createEntityManager();
	    try {
	        instance = em.find(ImageCategory.class, id);
	    } finally {
	        em.close();
	    }
		return instance;
	}
	
	public long createImageCategory(String name, String color) {
		ImageCategory imageCategory = new ImageCategory();
		imageCategory.setName(name);
		if (color == null || color.isEmpty() || !color.startsWith("#")) {
			imageCategory.setColor(ImageCategory.DEFAULT_COLOR);
		} else {
			imageCategory.setColor(color);
		}
		EntityManager em = getEMF().createEntityManager();
	    try {
	        em.getTransaction().begin();
        	em.persist(imageCategory);
	        em.getTransaction().commit();
	    } finally {
	        em.close();
	    }
	    return imageCategory.getId();
	}
	
	public void addCategoryToDescription(long categoryId, long descriptionId) {
		ImageCategory category = null;
		ImageDescription description = null;
		EntityManager em = getEMF().createEntityManager();
	    try {
	    	em.getTransaction().begin();
	        category = em.find(ImageCategory.class, categoryId);
	        description = em.find(ImageDescription.class, descriptionId);
	        if (category != null && description != null) {
	        	if (!category.getDescriptions().contains(description)) {
	        		category.getDescriptions().add(description);
	        		description.getCategories().add(category);
	        	}
	        }
	        em.getTransaction().commit();
	    } finally {
	        em.close();
	    }
	}
	
	public void removeCategoryFromDescription(long categoryId, long descriptionId) {
		ImageCategory category = null;
		ImageDescription description = null;
		EntityManager em = getEMF().createEntityManager();
	    try {
	    	em.getTransaction().begin();
	        category = em.find(ImageCategory.class, categoryId);
	        description = em.find(ImageDescription.class, descriptionId);
	        if (category != null && description != null) {
	        	if (category.getDescriptions().contains(description)) {
	        		category.getDescriptions().remove(description);
	        		description.getCategories().remove(category);
	        	}
	        }
	        em.getTransaction().commit();
	    } finally {
	        em.close();
	    }
	}
	
	public List<ImageCategory> getCategories() {
		List<ImageCategory> categories = null;
		EntityManager em = getEMF().createEntityManager();
		try {
	    	TypedQuery<ImageCategory> query = em.createQuery(
			"SELECT c FROM ImageCategory c", ImageCategory.class);
	    	categories = query.getResultList();
	    } finally {
	        em.close();
	    }
		if (categories == null) {
			return new ArrayList<>();
		}
		return categories;
	}
	
	/**
	 * Removes the entity with the id from database.
	 * @param id The id of the entity to remove.
	 * @return True, if delete succeded. False, if it failed (entity with id not found).
	 */
	public boolean removeCategory(long id) {
		boolean found = false;
		EntityManager em = getEMF().createEntityManager();
	    try {
	        em.getTransaction().begin();
	        ImageCategory entity = em.find(ImageCategory.class, id);
	        if (entity != null) {
	        	em.remove(entity);
	        	found = true;
	        	getEMF().getCache().evict(ImageDescription.class);
	        }
	        em.getTransaction().commit();
	    } finally {
	        em.close();
	    }
	    return found;
	}
}
