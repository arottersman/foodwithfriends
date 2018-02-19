(ns fwf.db
  (:require [cljs.reader]
            [cljs-time.core]
            [clojure.string]
            [re-frame.core :as re-frame]
            [fwf.constants :refer [access-token-ls-key
                                   profile-ls-key]]
            [fwf.utils :refer [parse-id-token]]))

;; -- Default app-db value --------

;; event date default values
(def possible-event-start (cljs-time.core/now))
(def possible-event-end (cljs-time.core/plus
                         possible-event-start
                         (cljs-time.core/months 1)
                         (cljs-time.core/days 15)))

(def default-db {::showing-events :upcoming-events
                 ::auth0 {::polling? false}
                 ;; onboarding forms
                 ::user-form
                 {::dietary-restrictions []
                  ::name ""
                  ::polling? false}
                 ::host-form
                 ;; host-form searched hosts
                 {::searched-hosts
                  :not-searched
                  ;; host-form fields
                  ::host-form-fields
                  {::address ""
                   ::city ""
                   ::state ""
                   ::zipcode ""
                   ::max-occupancy 0}
                  ;; host-form api
                  ::host-form-api
                  {::polling? false
                   ::searching? false}}
                 ;; admin
                 ::send-invites {::polling? false
                                 ::succeeded? false
                                 ::num-hosts 0}
                 ;; event lists
                 ::upcoming-events {::stale? true
                                    ::rsvping? false}
                 ::user-events {::stale? true}
                 ::the-user {::stale? true}
                 ;; event form
                 ::event-form
                 {::happening-at-time
                  {::hour 6
                   ::minute 0
                   ::time-of-day :pm}
                  ::happening-at-date (cljs-time.core/now)
                  ::title ""
                  ::description ""
                  ::email-participants? false
                  ::polling? false}
                 ::possible-event-start
                 possible-event-start
                 ::possible-event-end
                 possible-event-end})

;; -- Local storage --------------------

(defn auth0->local-store
  "Put the auth0 access token and profile into localStorage"
  [_ [{:keys [access_token id_token]}]]
  (.setItem js/localStorage access-token-ls-key access_token)
  (.setItem js/localStorage profile-ls-key id_token))

(defn wipe-local-store
  []
  (.setItem js/localStorage access-token-ls-key "")
  (.setItem js/localStorage profile-ls-key ""))

(defn wipe-local-store-if-401 [_ [{:keys [status]}]]
  (if (= status 401)
    (wipe-local-store)))

(defn- parse-auth0-profile [{:keys [name
                                    email_verified
                                    sub
                                    email]}]
  {::name name
   ::email-verified email_verified
   ::sub sub
   ::email email})

(defn id-token->auth0-profile [id-token]
  (parse-auth0-profile (parse-id-token id-token)))

;; -- Coeffect Handler Registrations ----------------

(re-frame/reg-cofx
 :local-store-auth0
 (fn [cofx _]
   (let [access-token (.getItem js/localStorage
                                access-token-ls-key)
         id-token (.getItem js/localStorage
                            profile-ls-key)]
     (if (or access-token id-token)
       (assoc cofx :local-store-auth0
              {::access-token
               (clojure.string/replace
                (clojure.string/trim
                 access-token) #"^\"|\"$" "")
               ::profile
               (id-token->auth0-profile id-token)})))))
