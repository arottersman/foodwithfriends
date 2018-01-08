(ns fwf.event-list.subs
  (:require [re-frame.core :refer [reg-sub
                                   subscribe
                                   dispatch
                                   reg-sub-raw]]
            [reagent.core :as reagent]
            [reagent.ratom]
            [cljs-time.format]
            [clojure.string]
            [fwf.api-helpers :as api-helpers]
            [fwf.utils :refer [>evt <sub]]
            [fwf.db :as db]))

(defn- parse-date
  [date]
  (cond
    (nil? date)
    nil
    :else
    (cljs-time.format/unparse-local
     (cljs-time.format/formatter-local
      "E, M/d hh:mma")
     (cljs-time.format/parse
      api-helpers/server-response-date-formatter
      date))))

(defn- gmaps-url [address]
  (str "https://google.com/maps/place/"
       (clojure.string/replace address
        " " "+")))
(defn- str-address [{:keys [fwf.db/address
                            fwf.db/city
                            fwf.db/state
                            fwf.db/zipcode]}]
  (str address " " city ", " state " " zipcode))
(defn- rsvped? [user-id {:keys [fwf.db/participants]}]
  (some #{user-id} (map ::db/user-id participants)))
(defn- agg-dietary-restrictions [participants]
  (flatten (map ::db/dietary-restrictions participants)))
(defn- participant-str [participants max-occupancy]
  (str "(" (count participants) "/" max-occupancy ")"))
(defn- email-chain [participants]
  (clojure.string/join "," (map ::db/email participants)))

(defn prettify-event [event user-id user-host-id]
  (let [host-id (-> event ::db/host ::db/host-id)
        address-str (str-address (::db/host event))
        participants (::db/participants event)
        max-occupancy (-> event ::db/host ::db/max-occupancy)]
    (-> event
        (assoc ::db/rsvped? (rsvped? user-id event))
        (assoc ::db/your-house? (= host-id user-host-id))
        (assoc ::db/happening-at (parse-date (::db/happening-at event)))
        (assoc ::db/address-str address-str)
        (assoc ::db/google-maps-url (gmaps-url address-str))
        (assoc ::db/agg-dietary-restrictions (agg-dietary-restrictions participants))
        (assoc ::db/email-chain (email-chain participants))
        (assoc ::db/participant-str (participant-str participants max-occupancy))
     )))

;; --- subscriptions

(reg-sub
 :upcoming-events/error
 (fn [db _]
   (-> db ::db/upcoming-events ::db/error-response :status-message)))

(reg-sub
 :upcoming-events/rsvping?
 (fn [db _]
   (-> db ::db/upcoming-events ::db/rsvping?)))

(reg-sub
 :user-events/error
 (fn [db _]
   (-> db ::db/user-events ::db/error-response :status-message)))

(reg-sub
 :upcoming-events/detail-id
 (fn [db _]
   (-> db ::db/upcoming-events ::db/detail-id)))

(reg-sub
 :user-events/detail-id
 (fn [db _]
   (-> db ::db/user-events ::db/detail-id)))

(reg-sub
 :upcoming-events/stale?
 (fn [db _]
   (-> db ::db/upcoming-events ::db/stale?)))

(reg-sub
 :user-events/stale?
 (fn [db _]
   (-> db ::db/user-events ::db/stale?)))

(reg-sub
 :user-detail
 (fn [db _]
   (-> db ::db/user-detail)))

(reg-sub
 :user-assigned-dish
 (fn [db _]
   (-> db ::db/user-assigned-dish)))

;; -- Remote Dependent Subscriptions --
(reg-sub-raw
 :upcoming-events
 (fn [app-db _]
   (reagent.ratom/make-reaction
    (fn []
      (let [stale? (<sub [:upcoming-events/stale?])
            access-token (<sub [:auth0/access-token])]
        (if (and stale? access-token)
          (api-helpers/fetch-upcoming-events!
           {:access-token access-token
            :on-success #(>evt [:set-upcoming-events %])
            :on-failure #(>evt [:set-upcoming-events-error %])})))
      (get-in @app-db [::db/upcoming-events ::db/events])))))

(reg-sub-raw
 :user-events
 (fn [app-db _]
   (reagent.ratom/make-reaction
    (fn []
      (let [user-id (:user-id (<sub [:user]))
            stale? (<sub [:user-events/stale?])
            access-token (<sub [:auth0/access-token])]
        (if (and stale? user-id access-token)
            (api-helpers/fetch-user-events!
             {:user-id user-id
              :access-token access-token
              :on-success #(>evt [:set-user-events %])
              :on-failure #(>evt [:set-user-events-error %])})))
      (get-in @app-db [::db/user-events ::db/events])))))

;;--- subscription handlers
(reg-sub
 :pretty-user-events
 (fn [_ _]
   [(subscribe [:user-events])
    (subscribe [:user])])

 (fn [[events {:keys [fwf.db/user-id fwf.db/host-id]}]]
   (map #(prettify-event % user-id host-id) events)))

(reg-sub
 :pretty-upcoming-events
 (fn [_ _]
   [(subscribe [:upcoming-events])
    (subscribe [:user])])

 (fn [[events {:keys [fwf.db/user-id fwf.db/host-id]}]]
   (map #(prettify-event % user-id host-id) events)))
