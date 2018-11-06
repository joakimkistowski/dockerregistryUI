package persistence

import (
	"errors"
	"log"
	"strings"
)

/*CreateAndPersistImageCategory Creates a new image category and persists it. */
func (handle *DBHandle) CreateAndPersistImageCategory(name string, color string) *ImageCategory {
	category := ImageCategory{Name: name}
	if len(color) == 0 || !strings.HasPrefix(color, "#") {
		category.Color = DefaultColorImageCategory
	} else {
		category.Color = color
	}
	handle.db.Create(&category)
	return &category
}

/*CreateAndPersistOrUpdateImageDescription Creates an image description or updates one if one with the name exists. */
func (handle *DBHandle) CreateAndPersistOrUpdateImageDescription(
	name string, description string, exampleCommand string) *ImageDescription {
	imageDescription := ImageDescription{ImageName: name, Description: description, ExampleCommand: exampleCommand}
	tx := handle.db.Begin()
	if tx.First(&imageDescription, "image_name = ?", name).RecordNotFound() {
		tx.Create(&imageDescription)
	} else {
		imageDescription.Description = description
		imageDescription.ExampleCommand = exampleCommand
		tx.Save(&imageDescription)
	}
	tx.Commit()
	return &imageDescription
}

/*FindImageDescriptionByName Find an image description using its name. */
func (handle *DBHandle) FindImageDescriptionByName(name string) (*ImageDescription, error) {
	imageDescription := ImageDescription{}
	retreived, err := handle.retreiveWithAttribute("image_name", name, &imageDescription, "Categories")
	return retreived.(*ImageDescription), err
}

/*FindImageDescriptionByID Find an image description using its ID. */
func (handle *DBHandle) FindImageDescriptionByID(id uint) (*ImageDescription, error) {
	imageDescription := ImageDescription{}
	retreived, err := handle.retreiveWithAttribute("id", id, &imageDescription, "Categories")
	return retreived.(*ImageDescription), err
}

/*FindImageCategoryByID Find an image category using its ID. */
func (handle *DBHandle) FindImageCategoryByID(id uint) (*ImageCategory, error) {
	category := ImageCategory{}
	retreived, err := handle.retreiveWithAttribute("id", id, &category, "Descriptions")
	return retreived.(*ImageCategory), err
}

func (handle *DBHandle) retreiveWithAttribute(
	attributeName string, attribute interface{}, entity interface{}, optionalPreload ...string) (interface{}, error) {
	if len(optionalPreload) > 0 {
		if handle.db.Preload(optionalPreload[0]).First(entity, attributeName+" = ?", attribute).RecordNotFound() {
			return entity, errors.New("Entity not found")
		}
	} else {
		if handle.db.First(entity, attributeName+" = ?", attribute).RecordNotFound() {
			return entity, errors.New("Entity not found")
		}
	}
	return entity, nil
}

/*FindAllImageCategories Finds and returns all image categories. */
func (handle *DBHandle) FindAllImageCategories() *[]ImageCategory {
	var imageCategories []ImageCategory
	handle.db.Find(&imageCategories)
	return &imageCategories
}

/*DeleteImageDescription Deletes an image description if it exists. */
func (handle *DBHandle) DeleteImageDescription(id uint) bool {
	return handle.deleteWithID(id, ImageDescription{})
}

/*DeleteImageCategory Deletes an image description if it exists. */
func (handle *DBHandle) DeleteImageCategory(id uint) bool {
	return handle.deleteWithID(id, ImageCategory{})
}

func (handle *DBHandle) deleteWithID(id uint, placeholder interface{}) bool {
	if id > 0 {
		err := handle.db.Where("id = ?", id).Delete(placeholder)
		return err == nil
	}
	return false
}

/*AddImageCategoryToImageDescription Adds the image category to the image description. */
func (handle *DBHandle) AddImageCategoryToImageDescription(categoryID uint, descriptionID uint) {
	imageDescription := ImageDescription{}
	imageCategory := ImageCategory{}
	tx := handle.db.Begin()
	if !tx.First(&imageDescription, "ID = ?", descriptionID).RecordNotFound() &&
		!tx.First(&imageCategory, "ID = ?", categoryID).RecordNotFound() {
		if err := tx.Model(&imageDescription).Association("Categories").Append(&imageCategory).Error; err != nil {
			tx.Rollback()
			log.Fatalf("Could not add to association: %s\n", err)
			return
		}
	}
	tx.Commit()
}

/*RemoveImageCategoryFromImageDescription Removes the image category from the image description. */
func (handle *DBHandle) RemoveImageCategoryFromImageDescription(categoryID uint, descriptionID uint) {
	imageDescription := ImageDescription{}
	imageCategory := ImageCategory{}
	tx := handle.db.Begin()
	if !tx.Where("id = ?", descriptionID).First(&imageDescription).RecordNotFound() &&
		!tx.Where("id = ?", descriptionID).First(&imageCategory).RecordNotFound() {
		if err := tx.Model(&imageDescription).Association("Categories").Delete(&imageCategory).Error; err != nil {
			tx.Rollback()
			log.Fatalf("Could not remove from association: %s\n", err)
			return
		}
	}
	tx.Commit()
}
