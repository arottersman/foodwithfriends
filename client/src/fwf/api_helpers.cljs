(ns fwf.api-helpers
  (:require [ajax.core :as ajax]
            [cljs-time.format]))

(def api-url "http://localhost:8080")

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
  {:name name
   :user-id userId
   :email email
   :dietary-restrictions dietaryRestrictions
   :assigned-dish assignedDish
   :auth0-id auth0Id
   :host-id hostId})

(defn parse-host [{:keys [address
                          city
                          state
                          zipcode
                          maxOccupancy
                          hostId
                          users]}]
  {:address address
   :city city
   :state state
   :zipcode zipcode
   :max-occupancy maxOccupancy
   :host-id hostId
   :users (map parse-user users)})


(defn parse-event [{:keys [eventId
                           title
                           description
                           participants
                           host
                           happeningAt]}]
  {:event-id eventId
   :title title
   :description description
   :participants (map parse-user participants)
   :host (parse-host host)
   :happening-at happeningAt})

(defn parse-assigned-dish [event-response user-id]
  (let [{:keys [participants]} (parse-event event-response)]
    (:assigned-dish (first (filter #(= user-id (:user-id %)) participants)))))

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

