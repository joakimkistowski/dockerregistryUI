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

/*RegistryImage Image, as described by the registry. */
type RegistryImage struct {
	/*ImageName The image's name. */
	ImageName string
	/*ImageTags The image's tags. */
	ImageTags []string
}

/*RegistryHTTPClient An HTTP client that handles communication with the registry. */
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

/*NewRegistryHTTPClient Initializes a new HTTP client for communication with the Docker registry. */
func NewRegistryHTTPClient(settings DockerRegistryUISettings) *RegistryHTTPClient {
	if settings.IgnoreInsecureHTTPS {
		http.DefaultTransport.(*http.Transport).TLSClientConfig = &tls.Config{InsecureSkipVerify: true}
	}
	return &RegistryHTTPClient{initialized: true, settings: settings}
}

/*RetreiveRegistryImages Retreives image infos from the registry. */
func (client *RegistryHTTPClient) RetreiveRegistryImages() []RegistryImage {
	if !client.initialized {
		return []RegistryImage{}
	}
	response, err := getResponse(client.settings, catalogURI)
	if err != nil {
		return []RegistryImage{}
	}
	var rlist repositoryList
	err = unmarshalJSON(response, &rlist)
	if err != nil {
		return []RegistryImage{}
	}
	var imageInfos []RegistryImage
	for _, image := range rlist.Repositories {
		var imageInfo RegistryImage
		imageInfo.ImageName = image
		response, err = getResponse(client.settings, tagsListURIPrefix+image+tagsListURISuffix)
		if err != nil {
			return []RegistryImage{}
		}
		var tlist tagList
		err = unmarshalJSON(response, &tlist)
		if err != nil {
			return []RegistryImage{}
		}
		for _, tag := range tlist.Tags {
			imageInfo.ImageTags = append(imageInfo.ImageTags, tag)
		}
		imageInfos = append(imageInfos, imageInfo)
	}
	return imageInfos
}

func getResponse(settings DockerRegistryUISettings, uri string) (*http.Response, error) {
	response, err := http.Get(settings.RegistryURL + uri)
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
	/*bodyBytes, _ := ioutil.ReadAll(response.Body)
	bodyString := string(bodyBytes)
	log.Println(bodyString)*/
	err := json.NewDecoder(response.Body).Decode(v)
	if err != nil {
		log.Printf("Could not get decode response from Registry; error: %s\n", err)
		return err
	}
	return nil
}
