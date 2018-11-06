package main

import (
	"html/template"
	"log"
	"net/http"
)

/*ContextRoot The root URI of the registry UI */
const ContextRoot string = "/ui"

var templates = template.Must(template.ParseFiles("index.html"))

/*UITemplateData Data passed to the HTML template. */
type UITemplateData struct {
}

func main() {
	fileServer := http.FileServer(http.Dir("static"))
	http.Handle(ContextRoot+"/static/", http.StripPrefix(ContextRoot+"/static/", fileServer))
	http.HandleFunc(ContextRoot+"/", indexHandler)
	http.HandleFunc(ContextRoot, rootRedirectHandler)
	http.HandleFunc("/", rootRedirectHandler)
	log.Println("Started Docker Registry UI")
	log.Fatal(http.ListenAndServe(":8080", nil))
}

func indexHandler(w http.ResponseWriter, r *http.Request) {
	setCommonHeaders(w)
	var err error
	err = templates.ExecuteTemplate(w, "index.html", UITemplateData{})
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
	}
}

func rootRedirectHandler(w http.ResponseWriter, r *http.Request) {
	http.Redirect(w, r, ContextRoot+"/", 302)
}

func setCommonHeaders(w http.ResponseWriter) {
	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	w.Header().Set("Cache-Control", "no-cache")
}
