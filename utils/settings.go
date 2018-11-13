package utils

import (
	"log"
	"os"
	"strconv"
	"strings"
)

const fallbackContextRoot string = "/ui"
const fallbackRegistryHostName string = "docker-registry"
const fallbackRegistryProtocol string = "https"
const fallbackURIStaticDir string = fallbackContextRoot + "/static/"
const fallbackURIWritePrefix string = fallbackContextRoot + "/write"
const fallbackURIAddCategoryToImage string = fallbackURIWritePrefix + "/addcategorytoimage"
const fallbackURICreateCategory string = fallbackURIWritePrefix + "/createcategory"
const fallbackURIImageDescription string = fallbackURIWritePrefix + "/imagedescription"
const fallbackURIRemoveCategoryFromImage string = fallbackURIWritePrefix + "/removecategoryfromimage"
const fallbackURIRemoveCategory string = fallbackURIWritePrefix + "/removecategory"
const fallbackURIHello string = fallbackURIWritePrefix + "/hello"

// RegistryHostEnvironmentVariableName Name of the environemnt variable for the registry host.
const RegistryHostEnvironmentVariableName string = "REGISTRY_HOST"

// RegistryProtocolEnvironmentVariableName Name of the environemnt variable for the registry's protocol.
const RegistryProtocolEnvironmentVariableName string = "REGISTRY_PROTOCOL"

// RegistryURLEnvironmentVariableName Name of the environemnt variable for the registry's URL.
const RegistryURLEnvironmentVariableName string = "REGISTRY_URL"

// IgnoreInsecureHTTPSEnvironmentVariableName Name of the environemnt variable igonoring insecure HTTPS.
const IgnoreInsecureHTTPSEnvironmentVariableName string = "IGNORE_INSECURE_HTTPS"

// RegistryBasicAuthUserEnvironmentVariableName Name of the environemnt variable for a basic authentication user.
const RegistryBasicAuthUserEnvironmentVariableName string = "REGISTRY_BASIC_AUTH_USER"

// RegistryBasicAuthPasswordEnvironmentVariableName Name of the environemnt variable for a basic authentication pw.
const RegistryBasicAuthPasswordEnvironmentVariableName string = "REGISTRY_BASIC_AUTH_PASSWORD"

// DockerRegistryUISettings Contains all settings for the registry ui.
type DockerRegistryUISettings struct {
	ContextRoot                string
	RegistryHostName           string
	RegistryProtocol           string
	RegistryURL                string
	RegistryBasicAuthUser      string
	RegistryBasicAuthPassword  string
	IgnoreInsecureHTTPS        bool
	URIStaticDir               string
	URIAddCategoryToImage      string
	URICreateCategory          string
	URIImageDescription        string
	URIRemoveCategoryFromImage string
	URIRemoveCategory          string
	URIHello                   string
}

// DefaultSettings Returns the default settings.
func DefaultSettings() DockerRegistryUISettings {
	return DockerRegistryUISettings{
		ContextRoot:                fallbackContextRoot,
		RegistryHostName:           fallbackRegistryHostName,
		RegistryProtocol:           fallbackRegistryProtocol,
		RegistryURL:                fallbackRegistryProtocol + "://" + fallbackRegistryHostName + "/",
		RegistryBasicAuthUser:      "",
		RegistryBasicAuthPassword:  "",
		IgnoreInsecureHTTPS:        false,
		URIStaticDir:               fallbackURIStaticDir,
		URIAddCategoryToImage:      fallbackURIAddCategoryToImage,
		URICreateCategory:          fallbackURICreateCategory,
		URIImageDescription:        fallbackURIImageDescription,
		URIRemoveCategoryFromImage: fallbackURIRemoveCategoryFromImage,
		URIRemoveCategory:          fallbackURIRemoveCategory,
		URIHello:                   fallbackURIHello,
	}
}

// DefaultSettingsForRegistryHostAndProtocol Returns the default settings for a custum registry host name.
func DefaultSettingsForRegistryHostAndProtocol(hostName string, protocol string) DockerRegistryUISettings {
	settings := DefaultSettings()
	settings.RegistryHostName = hostName
	settings.RegistryProtocol = protocol
	settings.RegistryURL = protocol + "://" + hostName + "/"
	return settings
}

// SettingsFromEnvironmentVariables Initializes settings using environment variables (see variables in Dockerfile).
func SettingsFromEnvironmentVariables() DockerRegistryUISettings {
	settings := DefaultSettings()
	if host := os.Getenv(RegistryHostEnvironmentVariableName); host != "" {
		settings.RegistryHostName = host
	} else {
		log.Printf("No Docker registry host name specified. Specify one using environment variable: %s\n",
			RegistryHostEnvironmentVariableName)
	}
	if protocol := os.Getenv(RegistryProtocolEnvironmentVariableName); protocol != "" {
		settings.RegistryProtocol = protocol
	}
	if url := os.Getenv(RegistryURLEnvironmentVariableName); url != "" {
		if strings.HasSuffix(url, "/") {
			settings.RegistryURL = url
		} else {
			settings.RegistryURL = url + "/"
		}
	} else {
		settings.RegistryURL = settings.RegistryProtocol + "://" + settings.RegistryHostName + "/"
	}
	if ignore := os.Getenv(IgnoreInsecureHTTPSEnvironmentVariableName); ignore != "" {
		if ignoreb, err := strconv.ParseBool(ignore); err != nil {
			log.Printf("Invalid setting for %s: %s\n", IgnoreInsecureHTTPSEnvironmentVariableName, ignore)
		} else {
			settings.IgnoreInsecureHTTPS = ignoreb
		}
	}
	if user := os.Getenv(RegistryBasicAuthUserEnvironmentVariableName); user != "" {
		settings.RegistryBasicAuthUser = user
	}
	if pw := os.Getenv(RegistryBasicAuthPasswordEnvironmentVariableName); pw != "" {
		settings.RegistryBasicAuthPassword = pw
	}
	return settings
}
