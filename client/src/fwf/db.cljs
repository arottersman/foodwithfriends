(ns fwf.db
  (:require [cljs.reader]
            [clojure.string]
            [cljs.spec :as spec]
            [re-frame.core :as re-frame]
            [fwf.constants :refer [access-token-ls-key
                                   profile-ls-key]]
            [fwf.utils :refer [parse-id-token]]))

;; -- Spec ----------------------------


;; TODO I don't think these validate the way this assumes --
;; using the keys to refer to other specs...
;; Also, rework the opt keys, and give defaults in the state

(spec/def ::page keyword?)

(spec/def ::polling? boolean)
(spec/def ::error-response (spec/map-of keyword? string?))


(spec/def ::access-token string?)
(spec/def ::email_verified boolean)

(spec/def ::profile (spec/keys :req [::name
                                     ::email_verified
                                     ::user_id
                                     ::email]))
(spec/def ::auth0 (spec/keys :req [::polling?]
                             :opt [::access-token
                                   ::profile]))

(spec/def ::showing-events
  #{:upcoming-events
    :user-events
    })

(spec/def ::auth0-id string?)
(spec/def ::user-id int?)
(spec/def ::name string?)
(spec/def ::email string?)
(spec/def ::dietary-restrictions (spec/and vector?
                                           (spec/every string?)))
(spec/def ::assigned-dish string?)
(spec/def ::host-id int?)

(spec/def ::user (spec/or :no-account keyword?
                          :user (spec/keys :req [::auth0-id
                                           ::user-id
                                           ::name
                                           ::email
                                           ::dietary-restrictions
                                           ::assigned-dish
                                           ::host-id
                                           ])))
(spec/def ::user-form (spec/keys :req
                                 [::dietary-restrictions
                                  ::name
                                  ::polling?
                                  ::error-response]))

(spec/def ::the-user (spec/keys :opt [::user
                                      ::stale?
                                      ::error]))

(spec/def ::address string?)
(spec/def ::city string?)
(spec/def ::state string?)
(spec/def ::zipcode string?)
(spec/def ::max-occupancy int?)
(spec/def ::users (spec/* ::user))
(spec/def ::host (spec/keys :req [::host-id
                                  ::address
                                  ::city
                                  ::state
                                  ::zipcode
                                  ::max-occupancy
                                  ::users]))

(spec/def ::searched-hosts (spec/or :not-searched keyword?
                                    :hosts (spec/* ::host)))
(spec/def ::host-form (spec/keys :opt [::address
                                       ::city
                                       ::state
                                       ::zipcode
                                       ::polling?
                                       ::error-response
                                       ::searched-hosts
                                       ::max-occupancy]))

(spec/def ::title string?)
(spec/def ::description string?)
(spec/def ::happening-at int?) ;; TODO time?
(spec/def ::participants ::users)
(spec/def ::event-id int?)
(spec/def ::rsvping? boolean)
(spec/def ::stale? boolean)
(spec/def ::event (spec/keys :req [::event-id
                                   ::host
                                   ::participants
                                   ::title
                                   ::description
                                   ::happening-at]))
(spec/def ::events (spec/* ::event))
(spec/def ::upcoming-events (spec/keys :opt [::stale?
                                             ::events
                                             ::detail-id
                                             ::rsvping?
                                             ::error]))

(spec/def ::user-events (spec/keys :opt [::stale?
                                         ::events
                                         ::detail-id
                                         ::error]))

(spec/def ::user-detail ::user)
(spec/def ::user-assigned-dish string?)

(spec/def ::hour int?)
(spec/def ::minute int?)
(spec/def ::time-of-day #{:am
                          :pm})
(spec/def ::happening-at-time (spec/keys :req [::hour
                                               ::minute
                                               ::time-of-day]))
(spec/def ::event-form (spec/keys :opt [::title
                                        ::description
                                        ::happening-at-date
                                        ::happening-at-time
                                        ::error
                                        ::polling?]))

(spec/def ::num-hosts int?)
(spec/def ::succeeded boolean)
(spec/def ::send-invites (spec/keys :opt [::polling?
                                          ::error
                                          ::succeeded?
                                          ::num-hosts]))

(spec/def ::db (spec/keys :opt [::page
                                ::showing-auth-page
                                ::showing-events
                                ::upcoming-events
                                ::user-events
                                ::auth0
                                ::send-invites
                                ::the-user
                                ::user-detail
                                ::user-assigned-dish
                                ::user-form
                                ::host-form
                                ::event-form
                                ::possible-event-start
                                ::possible-event-end]))

;; event date default values

(def possible-event-start (cljs-time.core/now))
(def possible-event-end (cljs-time.core/plus
                         possible-event-start
                         (cljs-time.core/months 1)
                         (cljs-time.core/days 15)))

;; -- Default app-db value --------

(def default-db {:showing-events :upcoming-events
                 :showing-auth-page :signin
                 :auth0 {:polling? false}
                 :user-form {:dietary-restrictions []
                             :polling? false
                             :error-response nil}
                 :host-form {:polling? false
                             :error-response nil
                             :searched-hosts :not-searched}
                 :send-invites {:polling? false}
                 :upcoming-events {:stale? true}
                 :user-events {:stale? true}
                 :the-user {:stale? true}
                 :event-form {:happening-at-time {:hour 6
                                                  :minute 0
                                                  :time-of-day :pm}}
                 ;; event date
                 :possible-event-start possible-event-start
                 :possible-event-end possible-event-end
                 })

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

(defn js-str->clj [js-str]
  (try (-> js-str
           (js/JSON.parse)
           (js->clj :keywordize-keys true))
       (catch js/Object e
         "")))

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
              {:access-token
               (clojure.string/replace
                (clojure.string/trim
                 access-token) #"^\"|\"$" "")
               :profile
               (js-str->clj (parse-id-token id-token))})))))
