# Docker Registry UI

UI for private Docker registries. Shows images and tags in of all images in the registry. Displays user-provided image descriptions and run documentation. Is deliberatly kept light on features to be simple to setup, run, and maintain.
Designed with a focus on internal and development registries that require frequent updates to images and image documentation.

## Features
1. Displays all images and tags of the Docker registry
2. Documents user-provided descriptions and example run commands for images
3. Images can be labelled and filtered for labels

![Example UI Instance](https://user-images.githubusercontent.com/6392457/48422040-12041680-e75e-11e8-8919-2d361488e627.png)

## Getting Started

**Please note that the setup is currently not working, as the UI is not on DockerHub yet**

Running the registry UI is as simple as executing the following Docker command line (change `/my/persistent/path` to a path on your machine and `myregistry.com` to the host name of your Docker registry):

```bash
$ docker run -d --restart=always -v /my/persistent/path:/data -e REGISTRY_HOST=myregistry.com -p 8080:8080 descartesresearch/dockerregistryui
```

With this setup, the UI will save its data to the mounted volume path (substituted for `/my/persistent/path` in the example). It will attempt to connect to a registry at `myregistry.com` using HTTPS without any authorization. The UI can be accessed at http://yourmachine:8080/ui/.

In general, you can configure the registry UI using environment variables. The following environment variables are supported:

* `REGISTRY_HOST` : The hostname of the docker registry. Must be provided with a non-empty value.
* `REGISTRY_PROTOCOL` : The protocol the registry is listening on. Is `https` by default.
* `REGISTRY_URL` : This optional variable can be set to specify the URL the UI should use to access the registry. It is usually automatically derived from `REGISTRY_HOST` and `REGISTRY_PROTOCOL`. However, in some cases, you may want your UI to access the registry using a URL that differs from the public protocol or hostname (e.g., to facilitate access via a local network). This variable can be set to achieve this.
* `IGNORE_INSECURE_HTTPS` : Set this to `true` to get the UI to ignore bad or missing certificates when accessing the registry via HTTPS. Usage of this setting is not recommended. You may want to side-step the HTTPS-connection by accessing the registry via http from a local network, a linked container, or on the same pod using the `REGISTRY_URL` variable instead.
* `REGISTRY_BASIC_AUTH_USER` : User to access registries protected with basic authentication.
* `REGISTRY_BASIC_AUTH_PASSWORD` : Password to access registries protected with basic authentication.

## Accessing the UI

The UI can be accessed at http://yourmachine:8080/ (assuming you exposed it at port 8080). Accessing it at this root path will immediatly redirect you to http://yourmachine:8080/ui/ . Note that the `/ui` path and all nested sub-paths do not collide with the Docker registry API. As a result, your proxy could expose both the UI and the registry itself using the same host name.
