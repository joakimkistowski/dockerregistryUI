package persistence

import (
	"log"
	"os"
	"testing"
)

func SetupPersistenceTest(t *testing.T) *DBHandle {
	config := NewDBConfig()
	config.DBPathPrefix = os.TempDir() + "/"
	return StartPersistenceContext(config)
}

func TeardownPersistenceTest(t *testing.T, handle *DBHandle) {
	handle.StopPersistenceContext()
	if err := os.Remove(handle.Config.DBPathPrefix + handle.Config.DBName); err != nil {
		t.Error("Could not teardown. Error deleting database: ", err)
	}
}

func TestPersistence(t *testing.T) {
	handle := SetupPersistenceTest(t)
	defer TeardownPersistenceTest(t, handle)
	performPersistenceTest(t, handle)
}

func performPersistenceTest(t *testing.T, db *DBHandle) {
	//Create a description
	description := db.CreateAndPersistOrUpdateImageDescription("image0", "description0", "exampleCommand0")
	AssertTrue(t, description.ID > 0, "No description ID created")
	//Create a category
	category := db.CreateAndPersistImageCategory("category0", "#ffffff")
	AssertTrue(t, category.ID > 0, "No category ID created")
	AssertEquals(t, "#ffffff", category.Color, "Category color does not match")
	AssertEquals(t, "category0", category.Name, "Category name does not match")
	foundCategory, _ := db.FindImageCategoryByID(category.ID)
	AssertEquals(t, category.ID, foundCategory.ID, "Retreived category ID does not match original ID")
	AssertEquals(t, category.Name, foundCategory.Name, "Retreived category Name does not match original Name")
	AssertEquals(t, category.Color, foundCategory.Color, "Retreived category Color does not match original Color")
	//Retreive an Image Description
	foundDescription, _ := db.FindImageDescriptionByID(description.ID)
	AssertEquals(t, description.ID, foundDescription.ID, "Retreived description ID does not match original ID")
	AssertEquals(t, description.ImageName, foundDescription.ImageName,
		"Retreived description ImageName does not match original ImageName")
	AssertEquals(t, description.ExampleCommand, foundDescription.ExampleCommand,
		"Retreived description ExampleCommand does not match original ExampleCommand")
	AssertEquals(t, description.Description, foundDescription.Description,
		"Retreived description Description does not match original Description")
	foundDescription, _ = db.FindImageDescriptionByName(description.ImageName)
	AssertEquals(t, description.ImageName, foundDescription.ImageName,
		"Retreived description ImageName does not match original ImageName")
	AssertEquals(t, description.ExampleCommand, foundDescription.ExampleCommand,
		"Retreived description ExampleCommand does not match original ExampleCommand")
	AssertEquals(t, description.Description, foundDescription.Description,
		"Retreived description Description does not match original Description")
	//add category to description, does not have to be in memory, db only is ok
	db.AddImageCategoryToImageDescription(category.ID, description.ID)
	//reload from database
	description, _ = db.FindImageDescriptionByID(description.ID)
	category, _ = db.FindImageCategoryByID(category.ID)
	AssertEquals(t, 1, len(description.Categories), "Description does not have expected number of categories")
	AssertEquals(t, 1, len(category.Descriptions), "Category does not have expected number of descriptions")
	tmpDescr, _ := db.FindImageDescriptionByName("image0")
	AssertEquals(t, 1, len(tmpDescr.Categories),
		"Description (found by name) does not have expected number of categories")
	AssertEquals(t, category.ID, description.Categories[0].ID, "Category in association does not have correct ID")
	AssertEquals(t, description.ID, category.Descriptions[0].ID, "Description in association does not have correct ID")
	//remove category from description
	db.RemoveImageCategoryFromImageDescription(category.ID, description.ID)
	description, _ = db.FindImageDescriptionByID(description.ID)
	category, _ = db.FindImageCategoryByID(category.ID)
	AssertEquals(t, 0, len(description.Categories), "Description does not have expected number of categories")
	AssertEquals(t, 0, len(category.Descriptions), "Category does not have expected number of descriptions")
	tmpDescr, _ = db.FindImageDescriptionByName("image0")
	AssertEquals(t, 0, len(tmpDescr.Categories),
		"Description (found by name) does not have expected number of categories")
	//re-add category to description
	db.AddImageCategoryToImageDescription(category.ID, description.ID)
	//reload from database
	description, _ = db.FindImageDescriptionByID(description.ID)
	category, _ = db.FindImageCategoryByID(category.ID)
	//update description
	updatedDescription := db.CreateAndPersistOrUpdateImageDescription("image0", "description1", "exampleCommand1")
	AssertEquals(t, description.ID, updatedDescription.ID, "Updated description did not retain original ID")
	AssertEquals(t, "image0", updatedDescription.ImageName, "Updated description did not retain original ImageName")
	AssertEquals(t, "description1", updatedDescription.Description, "Did not correctly update description Description")
	AssertEquals(t, "exampleCommand1", updatedDescription.ExampleCommand,
		"Did not correctly update description ExampleCommand")
	description = updatedDescription
	//create additional description
	createdDescription := db.CreateAndPersistOrUpdateImageDescription("image1", "description2", "exampleCommand2")
	AssertTrue(t, createdDescription.ID != description.ID, "New description has conflicting, old ID")
	AssertEquals(t, "image1", createdDescription.ImageName, "Created description has wrong ImageName")
	AssertEquals(t, "description2", createdDescription.Description, "Created description has wrong Description")
	AssertEquals(t, "exampleCommand2", createdDescription.ExampleCommand,
		"Created description has wrong ExampleCommand")
	AssertEquals(t, "description1", updatedDescription.Description, "Description did not retain Description")
	AssertEquals(t, "exampleCommand1", updatedDescription.ExampleCommand, "Description did not retain ExampleCommand")
	//create a second category and add to newest description
	category2 := db.CreateAndPersistImageCategory("category1", "invalid color")
	AssertTrue(t, category2.ID > 0, "New category has invalid ID")
	AssertEquals(t, DefaultColorImageCategory, category2.Color, "New category does not have invalid color")
	db.AddImageCategoryToImageDescription(category2.ID, createdDescription.ID)
	createdDescription, _ = db.FindImageDescriptionByID(createdDescription.ID)
	category2, _ = db.FindImageCategoryByID(category2.ID)
	AssertEquals(t, 1, len(createdDescription.Categories), "Description does not have expected number of categories")
	AssertEquals(t, 1, len(category2.Descriptions), "Category does not have expected number of descriptions")
	//remove new category and check if it was removed from description
	cat2ID := category2.ID
	db.DeleteImageCategory(cat2ID)
	_, retreiveError := db.FindImageCategoryByID(cat2ID)
	AssertTrue(t, retreiveError != nil, "Getting the deleted category should result in an error")
	createdDescription, _ = db.FindImageDescriptionByID(createdDescription.ID)
	AssertEquals(t, 0, len(createdDescription.Categories),
		"Description did not update categories with category deletion")
	desID := description.ID
	db.DeleteImageDescription(desID)
	_, retreiveError = db.FindImageDescriptionByID(desID)
	AssertTrue(t, retreiveError != nil, "Getting the deleted description should result in an error")
	_, retreiveError = db.FindImageDescriptionByName("image0")
	AssertTrue(t, retreiveError != nil, "Getting the deleted description by name should result in an error")
	category, _ = db.FindImageCategoryByID(category.ID)
	AssertEquals(t, 0, len(category.Descriptions), "Category did not update descriptions with description deletion")
}

func AssertEquals(t *testing.T, expected interface{}, value interface{}, errorMessage string) {
	if expected != value {
		log.Printf(errorMessage+"; Expected: %v, Value: %v\n", expected, value)
		t.Fail()
	}
}

func AssertTrue(t *testing.T, assertion bool, errorMessage string) {
	if !assertion {
		log.Println(errorMessage)
		t.Fail()
	}
}
