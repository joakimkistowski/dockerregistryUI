package utils

import (
	"crypto/tls"
	"encoding/json"
	"errors"
	"log"
	"net/http"
	"strconv"
)

const catalogURI string = "v2/_catalog"
const tagsListURIPrefix string = "v2/"
const tagsListURISuffix string = "/tags/list"

// RegistryImage Image, as described by the registry.
type RegistryImage struct {
	ImageName string
	ImageTags []string
}

// DockerImageMetaData Interface for meta-data on Docker images with a name and tags.
type DockerImageMetaData interface {
	GetImageName() string
	GetImageTags() []string
}

// RegistryHTTPClient An HTTP client that handles communication with the registry.
type RegistryHTTPClient struct {
	initialized bool
	settings    DockerRegistryUISettings
}

type repositoryList struct {
	Repositories []string `json:"repositories"`
}

type tagList struct {
	Tags []string `json:"tags"`
}

// NewRegistryHTTPClient Initializes a new HTTP client for communication with the Docker registry.
func NewRegistryHTTPClient(settings DockerRegistryUISettings) *RegistryHTTPClient {
	if settings.IgnoreInsecureHTTPS {
		http.DefaultTransport.(*http.Transport).TLSClientConfig = &tls.Config{InsecureSkipVerify: true}
	}
	return &RegistryHTTPClient{initialized: true, settings: settings}
}

// RetreiveRegistryImages Retreives image infos from the registry.
func (client *RegistryHTTPClient) RetreiveRegistryImages() []RegistryImage {
	imageInfos, _ := client.CheckUpToDateOrRetreiveRegistryImages(nil)
	return imageInfos
}

// CheckUpToDateOrRetreiveRegistryImages Checks if the provided images are still up-to-date.
// Returns an empty image list and "true" if they are. Returns a more up-to-date list and "false" otherwise.
// The original list is never modified.
func (client *RegistryHTTPClient) CheckUpToDateOrRetreiveRegistryImages(
	imagesToCheck []DockerImageMetaData) ([]RegistryImage, bool) {
	if !client.initialized {
		return []RegistryImage{}, false
	}
	// get images from registry
	response, err := getResponse(client.settings, catalogURI)
	if err != nil {
		return []RegistryImage{}, false
	}
	var rlist repositoryList
	err = unmarshalJSON(response, &rlist)
	if err != nil {
		return []RegistryImage{}, false
	}
	// check if the image count has changed
	registryUpToDate := imagesToCheck != nil && (len(rlist.Repositories) == len(imagesToCheck))
	// get tags from registry
	var imageInfos []RegistryImage
	for _, image := range rlist.Repositories {
		var imageInfo RegistryImage
		imageInfo.ImageName = image
		response, err = getResponse(client.settings, tagsListURIPrefix+image+tagsListURISuffix)
		if err != nil {
			return []RegistryImage{}, false
		}
		var tlist tagList
		err = unmarshalJSON(response, &tlist)
		if err != nil {
			return []RegistryImage{}, false
		}
		for _, tag := range tlist.Tags {
			imageInfo.ImageTags = append(imageInfo.ImageTags, tag)
		}
		imageInfos = append(imageInfos, imageInfo)
	}
	// return if up-to-date version on image count mismatch, or check if the tag counts per image have changed
	if !registryUpToDate {
		return imageInfos, false
	}
	for i, retreivedImage := range imageInfos {
		if len(retreivedImage.ImageTags) != len(imagesToCheck[i].GetImageTags()) ||
			retreivedImage.ImageName != imagesToCheck[i].GetImageName() {
			return imageInfos, false
		}
	}
	return []RegistryImage{}, true
}

func getResponse(settings DockerRegistryUISettings, uri string) (*http.Response, error) {
	request, err := http.NewRequest("GET", settings.RegistryURL+uri, nil) //http.Get(settings.RegistryURL + uri)
	if len(settings.RegistryBasicAuthUser) > 0 || len(settings.RegistryBasicAuthPassword) > 0 {
		request.SetBasicAuth(settings.RegistryBasicAuthUser, settings.RegistryBasicAuthPassword)
	}
	response, err := http.DefaultClient.Do(request)
	if err != nil {
		log.Printf("Could not get response from Registry at %s; error: %s\n", settings.RegistryURL+uri, err)
	} else if response.StatusCode >= 400 {
		log.Printf("Could not get response from Registry at %s; Error Code:%v\n",
			settings.RegistryURL+uri, response.StatusCode)
		err = errors.New("Status Code: " + strconv.FormatInt(int64(response.StatusCode), 10))
	}
	return response, err
}

func unmarshalJSON(response *http.Response, v interface{}) error {
	defer response.Body.Close()
	err := json.NewDecoder(response.Body).Decode(v)
	if err != nil {
		log.Printf("Could not get decode response from Registry; error: %s\n", err)
		return err
	}
	return nil
}

// GetImageName Get the image name.
func (image *RegistryImage) GetImageName() string {
	return image.ImageName
}

// GetImageTags Get the image tags.
func (image *RegistryImage) GetImageTags() []string {
	return image.ImageTags
}
