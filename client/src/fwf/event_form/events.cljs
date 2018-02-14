(ns fwf.event-form.events
  (:require
   [ajax.core :as ajax]
   [cljs-time.core]
   [cljs-time.format]
   [re-frame.core :refer [reg-event-db
                          reg-event-fx]]
   [fwf.events :refer [fwf-interceptors]]
   [fwf.db :as db]
   [fwf.constants :refer [api-url]]
   [fwf.api-helpers :refer [auth-header
                            server-response-date-formatter
                            server-request-date-formatter]]))

(defn- construct-datetime
  [datetime {:keys [fwf.db/hour
                    fwf.db/minute
                    fwf.db/time-of-day]}]
  "Constructs a proper utc datetime object from a
   datetime and a map of the time given in hours,
   minutes and :am/:pm"
  (let [date-midnight (cljs-time.core/at-midnight datetime)
        date-with-time (cljs-time.core/plus
                        date-midnight
                        (cljs-time.core/hours hour)
                        (cljs-time.core/minutes minute)
                        (if (= time-of-day :pm)
                          (cljs-time.core/hours 12)))]
    (cljs-time.format/unparse
     server-request-date-formatter date-with-time)))

(defn- hour-of-datetime [datetime]
  (js/parseInt
   (cljs-time.format/unparse
    (cljs-time.format/formatter "h")
    datetime)))

(defn- time-of-day-of-datetime [datetime]
  (keyword
   (cljs-time.format/unparse
    (cljs-time.format/formatter "a")
    datetime)))

(reg-event-db
 :event-form/update-title
 fwf-interceptors
 (fn [db [new-title]]
   (assoc-in db [::db/event-form ::db/title]
             new-title)))

(reg-event-db
 :event-form/update-description
 fwf-interceptors
 (fn [db [new-description]]
   (assoc-in db [::db/event-form ::db/description]
             new-description)))

(reg-event-db
 :event-form/toggle-email-participants
 fwf-interceptors
 (fn [db _]
   (let [email-participants?
         (-> db
             ::db/event-form
             ::db/email-participants?)]
     (assoc-in db [::db/event-form ::db/email-participants?]
               (not email-participants?)))))

(reg-event-db
 :event-form/update-happening-at-date
 fwf-interceptors
 (fn [db [new-date]]
   (assoc-in db [::db/event-form ::db/happening-at-date]
             new-date)))

(reg-event-db
 :event-form/update-happening-at-time
 fwf-interceptors
 (fn [db [hour-str minute-str time-of-day]]
   (let [hour (js/parseInt hour-str)
         minute (js/parseInt minute-str)
         valid? (and (>= minute 0)
                     (<= hour 12)
                     (< minute 60))]
     (if valid?
       (assoc-in db
                 [::db/event-form ::db/happening-at-time]
                 {::db/hour hour
                  ::db/minute minute
                  ::db/time-of-day (keyword time-of-day)})
       db))))

(reg-event-db
 :event-form/from-event
 fwf-interceptors
 (fn [db [{:keys [fwf.db/event-id
                  fwf.db/title
                  fwf.db/description
                  fwf.db/happening-at]}]]
   (let [happening-at-date (cljs-time.format/parse
                                server-response-date-formatter
                                happening-at)
         happening-at-time {::db/hour
                            (hour-of-datetime happening-at-date)
                            ::db/minute
                            (cljs-time.core/minute
                             happening-at-date)
                            ::db/time-of-day
                            (time-of-day-of-datetime
                             happening-at-date)}]
     (assoc db ::db/event-form
            (assoc (::db/event-form db)
                   ::db/event-id event-id
                   ::db/title title
                   ::db/description description
                   ::db/happening-at-date happening-at-date
                   ::db/happening-at-time happening-at-time)))))
;; api
(reg-event-db
 :create-event-success
 fwf-interceptors
 (fn [db [response]]
   (js/window.location.assign "/#/")
   (-> db
       (assoc ::db/page :events)
       (assoc-in [::db/upcoming-events ::db/stale?]
                 true)
       (assoc-in [::db/user-events ::db/stale?]
                 true)
       (assoc-in [::db/event-form ::db/polling?]
                 false))))

(reg-event-db
 :bad-create-event-response
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [::db/event-form ::db/error-response]
                 response)
       (assoc-in [::db/event-form ::db/polling?]
                 false))))

(defn- event-form->base-http-params
  [event-form host-id]
  {:title
   (::db/title event-form)
   :description ""
   :happeningAt
   (construct-datetime
    (::db/happening-at-date
     event-form)
    (::db/happening-at-time
     event-form))
   :host {:hostId host-id}})

(defn- event-form->http-xhrio [db]
  (let [access-token (-> db ::db/auth0 ::db/access-token)]
  {:headers          (auth-header access-token)
   :uri              (str api-url "/events/")
   :keywords?        true
   :format           (ajax/json-request-format)
   :response-format   (ajax/json-response-format
                       {:keywords? true})
   :on-success        [:create-event-success]
   :on-failure        [:bad-create-event-response]}))

;; event-fx
(reg-event-fx
 :create-event
 (fn
   [{db :db} _]
   (let [event-form (::db/event-form db)
         host-id (-> db ::db/the-user ::db/user ::db/host-id)]
     {:http-xhrio (assoc (event-form->http-xhrio db)
                         :method           :put
                         :params           (event-form->base-http-params
                                            event-form host-id))
      :db (assoc-in db [::db/event-form ::db/polling?] true)})))

(reg-event-fx
 :edit-event
 (fn
   [{db :db} _]
   (let [event-form (::db/event-form db)
         host-id (-> db ::db/the-user ::db/user ::db/host-id)
         params  (assoc (event-form->base-http-params
                         event-form
                         host-id)
                        :eventId (::db/event-id event-form))]
   {:http-xhrio (assoc (event-form->http-xhrio db)
                       :method           :post
                       :uri              (str api-url "/events/"
                                              "?emailParticipants="
                                              (::db/email-participants?
                                               event-form))
                       :params           params)
    :db (assoc-in db [::db/event-form ::db/polling?] true)})))

(reg-event-fx
 :cant-host
 (fn
   [{db :db} _]
   (let [host-id (-> db ::db/the-user ::db/user ::db/host-id)]
     {:http-xhrio (assoc (event-form->http-xhrio db)
                         :method           :post
                         :uri              (str api-url
                                                "/events/cant-host/"
                                                "?hostId=" host-id)
                         :on-success        [:create-event-success]
                         :on-failure        [:bad-create-event-response])
      :db (assoc-in db [::db/event-form ::db/polling?] true)})))
