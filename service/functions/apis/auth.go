package main

import (
	"fmt"
	"net/http"
	"strings"
	"os"

	"github.com/auth0-community/go-auth0"
	jose "gopkg.in/square/go-jose.v2"
	jwt "gopkg.in/square/go-jose.v2/jwt"
)


var AUTH0_API_AUDIENCE = []string{os.Getenv("AUTH0_API_AUDIENCE")}
var AUTH0_API_CLIENT_SECRET = os.Getenv("AUTH0_API_CLIENT_SECRET")
var AUTH0_DOMAIN = os.Getenv("AUTH0_DOMAIN")


func authMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		token, err := getToken(r)

		if err != nil {
			fmt.Println(err)
			fmt.Println("Token is not valid:", token)
			w.WriteHeader(http.StatusUnauthorized)
			w.Write([]byte("Unauthorized"))
			return
		}

		next.ServeHTTP(w, r)
	})
}


func canSendInvitesMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		token, err := getToken(r)

		if err != nil {
			fmt.Println(err)
			fmt.Println("Token is not valid:", token)
			w.WriteHeader(http.StatusUnauthorized)
			w.Write([]byte("Unauthorized"))
			return
		}

		secret := []byte(AUTH0_API_CLIENT_SECRET)
		claims := make(map[string]interface{})
		if err = token.Claims(secret, &claims); err != nil {
			fmt.Println(err)
			fmt.Println("Claims not valid: ", claims)
			http.Error(w, "Malformed token claims",
				400)
			return
		}

		if (canSendInvites(claims)) {
			next.ServeHTTP(w, r)
		} else {
			w.WriteHeader(http.StatusUnauthorized)
			w.Write([]byte("Unauthorized"))
			return
		}
	})
}

func canSendInvites(claims map[string]interface{}) bool {
	customScopes := claims["https://foodwithfriends.api/roles"].(string)
	for _, scope := range strings.Split(customScopes, " ") {
		if scope == "send:invites" {
			return true
		}
	}
	return false
}

func getToken(r *http.Request) (*jwt.JSONWebToken, error){
	secret := []byte(AUTH0_API_CLIENT_SECRET)
	secretProvider := auth0.NewKeyProvider(secret)
	audience := AUTH0_API_AUDIENCE
	domain := fmt.Sprintf("https://%s.auth0.com/",
		AUTH0_DOMAIN)
	configuration := auth0.NewConfiguration(secretProvider,
		audience, domain, jose.HS256)
	validator := auth0.NewValidator(configuration)

	return validator.ValidateRequest(r)
}
