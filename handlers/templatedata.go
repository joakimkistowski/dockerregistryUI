package handlers

import (
	"dockerregistryUI/persistence"
	"dockerregistryUI/utils"
	"html/template"
)

var templates = template.Must(template.ParseFiles("templates/index.html"))

/*UITemplateData Data passed to the HTML template. */
type UITemplateData struct {
	Settings         utils.DockerRegistryUISettings
	HelloMessage     string
	Categories       []persistence.ImageCategory
	Images           []ImageData
	filterCategoryID uint
}

/*ImageData Data about an image, collected from registry and database. */
type ImageData struct {
	Name            string
	Tags            []string
	Description     *persistence.ImageDescription
	OtherCategories []persistence.ImageCategory
}

/*UITemplateCache Caches the template's data until the next database change.*/
type UITemplateCache interface {
	Cache(data *UITemplateData)
	Flush()
	GetCached() (*UITemplateData, bool)
}

/*InMemoryUITemplateCache Simple cache that holds a template's data in local memory until flushed. */
type inMemoryUITemplateCache struct {
	cached     *UITemplateData
	cleanCache bool
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
		Settings:         settings,
		HelloMessage:     "temporary hello",
		Categories:       handle.FindAllImageCategories(),
		filterCategoryID: 0,
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

/*FilterImages Adds a temporary filter to the template so that only images with the provided categoryID are shown. */
func (data *UITemplateData) FilterImages(categoryID uint) {
	data.filterCategoryID = categoryID
}

/*ImageMatchesCurrentFilter Checks if the image should be displayed considering current filter settings. */
func (data UITemplateData) ImageMatchesCurrentFilter(image ImageData) bool {
	//all images are displayed with no filter
	if data.filterCategoryID == 0 {
		return true
	}
	for _, category := range image.Description.Categories {
		if category.ID == data.filterCategoryID {
			return true
		}
	}
	return false
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

/*Cache Caches the current UI template data in local memory. */
func (cache *inMemoryUITemplateCache) Cache(data *UITemplateData) {
	cache.cached = data
	cache.cleanCache = true
}

/*Flush Flushes the current UI template data from local memory. */
func (cache *inMemoryUITemplateCache) Flush() {
	cache.cleanCache = false
}

/*GetCached Returns the cached element and a bool indicating if the cached element is clean (true if it is clean). */
func (cache *inMemoryUITemplateCache) GetCached() (*UITemplateData, bool) {
	return cache.cached, cache.cleanCache
}

func newInMemoryUITemplateCache() *inMemoryUITemplateCache {
	return &inMemoryUITemplateCache{
		cached:     &UITemplateData{},
		cleanCache: false,
	}
}
