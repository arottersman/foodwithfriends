package main

import (
    "os"
    "strconv"
    "encoding/json"
    "bytes"
    "fmt"
    "net/http"
    "net/http/httptest"
    "net/url"

    "github.com/apex/go-apex"
)


var isDeployEnv, _ = strconv.ParseBool(os.Getenv("DEPLOY"))

// Following https://medium.com/capital-one-developers/building-a-serverless-rest-api-in-go-3ffcb549ef2
func main() {
    handler := FoodWithFriendsHTTPHandler()
    if isDeployEnv {
        // Register the Lambda event handler
        apex.HandleFunc(func(event json.RawMessage,
            ctx  *apex.Context) (interface{}, error) {
                request, err := ParseLambdaRequest(event)
                if err != nil {
                    return FormatLambdaError(
                        http.StatusBadRequest,
                        err), nil
                }

                response := httptest.NewRecorder()

                handler.ServeHTTP(response, request)

                return FormatLambdaResponse(response), nil
            })
    } else {
        fmt.Printf("Running in dev mode")
        http.ListenAndServe(":8080", handler)
    }
}

func preflightOptionsMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method == "OPTIONS" {
			w.Header().Set("Allow", "POST, GET, OPTIONS, PUT, DELETE")
			return
		}

		next.ServeHTTP(w, r)
	})
}

func allowBasicAccessHeadersMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization")

		next.ServeHTTP(w, r)
	})
}

func logRequestMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if !isDeployEnv {
			fmt.Printf("Requested: %s\n\n", r.URL)
		}

		next.ServeHTTP(w, r)
	})
}

func foodWithFriendsMiddleware(next http.Handler) http.Handler {
	return logRequestMiddleware(
		allowBasicAccessHeadersMiddleware(
			preflightOptionsMiddleware(
				authMiddleware(next))))
}

func FoodWithFriendsHTTPHandler() http.Handler {
	mux := http.NewServeMux()

	mux.Handle("/events/", foodWithFriendsMiddleware(
		http.HandlerFunc(EventHandler)))
	mux.Handle("/users/", foodWithFriendsMiddleware(
		http.HandlerFunc(UserHandler)))
	mux.Handle("/hosts/", foodWithFriendsMiddleware(
		http.HandlerFunc(HostHandler)))
	mux.Handle("/admin/", foodWithFriendsMiddleware(
		canSendInvitesMiddleware(
			http.HandlerFunc(AdminHandler))))
	return mux
}

func ParseLambdaRequest(event json.RawMessage) (*http.Request, error) {
    var input LambdaInput
    if err := json.Unmarshal(event, &input); err != nil {
            return nil, err
    }

    inputParams := url.Values{}
    for key, value := range input.Params {
        inputParams.Set(key, value)
    }

    request, err := http.NewRequest(input.Method,
        input.Path+"?"+inputParams.Encode(),
        bytes.NewBufferString(input.Body))
    if err != nil {
        return nil, err
    }

    for key, value := range input.Headers {
        request.Header.Set(key, value)
    }

    request.Header.Set("Content-Type", "application/json")

    return request, nil
}

func FormatLambdaResponse(result *httptest.ResponseRecorder) LambdaOutput {
    lambdaResult := LambdaOutput {
        StatusCode: result.Code,
        Body:       result.Body.String(),
        Headers:    map[string]string{},
    }
    for key := range result.HeaderMap {
        lambdaResult.Headers[key] = result.HeaderMap.Get(key)
    }
    return lambdaResult
}

func FormatLambdaError(status int, err error) LambdaOutput {
    bodyString, _ := json.Marshal(err)
    return LambdaOutput {
        StatusCode: status,
        Body:       string(bodyString),
        Headers:    map[string]string{
            "Content-Type": "application/json",
        },
    }
}
