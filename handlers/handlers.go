package handlers

import (
	"dockerregistryUI/persistence"
	"dockerregistryUI/utils"
	"log"
	"net/http"
)

/*HandlerContext A context for the handlers. */
type HandlerContext struct {
	initialized bool
	settings    utils.DockerRegistryUISettings
	client      *utils.RegistryHTTPClient
	handle      *persistence.DBHandle
}

/*New Initializes a new HandlerContext. */
func New(settings utils.DockerRegistryUISettings, client *utils.RegistryHTTPClient,
	handle *persistence.DBHandle) *HandlerContext {
	return &HandlerContext{initialized: true, settings: settings, client: client, handle: handle}
}

/*IndexHandler Handles requests to the main index page. */
func (context *HandlerContext) IndexHandler(w http.ResponseWriter, r *http.Request) {
	//templateData := &UITemplateData{}
	templateData := InitializeUITemplateData(context.settings, context.handle, context.client)
	setCommonHeaders(w)
	err := templates.ExecuteTemplate(w, "index.html", templateData)
	if err != nil {
		log.Printf("Error rendering template: %s\n", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
	}
	//w.Write([]byte("Hello World"))
}

/*RootRedirectHandler Redirects to the index page. */
func (context *HandlerContext) RootRedirectHandler(w http.ResponseWriter, r *http.Request) {
	http.Redirect(w, r, context.settings.ContextRoot+"/", 302)
}

func setCommonHeaders(w http.ResponseWriter) {
	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	w.Header().Set("Cache-Control", "no-cache")
}
