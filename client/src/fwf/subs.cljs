(ns fwf.subs
  (:require [re-frame.core :refer [reg-sub
                                   subscribe
                                   dispatch
                                   reg-sub-raw]]
            [reagent.core :as reagent]
            [reagent.ratom]
            [cljs-time.format]
            [cljs-time.periodic]
            [clojure.string]
            [fwf.api-helpers :as api-helpers]
            [fwf.utils :refer [>evt <sub]]))

;; -- Helpers -----------------------
(defn parse-date
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

(defn prettify-event [event user-id user-host-id]
  (let [{:keys [host-id
                address
                city
                state
                zipcode]} (:host event)
        address-str (str address " "
                         city ", "
                         state " "
                         zipcode)]
    (-> event
        (assoc :rsvped? (some #{user-id}
                       (map :user-id (:participants event))))
        (assoc :your-house? (= host-id user-host-id))
        (assoc :happening-at (parse-date (:happening-at event)))
        (assoc :address-str address-str)
        (assoc :google-maps-url
               (str "https://google.com/maps/place/"
                    (clojure.string/replace
                     address-str
                     " " "+")))
        (assoc :email-chain (clojure.string/join
                             ","
                             (map :email (:participants event)))))))


;; -- Extractors --------------------

(reg-sub
 :page
 (fn [db _]
   (:page db)))

(reg-sub
 :auth0/error
 (fn [db _]
   (-> db :auth0 :error)))

(reg-sub
 :auth0/profile
 (fn [db _]
   (-> db :auth0 :profile)))

(reg-sub
 :auth0/access-token
 (fn [db _]
   (-> db :auth0 :access-token)))
(reg-sub
 :auth0/polling?
 (fn [db _]
   (-> db :auth0 :polling?)))

(reg-sub
 :user-form/name
 (fn [db _]
   (db :user-form :name)))
(reg-sub :user-form/email
 (fn [db _]
   (db :user-form :email)))

(reg-sub
 :user-form/dietary-restrictions
 (fn [db _]
   (-> db :user-form :dietary-restrictions)))

(reg-sub
 :user-form/error-response
 (fn [db _]
   (-> db :user-form :error-response)))

(reg-sub
 :user-form/polling?
 (fn [db _]
   (-> db :user-form :polling?)))

(reg-sub
 :host-form/address
 (fn [db _]
   (-> db :host-form :address)))

(reg-sub
 :host-form/city
 (fn [db _]
   (-> db :host-form :city)))

(reg-sub
 :host-form/state
 (fn [db _]
   (-> db :host-form :state)))

(reg-sub
 :host-form/zipcode
 (fn [db _]
   (-> db :host-form :zipcode)))

(reg-sub
 :host-form/max-occupancy
 (fn [db _]
   (-> db :host-form :max-occupancy)))

(reg-sub
 :host-form/searched-hosts
 (fn [db _]
   (-> db :host-form :searched-hosts)))

(reg-sub
 :host-form/polling?
 (fn [db _]
   (-> db :host-form :polling?)))

(reg-sub
 :host-form/error-response
 (fn [db _]
   (-> db :host-form :error-response)))

(reg-sub
 :showing-events
 (fn [db _]
   (-> db :showing-events)))

(reg-sub
 :user/error
 (fn [db _]
   (-> db :the-user :error)))

(reg-sub
 :upcoming-events/error
 (fn [db _]
   (-> db :upcoming-events :error)))

(reg-sub
 :upcoming-events/rsvping?
 (fn [db _]
   (-> db :upcoming-events :rsvping?)))

 (reg-sub
  :user-events/error
  (fn [db _]
    (-> db :user-events :error)))

(reg-sub
 :upcoming-events/detail-id
 (fn [db _]
   (-> db :upcoming-events :detail-id)))

(reg-sub
 :user-events/detail-id
 (fn [db _]
   (-> db :user-events :detail-id)))


(reg-sub
 :upcoming-events/stale?
 (fn [db _]
   (-> db :upcoming-events :stale?)))

(reg-sub
 :user-events/stale?
 (fn [db _]
   (-> db :user-events :stale?)))

(reg-sub
 :user/stale?
 (fn [db _]
   (-> db :the-user :stale?)))

(reg-sub
 :user-detail
 (fn [db _]
   (-> db :user-detail)))

(reg-sub
 :user-assigned-dish
 (fn [db _]
   (-> db :user-assigned-dish)))

(reg-sub
 :send-invites/num-hosts
 (fn [db _]
   (-> db :send-invites :num-hosts)))

(reg-sub
 :send-invites/succeeded?
 (fn [db _]
   (-> db :send-invites :succeeded?)))

(reg-sub
 :send-invites/error
 (fn [db _]
   (-> db :send-invites :error)))

(reg-sub
 :send-invites/polling?
 (fn [db _]
   (-> db :send-invites :polling?)))

;; event-form dates
(reg-sub
 :possible-event-start
 (fn [db _]
   (db :possible-event-start)))

(reg-sub
 :possible-event-end
 (fn [db _]
   (db :possible-event-end)))

;; event-form

(reg-sub
 :event-form/title
 (fn [db _]
   (-> db :event-form :title)))

(reg-sub
 :event-form/description
 (fn [db _]
   (-> db :event-form :description)))

(reg-sub
 :event-form/happening-at-date
 (fn [db _]
   (-> db :event-form :happening-at-date)))

(reg-sub
 :event-form/happening-at-time
 (fn [db _]
   (-> db :event-form :happening-at-time)))

(reg-sub
 :event-form/polling?
 (fn [db _]
   (-> db :event-form :polling?)))

(reg-sub
 :event-form/error-response
 (fn [db _]
   (-> db :event-form :error-response)))

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
      (get-in @app-db [:upcoming-events :events])))))

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
      (get-in @app-db [:user-events :events])))))

(reg-sub-raw
 :user
 (fn [app-db _]
  (reagent.ratom/make-reaction
   (fn []
     (let [stale? (<sub [:user/stale?])
           access-token (<sub [:auth0/access-token])
           {:keys [sub]} (<sub [:auth0/profile])]
       (if (and stale? sub access-token)
         (api-helpers/fetch-user!
          {:auth0-id sub
           :access-token access-token
           :on-success #(>evt [:set-user %])
           :on-failure #(>evt [:set-user-error %])})))
      (get-in @app-db [:the-user :user])))))

;; -- Subscription handlers ---------

(reg-sub
 :user-form/dietary-restrictions-with-blank
 (fn [_ _]
   [(subscribe [:user-form/dietary-restrictions])])
 (fn [[user-form/dietary-restrictions] _]
   (conj dietary-restrictions "")))

(reg-sub
 :user-form/error-string
 (fn [_ _]
   [(subscribe [:user-form/error-response])])
 (fn [[user-form/error-response] _]
   (if error-response
     "Uh-oh, something went wrong!")))

(reg-sub
 :host-form/error-string
 (fn [_ _]
   [(subscribe [:host-form/error-response])])
 (fn [[host-form/error-response] _]
   (if error-response
     "Uh-oh, something went wrong!")))

(reg-sub
 :event-form/error-string
 (fn [_ _]
   [(subscribe [:event-form/error-response])])
 (fn [[event-form/error-response] _]
   (if error-response
     "Uh-oh, something went wrong!")))

(reg-sub
 :host-form/search-valid?
 (fn [_ _]
   [(subscribe [:host-form/address])
    (subscribe [:host-form/city])
    (subscribe [:host-form/state])
    (subscribe [:host-form/zipcode])])
 (fn [[host-form/address
       host-form/city
       host-form/state
       host-form/zipcode]]
   (every? not-empty [address
                      city
                      state
                      zipcode])))

(reg-sub
 :host-form/create-host-valid?
 (fn [_ _]
   [(subscribe [:host-form/max-occupancy])])
 (fn [[host-form/max-occupancy]]
   (> max-occupancy 0)))

(reg-sub
 :pretty-user-events
 (fn [_ _]
   [(subscribe [:user-events])
    (subscribe [:user])])

 (fn [[events {:keys [user-id host-id]}]]
   (map #(prettify-event % user-id host-id) events)))

(reg-sub
 :pretty-upcoming-events
 (fn [_ _]
   [(subscribe [:upcoming-events])
    (subscribe [:user])])

 (fn [[events {:keys [user-id host-id]}]]
   (map #(prettify-event % user-id host-id) events)))


;; event dates

(defn- start-day-of-week [datetimes])

(reg-sub
 :event-form/grouped-date-options
 (fn [_ _]
   [(subscribe [:possible-event-start])
    (subscribe [:possible-event-end])])
 (fn [[start end]]
   (let [possible-dates      (cljs-time.periodic/periodic-seq
                              start end
                              (cljs-time.core/days 1))
         grouped-by-month    (group-by cljs-time.core/month possible-dates)]
     (reduce
      (fn [calendarized-shape
           [month [first-datetime & rest-datetimes :as all]]]
        (assoc calendarized-shape month
               (let [first-day
                     (cljs-time.core/day-of-week
                      first-datetime)
                     last-day
                     (cljs-time.core/day-of-week
                      (last all))
                     start-padding (repeat (- first-day 1)
                                           :not-in-month)
                     end-padding (repeat (- 7 last-day)
                                         :not-in-month)]
                 (partition
                  7
                  (flatten [start-padding all end-padding])))))
      {}
      grouped-by-month))))

(reg-sub
 :event-form/happening-at-time-strs
 (fn [_ _]
   [(subscribe [:event-form/happening-at-time])])
 (fn [[{:keys [hour minute time-of-day]}]]
   (let [hour-str (if (< hour 10)
                    (str "0" hour)
                    (str hour))
         minute-str (if (< minute 10)
                      (str "0" minute)
                      (str minute))]
     {:hour hour-str
      :minute minute-str
      :time-of-day time-of-day})))

(reg-sub
 :event-form/valid?
 (fn [_ _]
   [(subscribe [:event-form/title])
    (subscribe [:event-form/happening-at-date])
    (subscribe [:event-form/happening-at-time])])
 (fn [[title happening-at-date {:keys [hour]}]]
   (and (every? not-empty [title])
        (not (nil? happening-at-date))
        (> hour 0))))
