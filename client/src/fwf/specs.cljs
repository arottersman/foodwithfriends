(ns fwf.specs
  (:require [cljs.spec :as spec]
            [cljs-time.core]
            [fwf.db :as db]))

;; util
(def nullable-string? (spec/or :value string? :nil nil?))
(def nullable-int? (spec/or :value int? :nil nil?))
(def nullable-boolean? (spec/or :value boolean? :nil nil?))

;; Page State
(spec/def ::db/page keyword?)
(spec/def ::db/showing-events
  #{:upcoming-events
    :user-events})

;; API state
(spec/def ::db/polling? boolean?)
(spec/def ::db/stale? boolean?)

;; Auth
(spec/def ::db/access-token nullable-string?)
(spec/def ::db/email-verified nullable-boolean?)
(spec/def ::db/email nullable-string?)
(spec/def ::db/sub nullable-string?)
(spec/def ::db/profile
  (spec/keys
   :req [::db/email-verified
         ::db/sub
         ::db/email]
   :opt [::db/name]))
(spec/def ::db/auth0 (spec/keys
                      :opt [::db/polling?
                            ::db/access-token
                            ::db/profile
                            ::db/error-response]))

;; User
(spec/def ::db/host-id nullable-int? )
(spec/def ::db/auth0-id string?)
(spec/def ::db/user-id int?)
(spec/def ::db/name nullable-string?)
(spec/def ::db/dietary-restrictions
  (spec/and vector? (spec/every string?)))
(spec/def ::db/assigned-dish nullable-string?)

(spec/def ::db/user
  (spec/or
   :no-account keyword?
   :user (spec/keys
          :req [::db/auth0-id
                ::db/user-id
                ::db/name
                ::db/email
                ::db/dietary-restrictions
                ::db/assigned-dish]
          :opt [::db/host-id])))

(spec/def ::db/users (spec/* ::db/user))

;; logged in user
(spec/def ::db/the-user
  (spec/keys :opt [::db/user
                   ::db/stale?
                   ::db/error-response]))
;; User Form
(spec/def ::db/user-form
  (spec/keys
   :req [::db/dietary-restrictions
         ::db/name
         ::db/polling?]
   :opt [::db/error-response]))
;; User Detail
(spec/def ::user-detail ::db/user)
(spec/def ::user-assigned-dish string?)

;; Host
(spec/def ::db/address string?)
(spec/def ::db/city string?)
(spec/def ::db/state string?)
(spec/def ::db/zipcode string?)
(spec/def ::db/max-occupancy int?)
(spec/def ::db/host (spec/keys
                      :req [::db/host-id
                            ::db/address
                            ::db/city
                            ::db/state
                            ::db/zipcode
                            ::db/max-occupancy
                            ::db/users]))

(spec/def ::db/hosts (spec/* ::db/host))

;; Host Form
(spec/def ::db/searching? boolean?)
(spec/def ::db/searched-hosts
  (spec/or :not-searched keyword?
           :hosts ::db/hosts))
(spec/def ::db/host-form-fields
  (spec/keys
   :req [::db/address
         ::db/city
         ::db/state
         ::db/zipcode
         ::db/max-occupancy]))
(spec/def ::db/host-form-api
      (spec/keys :req [::db/polling?
                       ::db/searching?]
                 :opt [::db/error-response]))

(spec/def ::db/host-form
  (spec/keys
   :req [::db/searched-hosts
         ::db/host-form-fields
         ::db/host-form-api]))

;; Event
(spec/def ::db/title string?)
(spec/def ::db/description string?)
(spec/def ::db/happening-at string?)
(spec/def ::db/participants ::db/users)
(spec/def ::db/event-id int?)
(spec/def ::db/detail-id (spec/or :open ::db/event-id
                                  :closed nil?))
(spec/def ::db/rsvping? boolean?)
(spec/def ::db/event
  (spec/keys
   :req [::db/event-id
         ::db/host
         ::db/participants
         ::db/title
         ::db/description
         ::db/happening-at]))
(spec/def ::db/events (spec/* ::db/event))
(spec/def ::db/upcoming-events
  (spec/keys
   :req [::db/stale?
         ::db/rsvping?]
   :opt [::db/events
         ::db/detail-id
         ::db/error-response]))
(spec/def ::db/user-events
  (spec/keys
   :req [::db/stale?]
   :opt [::db/events
         ::db/detail-id
         ::db/error-response]))

;; Event form
(spec/def ::db/hour int?)
(spec/def ::db/minute int?)
(spec/def ::db/time-of-day #{:am
                             :pm})
(spec/def ::db/happening-at-date cljs-time.core/date?)
(spec/def ::db/happening-at-time
  (spec/keys
   :req [::db/hour
         ::db/minute
         ::db/time-of-day]))
(spec/def ::db/event-form
  (spec/keys :req [::db/title
                   ::db/description
                   ::db/happening-at-date
                   ::db/happening-at-time
                   ::db/polling?]
             :opt [::db/error-response]))

(spec/def ::db/possible-event-datetime cljs-time.core/date?)
(spec/def
  ::db/possible-event-start
  ::db/possible-event-datetime)
(spec/def
  ::db/possible-event-end
  ::db/possible-event-datetime)

;; Admin Send Invites
(spec/def ::db/num-hosts int?)
(spec/def ::db/succeeded? boolean?)
(spec/def ::db/send-invites
  (spec/keys :req [::db/polling?
                   ::db/succeeded?
                   ::db/num-hosts]
             :opt [::db/error-response]))

(spec/def ::db/db
  (spec/keys :opt [::db/page
                   ::db/showing-events
                   ::db/upcoming-events
                   ::db/user-events
                   ::db/auth0
                   ::db/send-invites
                   ::db/the-user
                   ::db/user-detail
                   ::db/user-assigned-dish
                   ::db/user-form
                   ::db/host-form
                   ::db/event-form
                   ::db/possible-event-start
                   ::db/possible-event-end]))
