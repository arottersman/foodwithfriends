(ns fwf.event-list.events
  (:require
   [ajax.core :as ajax]
   [re-frame.core :refer [reg-event-db
                          reg-event-fx]]
   [fwf.events :refer [fwf-interceptors
                       ->clear-auth-if-needed]]
   [fwf.db :as db]
   [fwf.constants :refer [api-url]]
   [fwf.api-helpers :refer [auth-header
                            parse-event
                            parse-assigned-dish]]))

;; page
(reg-event-db
 :set-showing-events
 fwf-interceptors
 (fn [db [new-showing]]
   (assoc db ::db/showing-events new-showing)))

;; active list detail
(reg-event-db
 :set-upcoming-event-detail
 fwf-interceptors
 (fn [db [detail-id]]
   (assoc-in db [::db/upcoming-events ::db/detail-id]
             detail-id)))

(reg-event-db
 :set-user-event-detail
 fwf-interceptors
 (fn [db [detail-id]]
   (assoc-in db [::db/user-events ::db/detail-id]
             detail-id)))

;; modals
(reg-event-db
 :set-user-detail
 fwf-interceptors
 (fn [db [user]]
   (assoc db ::db/user-detail user)))

(reg-event-db
 :clear-assigned-dish-modal
 fwf-interceptors
 (fn [db _]
   (assoc db ::db/user-assigned-dish nil)))

;; event list api
(defn- handle-401-if-needed
  [db error-response]
  (if (= 401 (:status error-response))
    (do
      (assoc db ::db/page :login)
      (assoc-in db [::db/auth0 ::db/access-token] "")
      (assoc-in db [::db/auth0 ::db/profile] {}))
    db))

(reg-event-db
 :set-upcoming-events
 fwf-interceptors
 (fn [db [events-response]]
   (-> db
       (assoc-in [::db/upcoming-events ::db/stale?]
                 false)
       (assoc-in [::db/upcoming-events ::db/polling?]
                 false)
       (assoc-in [::db/upcoming-events ::db/events]
                 (map parse-event events-response)))))

(reg-event-db
 :set-upcoming-events-error
 [fwf-interceptors
  ->clear-auth-if-needed]
 (fn [db [error-response]]
   (as-> db d
     (assoc-in d [::db/upcoming-events ::db/stale?]
               false)
     (assoc-in d [::db/upcoming-events ::db/polling?]
               false)
     (assoc-in d [::db/upcoming-events ::db/error-response]
               error-response)
     (handle-401-if-needed d error-response))))

(reg-event-db
 :set-upcoming-events-polling
 fwf-interceptors
 (fn [db _]
   (assoc-in db [::db/upcoming-events ::db/polling?] true)))

(reg-event-db
 :set-user-events
 fwf-interceptors
 (fn [db [events-response]]
   (-> db
       (assoc-in [::db/user-events ::db/stale?] false)
       (assoc-in [::db/user-events ::db/polling?] false)
       (assoc-in [::db/user-events ::db/events]
                 (map parse-event events-response)))))

(reg-event-db
 :set-user-events-error
 [fwf-interceptors
  ->clear-auth-if-needed]
 (fn [db [error-response]]
   (as-> db d
     (assoc-in d [::db/user-events ::db/stale?]
               false)
     (assoc-in d [::db/user-events ::db/polling?]
               false)
     (assoc-in d [::db/user-events ::db/error-response]
               error-response)
     (handle-401-if-needed d error-response))))

(reg-event-db
 :set-user-events-polling
 fwf-interceptors
 (fn [db _]
   (assoc-in db [::db/user-events ::db/polling?] true)))

;; rsvp api
(reg-event-db
 :rsvp-success
 fwf-interceptors
 (fn [db [response]]
   (let [user-id (-> db
                     ::db/the-user
                     ::db/user
                     ::db/user-id)]
     (-> db
         (assoc ::db/user-assigned-dish
                (parse-assigned-dish
                 response
                 user-id))
         (assoc-in [::db/user-events ::db/stale?]
                   true)
         (assoc-in [::db/upcoming-events ::db/stale?]
                   true)
         (assoc-in [::db/upcoming-events ::db/rsvping?]
                   false)))))

(reg-event-db
 :bad-rsvp-response
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [::db/upcoming-events
                  ::db/error-response]
                 response)
       (assoc-in [::db/upcoming-events ::db/rsvping?]
                 false))))

;; event fx
(reg-event-fx
 :rsvp
 (fn
   [{db :db} [_ event-id]]
   (let [access-token (-> db ::db/auth0 ::db/access-token)
         user-id (-> db ::db/the-user ::db/user ::db/user-id)
         uri (str api-url "/events/add-participant/"
                  "?userId=" user-id
                  "&eventId=" event-id)]
     {:http-xhrio {:method           :post
                   :headers          (auth-header access-token)
                   :uri               uri
                   :keywords?         true
                   :format            (ajax/url-request-format)
                   :response-format   (ajax/json-response-format
                                       {:keywords? true})
                   :on-success        [:rsvp-success]
                   :on-failure        [:bad-rsvp-response]}
      :db (assoc-in db [::db/upcoming-events ::db/rsvping?] true)})))
