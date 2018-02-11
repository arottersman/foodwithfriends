(ns fwf.event-form.subs
  (:require [re-frame.core :refer [reg-sub
                                   subscribe]]
             [reagent.core :as reagent]
             [reagent.ratom]
             [cljs-time.format]
             [cljs-time.periodic]
             [fwf.db :as db]))

;; event-form dates
(reg-sub
 :possible-event-start
 (fn [db _]
   (db ::db/possible-event-start)))

(reg-sub
 :possible-event-end
 (fn [db _]
   (db ::db/possible-event-end)))

;; event-form

(reg-sub
 :event-form/title
 (fn [db _]
   (-> db ::db/event-form ::db/title)))

(reg-sub
 :event-form/event-id
 (fn [db _]
   (-> db ::db/event-form ::db/event-id)))

(reg-sub
 :event-form/happening-at-date
 (fn [db _]
   (-> db ::db/event-form ::db/happening-at-date)))

(reg-sub
 :event-form/happening-at-time
 (fn [db _]
   (-> db ::db/event-form ::db/happening-at-time)))

(reg-sub
 :event-form/polling?
 (fn [db _]
   (-> db ::db/event-form ::db/polling?)))

(reg-sub
 :event-form/error-response
 (fn [db _]
   (-> db ::db/event-form ::db/error-response)))

;; -- subscription handlers ---
(reg-sub
 :event-form/error-string
 (fn [_ _]
   [(subscribe [:event-form/error-response])])
 (fn [[event-form/error-response] _]
   (if error-response
     "Uh-oh, something went wrong! Are you sure it's your turn to create an event?")))

(reg-sub
 :event-form/grouped-date-options
 (fn [_ _]
   [(subscribe [:possible-event-start])
    (subscribe [:possible-event-end])
    (subscribe [:event-form/happening-at-date])])
 (fn [[possible-start possible-end
       happening-at-date]]
   (let [start               (cljs-time.core/earliest
                              possible-start happening-at-date)
         end                 (cljs-time.core/latest
                              possible-end happening-at-date)
         possible-dates      (cljs-time.periodic/periodic-seq
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
                     start-padding (repeat (- first-day 0)
                                           :not-in-month)
                     end-padding (repeat (- 6 last-day)
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
 (fn [[{:keys [fwf.db/hour
               fwf.db/minute
               fwf.db/time-of-day]}]]
   (let [hour-str (if (< hour 10)
                    (str "0" hour)
                    (str hour))
         minute-str (if (< minute 10)
                      (str "0" minute)
                      (str minute))]
     {::db/hour hour-str
      ::db/minute minute-str
      ::db/time-of-day time-of-day})))

(reg-sub
 :event-form/valid?
 (fn [_ _]
   [(subscribe [:event-form/title])
    (subscribe [:event-form/happening-at-date])
    (subscribe [:event-form/happening-at-time])])
 (fn [[title happening-at-date {:keys [fwf.db/hour]}]]
   (and (every? not-empty [title])
        (not (nil? happening-at-date))
        (> hour 0))))
