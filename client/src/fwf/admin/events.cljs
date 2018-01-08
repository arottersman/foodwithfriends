(ns fwf.admin.events
  (:require
   [ajax.core :as ajax]
   [re-frame.core :refer [reg-event-db
                          reg-event-fx]]
   [fwf.events :refer [fwf-interceptors]]
   [fwf.db :as db]
   [fwf.constants :refer [api-url]]
   [fwf.api-helpers :refer [auth-header]]))

(reg-event-db
 :send-invites/set-num-hosts
 fwf-interceptors
 (fn [db [num-hosts-str]]
   (let [num-hosts (js/parseInt num-hosts-str)]
     (if (number? num-hosts)
       (assoc-in db [::db/send-invites ::db/num-hosts]
                 num-hosts)
       db))))

;; admin api

(reg-event-db
 :send-invites-success
 fwf-interceptors
 (fn [db _]
   (-> db
       (assoc-in [::db/send-invites ::db/succeeded?]
                 true)
       (assoc-in [::db/send-invites ::db/polling?]
                 false))))

(reg-event-db
 :bad-send-invites-response
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [::db/send-invites ::db/error-response]
                 response)
       (assoc-in [::db/send-invites ::db/polling?]
                 false))))

(reg-event-fx
 :send-invites
 fwf-interceptors
 (fn
   [{db :db} _]
   (let [access-token (-> db ::db/auth0 ::db/access-token)]
     {:http-xhrio {:method           :post
                   :headers          (auth-header access-token)
                   :uri              (str api-url
                                          "/admin/invites/"
                                          "?numHosts="
                                          (-> db
                                              ::db/send-invites
                                              ::db/num-hosts))
                   :format           (ajax/url-request-format)
                   :response-format  (ajax/detect-response-format)
                   :on-success       [:send-invites-success]
                   :on-failure       [:bad-send-invites-response]}
      :db (-> db
              (assoc-in [::db/send-invites ::db/polling?] true)
              (assoc-in [::db/send-invites ::db/succeeded?] true))})))
