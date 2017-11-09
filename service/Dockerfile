FROM golang:1.7.6

EXPOSE 8080 8080

RUN if $DEPLOY; then curl https://raw.githubusercontent.com/apex/apex/master/install.sh | sh; fi

RUN go get github.com/apex/go-apex
RUN go get github.com/pressly/goose/cmd/goose
RUN go get github.com/lib/pq
RUN go get gopkg.in/square/go-jose.v2
RUN go get github.com/auth0-community/go-auth0
RUN go get github.com/dgrijalva/jwt-go
