(ns fwf.events
  (:require
   [ajax.core :as ajax]
   [day8.re-frame.http-fx]
   [re-frame.core :refer [reg-event-db
                          reg-event-fx
                          reg-cofx
                          inject-cofx
                          path
                          trim-v
                          after
                          debug]]
   [cljs.spec :as spec]
   [cljs-time.core]
   [cljs-time.format]
   [clojure.string :as str]
   [fwf.constants :refer [access-token-ls-key
                          api-url
                          profile-ls-key
                          auth0-client-id
                          auth0-domain
                          auth0-audience
                          auth0-redirect-uri
                          auth0-state]]
   [fwf.utils :refer [parse-id-token
                      navigate-to!]]
   [fwf.api-helpers :refer [auth-header
                            server-request-date-formatter
                            parse-user
                            parse-host
                            parse-event
                            parse-assigned-dish]]
   [fwf.db :refer [default-db
                   auth0->local-store
                   wipe-local-store
                   wipe-local-store-if-401]]))

;; -- Interceptors --------------------

(defn check-and-throw
  "Throws an exception if `db` doesn't match the spec `a-spec`"
  [a-spec db]
  (when-not (spec/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (spec/explain-str a-spec db)) {}))))

(def check-spec-interceptor (after (partial check-and-throw
                                            :fwf.db/db)))

(def ->auth0-local-store (after auth0->local-store))

(def ->wipe-local-store (after wipe-local-store))

(def ->clear-auth-if-needed (after wipe-local-store-if-401))

(def ->events-uri (after #(navigate-to! "/")))

(def fwf-interceptors [check-spec-interceptor
                       (when ^boolean js/goog.DEBUG debug)
                       trim-v])

; -- Helpers ----------------------

(defn construct-datetime [datetime {:keys [hour minute time-of-day]}]
  "Constructs a proper utc datetime object from a datetime and a map of the time"
  (let [date-midnight (cljs-time.core/at-midnight datetime)
        date-with-time (cljs-time.core/plus date-midnight
                             (cljs-time.core/hours hour)
                             (cljs-time.core/minutes minute)
                             (if (= time-of-day :pm)
                               (cljs-time.core/hours 12)))]
    (cljs-time.format/unparse server-request-date-formatter date-with-time)))

; -- Event Handlers ---------------
(reg-event-fx
 :initialize-db

 [(inject-cofx :local-store-auth0)
  check-spec-interceptor]

 (fn [{:keys [db local-store-auth0]} _]
   {:db (merge db (assoc default-db
                         :auth0 local-store-auth0))}))

(reg-event-db
 :set-page

 [check-spec-interceptor (path :page) trim-v]

 (fn [old-page [new-page-kw]]
   new-page-kw))

(reg-event-db
 :signout

 [fwf-interceptors
  ->wipe-local-store]

 (fn [db _]
   (assoc default-db :page :events)))

(reg-event-db
 :set-login-error
 fwf-interceptors
 (fn [db [error-message]]
   (assoc-in db [:auth0 :error] error-message)))

(reg-event-db
 :user/set-user-id
 fwf-interceptors
 (fn [db [user-id]]
   (assoc-in db [:the-user :user :user-id] user-id)))

(reg-event-db
 :user-form/update-name
 [check-spec-interceptor
  trim-v
  (path :user-form :name)]
 (fn [_ [new-value]]
   new-value))

(reg-event-db
 :user-form/update-dietary-restrictions
 [check-spec-interceptor
  trim-v
  (path :user-form :dietary-restrictions)]
 (fn [restrictions [idx new-value]]
   (filterv not-empty (assoc restrictions idx new-value))))

(reg-event-db
 :host-form/update-address
 fwf-interceptors
 (fn [db [new-address]]
   (assoc-in db [:host-form
                 :address] new-address)))
(reg-event-db
 :host-form/update-city
 fwf-interceptors
 (fn [db [new-city]]
   (assoc-in db [:host-form
                 :city] new-city)))

(reg-event-db
 :host-form/update-state
 fwf-interceptors
 (fn [db [new-state]]
      (assoc-in db [:host-form
                    :state] new-state)))


(reg-event-db
 :host-form/update-zipcode
 fwf-interceptors
 (fn [db [new-zipcode]]
      (assoc-in db [:host-form
                    :zipcode] new-zipcode)))

(reg-event-db
 :host-form/update-max-occupancy
 fwf-interceptors
 (fn [db [new-max-occupancy]]
   (assoc-in db [:host-form
                 :max-occupancy] new-max-occupancy)))

(reg-event-db
 :set-showing-events
 fwf-interceptors
 (fn [db [new-showing]]
   (assoc db :showing-events new-showing)))

(reg-event-db
 :set-upcoming-event-detail
 fwf-interceptors
 (fn [db [detail-id]]
   (assoc-in db [:upcoming-events :detail-id] detail-id)))

(reg-event-db
 :set-user-event-detail
 fwf-interceptors
 (fn [db [detail-id]]
   (assoc-in db [:user-events :detail-id] detail-id)))

(reg-event-db
 :set-user-detail
 fwf-interceptors
 (fn [db [user]]
   (assoc db :user-detail user)))

(reg-event-db
 :clear-assigned-dish-modal
 fwf-interceptors
 (fn [db _]
   (assoc db :user-assigned-dish nil)))

(reg-event-db
 :event-form/update-title
 fwf-interceptors
 (fn [db [new-title]]
   (assoc-in db [:event-form :title] new-title)))

(reg-event-db
 :event-form/update-description
 fwf-interceptors
 (fn [db [new-description]]
   (assoc-in db [:event-form :description] new-description)))

(reg-event-db
 :event-form/update-happening-at-date
 fwf-interceptors
 (fn [db [new-date]]
   (assoc-in db [:event-form :happening-at-date] new-date)))

(reg-event-db
 :event-form/update-happening-at-time
 fwf-interceptors
 (fn [db [hour-str minute-str time-of-day]]
   (let [hour (js/parseInt hour-str)
         minute (js/parseInt minute-str)]
     (if (and (>= minute 0)
              (<= hour 12)
              (< minute 60))
       (assoc-in db [:event-form :happening-at-time]
                 {:hour hour
                  :minute minute
                  :time-of-day time-of-day})
       db))))

(reg-event-db
 :send-invites/set-num-hosts
 fwf-interceptors
 (fn [db [num-hosts]]
   (assoc-in db [:send-invites :num-hosts] num-hosts)))

;; -- API Events and FX -----------

(reg-event-db
 :set-upcoming-events
 fwf-interceptors
 (fn [db [events-response]]
   (-> db
       (assoc-in [:upcoming-events :stale?] false)
       (assoc-in [:upcoming-events :events]
                 (map parse-event events-response)))))

(reg-event-db
 :set-upcoming-events-error
 [fwf-interceptors
  ->clear-auth-if-needed]
 (fn [db [error-response]]
   (as-> db d
       (assoc-in d [:upcoming-events :stale?] false)
       (assoc-in d [:upcoming-events :error] error-response)
       (if (= 401 (:status error-response))
         (do
           (assoc d :page :login)
           (assoc-in d [:auth0 :access-token] "")
           (assoc-in d [:auth0 :profile] {}))))))

(reg-event-db
 :set-user-events
 fwf-interceptors
 (fn [db [events-response]]
   (-> db
       (assoc-in [:user-events :stale?] false)
       (assoc-in [:user-events :events]
                 (map parse-event events-response)))))

(reg-event-db
 :set-user-events-error
 [fwf-interceptors
  ->clear-auth-if-needed]
 (fn [db [error-response]]
   (as-> db d
       (assoc-in d [:user-events :stale?] false)
       (assoc-in d [:user-events :error] error-response)
       (if (= 401 (:status error-response))
         (do
           (assoc d :page :login)
           (assoc-in d [:auth0 :access-token] "")
           (assoc-in d [:auth0 :profile] {}))))))

(reg-event-db
 :set-user
 fwf-interceptors
 (fn [db [user-response]]
   (let [user (parse-user user-response)
         host-id (:host-id user)]
     (-> db
         (assoc-in [:the-user :user] user)
         (assoc-in [:the-user :stale?] false)
         (cond-> (or (nil? host-id)
                     (= 0 host-id))
           (assoc :page :add-host-to-user))))))

(reg-event-db
 :set-user-error
 [fwf-interceptors
  ->clear-auth-if-needed]
 (fn [db [error-response]]
   (let [status (:status error-response)]
   (-> db
       (assoc-in [:the-user :stale?] false)
       (cond-> (= status 404)
         (->
          (assoc :page :create-user)
          (assoc-in [:the-user :user] :no-account)))
       (cond-> (not= status 404)
             (assoc-in [:the-user :error] error-response))
       (cond-> (= status 401)
         (-> (assoc :page :login)
             (assoc-in [:auth0 :access-token] "")
             (assoc-in [:auth0 :profile] {})))))))

(reg-event-db
 :bad-create-user-response
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [:user-form :error-response]
                 response)
       (assoc-in [:user-form :polling?] false))))

(reg-event-db
 :create-user-success
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [:the-user :user] (parse-user response))
       (assoc :page :add-host-to-user)
       (assoc-in [:user-form :polling?] false))))

(reg-event-db
 :search-hosts-success
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [:host-form :searched-hosts ]
                 (map parse-host response))
       (assoc-in [:host-form :polling?] false))))

(reg-event-db
 :bad-search-hosts-response
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [:host-form :error-response]
                 response)
       (assoc-in [:host-form :polling?] false))))

(reg-event-db
 :create-host-success
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [:the-user :user :host-id] (:host-id (parse-host response)))
       (assoc :page :events)
       (assoc-in [:host-form :polling?] false))))

(reg-event-db
 :bad-create-host-response
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [:host-form :error-response]
                 response)
       (assoc-in [:host-form :polling?] false))))

(reg-event-db
 :add-user-to-host-success
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc :page :events)
       (assoc-in [:the-user :stale?] true)
       (assoc-in [:host-form :polling?] false))))

(reg-event-db
 :bad-add-user-to-host-response
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [:host-form :error-response]
                 response)
       (assoc-in [:host-form :polling?] false))))

(reg-event-db
 :create-event-success
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc :page :events)
       (assoc-in [:upcoming-events :stale?] true)
       (assoc-in [:user-events :stale?] true)
       (assoc-in [:event-form :polling?] false))))

(reg-event-db
 :bad-create-event-response
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [:event-form :error-response]
                 response)
       (assoc-in [:event-form :polling?] false))))

(reg-event-db
 :rsvp-success
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc :user-assigned-dish
              (parse-assigned-dish response
                                   (-> db
                                       :the-user
                                       :user
                                       :user-id)))
       (assoc-in [:user-events :stale?] true)
       (assoc-in [:upcoming-events :stale?] true)
       (assoc-in [:upcoming-events :rsvping?] false))))

(reg-event-db
 :bad-rsvp-response
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [:upcoming-events :error] response)
       (assoc-in [:upcoming-events :rsvping?] false))))

(reg-event-db
 :get-auth0-tokens-success
 [fwf-interceptors
  ->auth0-local-store
  ->events-uri]
 (fn [db [{:keys [access_token id_token]}]]
   (-> db
       (assoc-in [:auth0 :access-token] access_token)
       (assoc-in [:auth0 :profile] (parse-id-token id_token))
       (assoc-in [:auth0 :polling?] false)
       (assoc :page :events))))

(reg-event-db
 :bad-get-auth0-tokens-response
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [:auth0 :error] (:status-message response))
       (assoc-in [:auth0 :polling?] false))))

(reg-event-db
 :send-invites-success
 fwf-interceptors
 (fn [db _]
   (-> db
       (assoc-in [:send-invites :succeeded?] true)
       (assoc-in [:send-invites :polling?] false))))

(reg-event-db
 :bad-send-invites-response
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [:send-invites :error]
                 response)
       (assoc-in [:send-invites :polling?] false))))


(reg-event-fx
 :create-user
 (fn
   [{db :db} _]
   {:http-xhrio {:method           :put
                 :headers          (auth-header (-> db
                                                    :auth0
                                                    :access-token))
                 :uri              (str api-url "/users/")
                 :format           (ajax/json-request-format)
                 :keywords?        true
                 :params           {:name
                                    (let
                                        [profile-name
                                         (-> db
                                             :auth0
                                             :profile
                                             :name)]
                                      (if profile-name
                                        profile-name
                                        (-> db
                                            :user-form
                                            :name)))
                                    :auth0Id
                                    (-> db
                                        :auth0
                                        :profile :sub)
                                    :email
                                    (-> db
                                        :auth0
                                        :profile :email)
                                    :dietaryRestrictions
                                    (-> db
                                        :user-form
                                        :dietary-restrictions)}
                 :response-format  (ajax/json-response-format
                                    {:keywords? true})
                 :on-success       [:create-user-success]
                 :on-failure       [:bad-create-user-response]}
    :db (assoc-in db [:user-form :polling?] true)}))

(reg-event-fx
 :search-hosts
 (fn
   [{db :db} _]
   {:http-xhrio {:method           :get
                 :headers          (auth-header (-> db
                                                    :auth0
                                                    :access-token))
                 :uri              (str api-url "/hosts/")
                 :keywords?        true
                 :params           {:address
                                    (->
                                     db
                                     :host-form
                                     :address
                                        ; remove last word
                                        ; bc it's probably
                                        ; "st" or something
                                        ; that will interfere
                                     ; with the psql search
                                     (clojure.string/trim)
                                     (clojure.string/replace
                                      #"\b(\w+)$" "")
                                     (clojure.string/trim))}
                 :response-format  (ajax/json-response-format
                                    {:keywords? true})
                 :on-success       [:search-hosts-success]
                 :on-failure       [:bad-search-hosts-response]}
    :db (assoc-in db [:host-form :polling?] true)}))


(reg-event-fx
 :create-host
 (fn
   [{db :db} _]
   {:http-xhrio {:method           :put
                 :headers          (auth-header (-> db
                                                    :auth0
                                                    :access-token))
                 :uri              (str api-url "/hosts/")
                 :keywords?        true
                 :format           (ajax/json-request-format)
                 :params           {:address
                                    (-> db
                                        :host-form
                                        :address)
                                    :city
                                    (-> db
                                        :host-form
                                        :city)
                                    :state
                                    (-> db
                                        :host-form
                                        :state)
                                    :zipcode
                                    (-> db
                                        :host-form
                                        :zipcode)
                                    :maxOccupancy
                                    (-> db
                                        :host-form
                                        :max-occupancy)
                                    :users [{:userId
                                             (-> db
                                                 :the-user
                                                 :user
                                                 :user-id)}]}
                 :response-format  (ajax/json-response-format
                                    {:keywords? true})
                 :on-success       [:create-host-success]
                 :on-failure       [:bad-create-host-response]}
    :db (assoc-in db [:host-form :polling?] true)}))


(reg-event-fx
 :add-user-to-host
 (fn
   [{db :db} [_ host-id]]
   {:http-xhrio {:method           :post
                 :headers         (auth-header (-> db
                                                   :auth0
                                                   :access-token))
                 :uri              (str
                                    api-url
                                    "/hosts/user/?hostId="
                                    host-id)
                 :keywords?        true
                 :format           (ajax/json-request-format)
                 :params           {:userId
                                    (-> db
                                        :the-user
                                        :user
                                        :user-id)}
                 :response-format  (ajax/json-response-format
                                    {:keywords? true})
                 :on-success       [:add-user-to-host-success]
                 :on-failure       [:bad-add-user-to-host-response]}
    :db (assoc-in db [:host-form :polling?] true)}))

(reg-event-fx
 :rsvp
 (fn
   [{db :db} [_ event-id]]
   {:http-xhrio {:method           :post
                 :headers          (auth-header (-> db
                                                    :auth0
                                                    :access-token))
                 :uri              (str
                                    api-url
                                    "/events/add-participant/"
                                    "?userId=" (-> db
                                                  :the-user
                                                  :user
                                                  :user-id)
                                    "&eventId=" event-id)
                 :keywords?         true
                 :format            (ajax/url-request-format)
                 :response-format   (ajax/json-response-format
                                     {:keywords? true})
                 :on-success        [:rsvp-success]
                 :on-failure        [:bad-rsvp-response]}
    :db (assoc-in db [:upcoming-events :rsvping?] true)}))

(reg-event-fx
 :add-user-to-host
 (fn
   [{db :db} [_ host-id]]
   {:http-xhrio {:method           :post
                 :headers         (auth-header (-> db
                                                   :auth0
                                                   :access-token))
                 :uri              (str
                                    api-url
                                    "/hosts/user/?hostId="
                                    host-id)
                 :keywords?        true
                 :format           (ajax/json-request-format)
                 :params           {:userId
                                    (-> db
                                        :the-user
                                        :user
                                        :user-id)}
                 :response-format  (ajax/json-response-format
                                    {:keywords? true})
                 :on-success       [:add-user-to-host-success]
                 :on-failure       [:bad-add-user-to-host-response]}
    :db (assoc-in db [:host-form :polling?] true)}))

(reg-event-fx
 :create-event
 (fn
   [{db :db} [_ event-id]]
   {:http-xhrio {:method           :put
                 :headers          (auth-header
                                    (-> db
                                        :auth0
                                        :access-token))
                 :uri              (str
                                    api-url
                                    "/events/")
                 :keywords?         true
                 :format            (ajax/json-request-format)
                 :params            {:title
                                     (-> db
                                         :event-form
                                         :title)
                                     :description
                                     (-> db
                                         :event-form
                                         :description)
                                     :happeningAt
                                     (construct-datetime
                                      (-> db
                                          :event-form
                                          :happening-at-date)
                                      (-> db
                                          :event-form
                                          :happening-at-time))
                                     :host {:hostId (-> db
                                                        :the-user
                                                        :user
                                                        :host-id)}}
                 :response-format   (ajax/json-response-format
                                     {:keywords? true})
                 :on-success        [:create-event-success]
                 :on-failure        [:bad-create-event-response]}
    :db (assoc-in db [:event-form :polling?] true)}))


(reg-event-fx
 :get-auth0-tokens
 fwf-interceptors
 (fn
   [{db :db} [code]]
   {:http-xhrio {:method           :post
                 :uri              (str
                                    auth0-domain
                                    "/oauth/token")
                 :keywords?         true
                 :format            (ajax/json-request-format)
                 :params            {:grant_type
                                     "authorization_code"
                                     :client_id
                                     auth0-client-id
                                     :code
                                     code
                                     :redirect_uri
                                     auth0-redirect-uri}
                 :response-format   (ajax/json-response-format
                                     {:keywords? true})
                 :on-success        [:get-auth0-tokens-success]
                 :on-failure        [:bad-get-auth0-tokens-response]}
    :db (assoc-in db [:auth0 :polling?] true)}))

(reg-event-fx
 :send-invites
 fwf-interceptors
 (fn
   [{db :db} _]
   {:http-xhrio {:method           :post
                 :headers          (auth-header
                                    (-> db
                                        :auth0
                                        :access-token))
                 :uri              (str api-url
                                        "/admin/invites/"
                                        "?numHosts="
                                        (-> db
                                            :send-invites
                                            :num-hosts))
                 :format           (ajax/url-request-format)
                 :response-format  (ajax/detect-response-format)
                 :on-success       [:send-invites-success]
                 :on-failure       [:bad-send-invites-response]}
    :db (-> db
            (assoc-in [:send-invites :polling?] true)
            (assoc-in [:send-invites :succeeded?] true))}))
