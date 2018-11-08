package handlers

import (
	"dockerregistryUI/persistence"
	"dockerregistryUI/utils"
	"html/template"
)

var templates = template.Must(template.ParseFiles("templates/index.html"))

/*UITemplateData Data passed to the HTML template. */
type UITemplateData struct {
	Settings     utils.DockerRegistryUISettings
	HelloMessage string
	Categories   []persistence.ImageCategory
	Images       []ImageData
}

/*ImageData Data about an image, collected from registry and database. */
type ImageData struct {
	Name            string
	Tags            []string
	Description     *persistence.ImageDescription
	OtherCategories []persistence.ImageCategory
}

/*MergeImageData Merges image data retreived from the registry with database data for use in the template. */
func MergeImageData(image utils.RegistryImage, description *persistence.ImageDescription) ImageData {
	var data ImageData
	data.Name = image.ImageName
	data.Tags = image.ImageTags
	data.Description = description
	return data
}

/*InitializeUITemplateData Initializes the data for a UI template, fetching it from registry and db and merging. */
func InitializeUITemplateData(settings utils.DockerRegistryUISettings,
	handle *persistence.DBHandle, client *utils.RegistryHTTPClient) UITemplateData {
	data := UITemplateData{
		Settings:     settings,
		HelloMessage: "temporary hello",
		Categories:   handle.FindAllImageCategories(),
	}
	registryImages := client.RetreiveRegistryImages()
	for _, registryImage := range registryImages {
		var imageData ImageData
		if description, err := handle.FindImageDescriptionByName(registryImage.ImageName); err == nil {
			imageData = MergeImageData(registryImage, description)

		} else {
			imageData = MergeImageData(registryImage, &persistence.ImageDescription{})
		}
		imageData.populateOtherCategories(data.Categories)
		data.Images = append(data.Images, imageData)
	}
	return data
}

func (imageData *ImageData) populateOtherCategories(allCategories []persistence.ImageCategory) {
	imageData.OtherCategories = imageData.OtherCategories[:0]
	for _, cat := range allCategories {
		if !containsCategory(imageData.Description.Categories, cat) {
			imageData.OtherCategories = append(imageData.OtherCategories, cat)
		}
	}
}

func containsCategory(categories []persistence.ImageCategory, category persistence.ImageCategory) bool {
	for _, cat := range categories {
		if cat.ID == category.ID {
			return true
		}
	}
	return false
}
