(ns fwf.constants
  (:require [clojure.string :as str]))

(goog-define client-host "http://localhost:3449")

(goog-define api-url "http://localhost:8080")

(def access-token-ls-key "access-token")

(def profile-ls-key "profile")

(def auth0-client-id "PpZGQ7wzJOgpJrgP75UQ7zQ6OokuYRmM")

(def auth0-domain "https://alicerottersman.auth0.com")

(def auth0-state "FOOD_WITH_FRIENDS")

(def auth0-audience "https://api.food-with-friends.com")

(def auth0-redirect-uri (str client-host
                             "/#/login-callback"))

(def auth0-authorize-url
  (str
   auth0-domain "/authorize?"
   "response_type=code&"
   "state=" auth0-state "&"
   "scope=" (js/encodeURIComponent "openid profile email") "&"
   "redirect_uri=" (js/encodeURIComponent
                    auth0-redirect-uri) "&"
   "client_id=" auth0-client-id "&"
   "audience=" (js/encodeURIComponent auth0-audience)))
