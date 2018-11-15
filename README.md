# Docker Registry UI

UI for private Docker registries. Shows images and tags of all images in the registry. Displays user-provided image descriptions and run documentation. Is deliberatly kept light on features to be simple to setup, run, and maintain.
Designed with a focus on internal and development registries that require frequent updates to images and image documentation.

## Features
1. Displays all images and tags of the Docker registry
2. Documents user-provided descriptions and example run commands for images
3. Images can be labelled and filtered for labels

![Example UI Instance](https://user-images.githubusercontent.com/6392457/48422040-12041680-e75e-11e8-8919-2d361488e627.png)

## Getting Started

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

Some more examples using the environment variables for different setups can be found in the [deployment examples](#deployment-examples).

## Accessing the UI

The UI can be accessed at http://yourmachine:8080/ (assuming you exposed it at port 8080). Accessing it at this root path will immediatly redirect you to http://yourmachine:8080/ui/ . Note that the `/ui` path and all nested sub-paths do not collide with the Docker registry API. As a result, your proxy could expose both the UI and the registry itself using the same host name.

## Securing the UI

The recommended way of securing the UI is by adding authentication to your proxy. You can add read/write authentication by allowing all read users to access `/ui/` and only allowing write users to `/ui/write/*`.

The following partial example uses a registry protected by an nginx proxy, configured as [described in the Docker documentation](https://docs.docker.com/registry/recipes/nginx/#setting-things-up). We edit the `location` element of the example configuration:

```nginx
location / {
    # Assumes the the htpasswd file exists and has at least one valid user
    auth_basic "Registry realm";
    auth_basic_user_file /etc/nginx/conf.d/nginx.htpasswd;

    # The proxy headers specified in the Docker documentation
    # (see https://docs.docker.com/registry/recipes/nginx/#setting-things-up)
    proxy_set_header  Host              $http_host;
    proxy_set_header  X-Real-IP         $remote_addr;
    proxy_set_header  X-Forwarded-For   $proxy_add_x_forwarded_for;
    proxy_set_header  X-Forwarded-Proto $scheme;
    proxy_read_timeout                  900;

    # Pass everything not in the /v2/ location to the UI
    proxy_pass http://<ui-host>:<ui-port>;

    # Proxy the Docker registry API to the actual registry
    location /v2/ {
      # You may want to catch old Docker Versions (omitted, see the linked Docker documentation)

      # Add $docker_distribution_api_version, as in Docker documentation nginx receipe
      add_header 'Docker-Distribution-Api-Version' $docker_distribution_api_version always;

      # disable limits to avoid HTTP 413 for large image uploads
      client_max_body_size 0;

      # proxy the /v2/ location to the registry, proxy settings were already set for parent location
      proxy_pass  http://<registry-host>:<registry-port>;
    }

    # Optional: restrict write access to the UI by restricting POST or the following location:
    location /ui/write/ {
        # some additional restrictions or htpasswd
    }
}

```

## Deployment Examples

Following, a few example deployments of the UI with a Docker registry.

### UI with access to Registry using linked Containers with Docker-Compose

In this example, the Registry and UI are expected to be secured usig some front-end proxy. However, the UI accesses the registry directly through Docker. This way, it doesn't need any authorization credentials or HTTPS shenanigans.

The example uses Docker-compose. A similar example using Kubernetes can be found below.

```yaml
version: "2"
services:
  registry:
    image: registry:2
    restart: always
    environment:
      <optional environemnt variables>
    volumes:
      - </your/registry/volume>:/var/lib/registry
    ports:
      - 5000:5000
  registry-ui:
    image: descartesresearch/dockerregistryui
    restart: always
    environment:
      - REGISTRY_HOST=<your.public.host>
      - REGISTRY_URL=http://registry:5000/
    volumes:
      - </your/registry-ui/volume>:/data
    links:
      - registry
    ports:
      - 8080:8080
```

### UI with access to Registry in Kubernetes Pod

In this example, the Registry and UI are co-located on the same pod in a Kubernetes cluster. can be achieved by co-locating both images in a Pod in Kubernetes. The example publishes the Registry and UI using a `NodePort` example service to ports 30050 and 30080. In production, you would probably use an ingress instead. This example also doesn't consider authentication.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: registry
  labels:
    run: registry
spec:
  containers:
  - name: registry
    image: registry:2
    ports:
    - containerPort: 5000
    # Don't forget to mount /var/lib/registry to one of your volumes
  - name: registry-ui
    image: descartesresearch/dockerregistryui
    ports:
    - containerPort: 8080
    env:
    - name: REGISTRY_HOST
      value: "myregistry.com"
    - name: REGISTRY_URL
      value: "http://localhost:5000/"
    # Don't forget to mount /data to one of your volumes
---
# Example service using node port. You should probably use an ingress (facilitating authentication) instead.
apiVersion: v1
kind: Service
metadata:
  name: registry-ui
  labels:
    run: registry
spec:
  type: NodePort
  ports:
  - port: 5000
    name: registry-port
    nodePort: 30050
    protocol: TCP
  - port: 8080
    name: registry-ui-port
    nodePort: 30080
    protocol: TCP
  selector:
    run: registry

```

### UI with access to public Registry with Basic Authentication

In this example, the UI accesses the Registry using its public host-name. The registry is secured using basic authentication.

```bash
$ docker run -d --restart=always --name registry-ui \
    -v </your/registry-ui/volume>:/data -p 8080:8080 \
    -e REGISTRY_HOST=<your.public.host> \
    -e REGISTRY_BASIC_AUTH_USER=<registry-username> \
    -e REGISTRY_BASIC_AUTH_PASSWORD=<registry-password> \
    descartesresearch/dockerregistryui
```