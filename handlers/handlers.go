package handlers

import (
	"dockerregistryUI/persistence"
	"dockerregistryUI/utils"
	"log"
	"net/http"
	"regexp"
	"strconv"
)

var validColor = regexp.MustCompile(`#(?:\d|[a-f]){6}`)

/*HandlerContext A context for the handlers. */
type HandlerContext struct {
	initialized bool
	settings    utils.DockerRegistryUISettings
	client      *utils.RegistryHTTPClient
	db          *persistence.DBHandle
}

/*New Initializes a new HandlerContext. */
func New(settings utils.DockerRegistryUISettings, client *utils.RegistryHTTPClient,
	db *persistence.DBHandle) *HandlerContext {
	return &HandlerContext{initialized: true, settings: settings, client: client, db: db}
}

/*IndexHandler Handles requests to the main index page. */
func (context *HandlerContext) IndexHandler(w http.ResponseWriter, r *http.Request) {
	templateData := InitializeUITemplateData(context.settings, context.db, context.client)
	categoryQuery := r.URL.Query().Get("category")
	if categoryID, err := strconv.ParseUint(categoryQuery, 10, 64); err == nil {
		templateData.FilterImages(uint(categoryID))
	}
	setCommonHeaders(w)
	err := templates.ExecuteTemplate(w, "index.html", templateData)
	if err != nil {
		log.Printf("Error rendering template: %s\n", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
	}
}

/*CreateCategoryHandler Creates a new category from a form using "name" and "color". */
func (context *HandlerContext) CreateCategoryHandler(w http.ResponseWriter, r *http.Request) {
	if checkForPostWithError(w, r) {
		return
	}
	r.ParseForm()
	name := r.PostFormValue("name")
	color := r.PostFormValue("color")
	if len(name) > 0 {
		context.db.CreateAndPersistImageCategory(name, escapeColor(color))
	}
	context.RootRedirectHandler(w, r)
}

func escapeColor(color string) string {
	if len(color) != 7 {
		return ""
	} else if validColor.MatchString(color) {
		return color
	}
	return ""
}

/*RemoveCategoryHandler Remvoes a category from a POST using "id". */
func (context *HandlerContext) RemoveCategoryHandler(w http.ResponseWriter, r *http.Request) {
	if checkForPostWithError(w, r) {
		return
	}
	r.ParseForm()
	id := r.PostFormValue("id")
	if len(id) > 0 {
		if parsedID, err := strconv.ParseUint(id, 10, 64); err == nil {
			context.db.DeleteImageCategory(uint(parsedID))
		} else {
			log.Printf("Cannot delete image category with invalid id: %s\n", id)
		}
	}
	context.RootRedirectHandler(w, r)
}

/*CreateDescriptionHandler Creates a description from a form using "imageName", "description", "exampleCommand". */
func (context *HandlerContext) CreateDescriptionHandler(w http.ResponseWriter, r *http.Request) {
	if checkForPostWithError(w, r) {
		return
	}
	r.ParseForm()
	imageName := r.PostFormValue("imageName")
	description := r.PostFormValue("description")
	exampleCommand := r.PostFormValue("exampleCommand")
	if len(imageName) > 0 {
		context.db.CreateAndPersistOrUpdateImageDescription(imageName,
			description, exampleCommand)
	}
	context.RootRedirectHandler(w, r)
}

/*AddCategoryToDescriptionHandler Adds a category to a descriptio from a POST using "category" and "image". */
func (context *HandlerContext) AddCategoryToDescriptionHandler(w http.ResponseWriter, r *http.Request) {
	if checkForPostWithError(w, r) {
		return
	}
	r.ParseForm()
	categoryID := r.PostFormValue("category")
	descriptionID := r.PostFormValue("image")
	if len(categoryID) > 0 && len(descriptionID) > 0 {
		parsedCategoryID, categoryErr := strconv.ParseUint(categoryID, 10, 64)
		parsedDescriptionID, descriptionErr := strconv.ParseUint(descriptionID, 10, 64)
		if categoryErr == nil && descriptionErr == nil {
			context.db.AddImageCategoryToImageDescription(uint(parsedCategoryID), uint(parsedDescriptionID))
		}
	}
	context.RootRedirectHandler(w, r)
}

/*RemoveCategoryFromDescriptionHandler Removes a category from a descriptio from a POST using "category" and "image". */
func (context *HandlerContext) RemoveCategoryFromDescriptionHandler(w http.ResponseWriter, r *http.Request) {
	if checkForPostWithError(w, r) {
		return
	}
	r.ParseForm()
	categoryID := r.PostFormValue("category")
	descriptionID := r.PostFormValue("image")
	if len(categoryID) > 0 && len(descriptionID) > 0 {
		parsedCategoryID, categoryErr := strconv.ParseUint(categoryID, 10, 64)
		parsedDescriptionID, descriptionErr := strconv.ParseUint(descriptionID, 10, 64)
		if categoryErr == nil && descriptionErr == nil {
			context.db.RemoveImageCategoryFromImageDescription(uint(parsedCategoryID), uint(parsedDescriptionID))
		}
	}
	context.RootRedirectHandler(w, r)
}

/*RootRedirectHandler Redirects to the index page. */
func (context *HandlerContext) RootRedirectHandler(w http.ResponseWriter, r *http.Request) {
	http.Redirect(w, r, context.settings.ContextRoot+"/", 302)
}

func setCommonHeaders(w http.ResponseWriter) {
	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	w.Header().Set("Cache-Control", "no-cache")
}

func checkForPostWithError(w http.ResponseWriter, r *http.Request) bool {
	if r.Method == "POST" {
		return true
	}
	http.NotFound(w, r)
	return false
}
