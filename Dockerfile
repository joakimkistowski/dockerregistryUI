FROM golang:alpine
#build container
RUN apk add --update-cache build-base git
RUN mkdir -p /go/src/dockerregistryUI/
WORKDIR /go/src/dockerregistryUI/
COPY ./*.go .
COPY ./handlers ./handlers
COPY ./persistence ./persistence
COPY ./utils ./utils
RUN go get -u github.com/microcosm-cc/bluemonday
RUN go get -u gitlab.com/golang-commonmark/markdown
RUN go get -u github.com/jinzhu/gorm
RUN go get -u github.com/jinzhu/gorm/dialects/sqlite
RUN go test ./persistence
RUN GOOS=linux go build -v .

RUN mkdir -p /opt/dockerregistryUI/
COPY ./templates /opt/dockerregistryUI/templates
COPY ./static /opt/dockerregistryUI/static

FROM alpine
# Execution Container
COPY --from=0 /opt/dockerregistryUI /opt/dockerregistryui
COPY --from=0 /go/src/dockerregistryUI/dockerregistryUI /opt/dockerregistryui/dockerregistryui

RUN mkdir /data

ENV REGISTRY_HOST ""
ENV REGISTRY_PROTOCOL https
ENV REGISTRY_URL ""
ENV IGNORE_INSECURE_HTTPS false
VOLUME /data

EXPOSE 8080

WORKDIR /opt/dockerregistryui
ENTRYPOINT ["/opt/dockerregistryui/dockerregistryui"]
