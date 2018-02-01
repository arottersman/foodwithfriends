(ns fwf.api-helpers
  (:require [ajax.core :as ajax]
            [cljs-time.format]
            [fwf.constants :refer [api-url]]
            [fwf.db :as db]))

(defn auth-header [access-token]
  {"Authorization"
   (str "Bearer " access-token)})

(def server-response-date-formatter (cljs-time.format/formatter "YYYY-MM-dd'T'HH:mm:ss'Z'"))
(def server-request-date-formatter (cljs-time.format/formatter "YYYY-MM-dd'T'HH:mm:ss+00:00"))

;; -- Parsers ------------------------------

(defn parse-user [{:keys [name
                          userId
                          email
                          dietaryRestrictions
                          assignedDish
                          auth0Id
                          hostId]}]
  {::db/name name
   ::db/user-id userId
   ::db/email email
   ::db/dietary-restrictions dietaryRestrictions
   ::db/assigned-dish assignedDish
   ::db/auth0-id auth0Id
   ::db/host-id hostId})

(defn parse-host [{:keys [address
                          city
                          state
                          zipcode
                          maxOccupancy
                          hostId
                          users]}]
  {::db/address address
   ::db/city city
   ::db/state state
   ::db/zipcode zipcode
   ::db/max-occupancy maxOccupancy
   ::db/host-id hostId
   ::db/users (map parse-user users)})


(defn parse-event [{:keys [eventId
                           title
                           description
                           participants
                           host
                           happeningAt]}]
  {::db/event-id eventId
   ::db/title title
   ::db/description description
   ::db/participants (map parse-user participants)
   ::db/host (parse-host host)
   ::db/happening-at happeningAt})

(defn parse-assigned-dish [event-response user-id]
  (let [{:keys [fwf.db/participants]}
        (parse-event event-response)
        user? #(if (= user-id (::db/user-id %)) %)]
    (::db/assigned-dish (some user? participants))))

;; -- Fetches -----------------------

(defn fetch-upcoming-events! [{:keys [access-token on-success on-failure]}]
  (ajax/GET (str api-url "/events/") {:headers (auth-header access-token)
                                      :handler on-success
                                      :error-handler on-failure
                                      :response-format :json
                                      :keywords? true}))


(defn fetch-user-events! [{:keys [user-id access-token on-success on-failure]}]
  (ajax/GET (str api-url "/events/") {:headers (auth-header access-token)
                                      :params {:userId user-id}
                                      :handler on-success
                                      :error-handler on-failure
                                      :response-format :json
                                      :keywords? true}))

(defn fetch-user! [{:keys [auth0-id access-token on-success on-failure]}]
  (ajax/GET (str api-url "/users/") {:headers (auth-header access-token)
                                     :params {:auth0Id auth0-id}
                                     :handler on-success
                                     :error-handler on-failure
                                     :response-format :json
                                     :keywords? true}))

(defn fetch-host! [{:keys [host-id access-token on-success on-failure]}]
  (ajax/GET (str api-url "/hosts/") {:headers (auth-header access-token)
                                     :params {:hostId host-id}
                                     :handler on-success
                                     :error-handler on-failure
                                     :response-format :json
                                     :keywords? true}))
