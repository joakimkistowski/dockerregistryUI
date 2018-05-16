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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ImageDescriptionRepositoryTest {
	
	/**
	 * Setup the test.
	 * @throws Throwable Throws uncaught throwables for test to fail.
	 */
	@Before
	public void setup() throws Throwable {
		PersistenceTestConfigurator.configureEMF();
	}
	
	@Test
	public void performTest() {
		//TODO: Test categories
		//Create a description
		long id0 = ImageDescriptionRepository.REPOSITORY.createOrUpdateImageDescription("image0", "description0", "exampleCommand0");
		Assert.assertTrue(id0 > 0);
		//Create a category
		long catId0 = ImageDescriptionRepository.REPOSITORY.createImageCategory("category0", "#ffffff");
		Assert.assertTrue(catId0 > 0);
		Assert.assertEquals(ImageDescriptionRepository.REPOSITORY.getCategoryById(catId0).getColor(), "#ffffff");
		Assert.assertEquals(ImageDescriptionRepository.REPOSITORY.getCategoryById(catId0).getName(), "category0");
		//add category to description
		ImageDescriptionRepository.REPOSITORY.addCategoryToDescription(catId0, id0);
		Assert.assertEquals(1, ImageDescriptionRepository.REPOSITORY.getCategoryById(catId0).getDescriptions().size());
		Assert.assertEquals(id0, ImageDescriptionRepository.REPOSITORY.getCategoryById(catId0).getDescriptions().get(0).getId());
		Assert.assertEquals(1, ImageDescriptionRepository.REPOSITORY.getImageDescriptionById(id0).getCategories().size());
		Assert.assertEquals(catId0, ImageDescriptionRepository.REPOSITORY.getImageDescriptionById(id0).getCategories().get(0).getId());
		//remove category from description
		ImageDescriptionRepository.REPOSITORY.removeCategoryFromDescription(catId0, id0);
		Assert.assertEquals(0, ImageDescriptionRepository.REPOSITORY.getCategoryById(catId0).getDescriptions().size());
		Assert.assertEquals(0, ImageDescriptionRepository.REPOSITORY.getImageDescriptionById(id0).getCategories().size());
		//re-add category to description
		ImageDescriptionRepository.REPOSITORY.addCategoryToDescription(catId0, id0);
		
		Assert.assertEquals("exampleCommand0", ImageDescriptionRepository.REPOSITORY.getImageDescriptionById(id0).getExampleCommand());
		long id1 = ImageDescriptionRepository.REPOSITORY.createOrUpdateImageDescription("image0", "description1", "exampleCommand1");
		Assert.assertEquals(id1, id0);
		Assert.assertEquals("image0", ImageDescriptionRepository.REPOSITORY.getImageDescriptionById(id1).getImageName());
		long id2 = ImageDescriptionRepository.REPOSITORY.createOrUpdateImageDescription("image1", "description2", "exampleCommand2");
		Assert.assertTrue(id2 > id1);
		Assert.assertEquals("description2", ImageDescriptionRepository.REPOSITORY.getImageDescriptionById(id2).getDescription());
		Assert.assertEquals("description1", ImageDescriptionRepository.REPOSITORY.getImageDescriptionById(id1).getDescription());
		Assert.assertEquals("exampleCommand1", ImageDescriptionRepository.REPOSITORY.getImageDescriptionForName("image0").getExampleCommand());
		Assert.assertEquals("image1", ImageDescriptionRepository.REPOSITORY.getImageDescriptionForName("image1").getImageName());
		//create a second category and add to newest description
		long catId1 = ImageDescriptionRepository.REPOSITORY.createImageCategory("category1", "invalid color");
		Assert.assertTrue(catId1 > 0);
		Assert.assertEquals(ImageDescriptionRepository.REPOSITORY.getCategoryById(catId1).getColor(), ImageCategory.DEFAULT_COLOR);
		ImageDescriptionRepository.REPOSITORY.addCategoryToDescription(catId1, id2);
		Assert.assertEquals(1, ImageDescriptionRepository.REPOSITORY.getImageDescriptionById(id2).getCategories().size());
		//remove new category and check if it was removed from description
		Assert.assertTrue(ImageDescriptionRepository.REPOSITORY.removeCategory(catId1));
		Assert.assertNull(ImageDescriptionRepository.REPOSITORY.getCategoryById(catId1));
		Assert.assertEquals(0, ImageDescriptionRepository.REPOSITORY.getImageDescriptionById(id2).getCategories().size());
		
		//remove initial description and check that category doesn't have description in its list anymore
		Assert.assertTrue(ImageDescriptionRepository.REPOSITORY.removeDescription(id1));
		Assert.assertNull(ImageDescriptionRepository.REPOSITORY.getImageDescriptionById(id1));
		Assert.assertEquals(0, ImageDescriptionRepository.REPOSITORY.getCategoryById(catId0).getDescriptions().size());
		
	}
	
}
