package persistence

// DefaultColorImageCategory Default color for image categories.
const DefaultColorImageCategory string = "#42adf4"

// ImageCategory A non-exclusive category for image grouping.
type ImageCategory struct {
	// ID The ID.
	ID uint `gorm:"primary_key"`
	// Name The category name.
	Name string
	// Color The category color.
	Color string
	// Descriptions All images descriptions in this category.
	Descriptions []ImageDescription `gorm:"many2many:image_category_image_description;"`
}

// ImageDescription Describes an images. An image's UI counterpart.
type ImageDescription struct {
	// ID The ID.
	ID uint `gorm:"primary_key"`
	// ImageName The name of the image.
	ImageName string `gorm:"unique"`
	// Description A description.
	Description string `gorm:"type:longtext;"`
	// ExampleCommand An example command for running the image.
	ExampleCommand string `gorm:"type:longtext;"`
	// Categories Categories the image is grouped into.
	Categories []ImageCategory `gorm:"many2many:image_category_image_description;"`
}

// HelloMessage The hello message of the UI.
type HelloMessage struct {
	// ID The ID.
	ID uint `gorm:"primary_key"`
	// Message The message.
	Message string `gorm:"type:longtext;"`
}

// HasImageCategory Checks if the image description has a category.
func (imageDescription *ImageDescription) HasImageCategory(imageCategory ImageCategory) bool {
	return imageDescription.FindImageCategory(imageCategory) >= 0
}

// FindImageCategory Checks if the image description has a category and returns its index or -1 if not found.
func (imageDescription *ImageDescription) FindImageCategory(imageCategory ImageCategory) int {
	for i, category := range imageDescription.Categories {
		if category.ID == imageCategory.ID {
			return i
		}
	}
	return -1
}

// HasImageDescription Checks if the image category has a description.
func (imageCategory *ImageCategory) HasImageDescription(imageDescription ImageDescription) bool {
	return imageCategory.FindImageDescription(imageDescription) >= 0
}

// FindImageDescription Checks if the image category has a description and returns its index or -1 if not found.
func (imageCategory *ImageCategory) FindImageDescription(imageDescription ImageDescription) int {
	for i, description := range imageCategory.Descriptions {
		if description.ID == imageDescription.ID {
			return i
		}
	}
	return -1
}
