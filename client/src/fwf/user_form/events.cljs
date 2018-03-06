(ns fwf.user-form.events
  (:require
   [ajax.core :as ajax]
   [re-frame.core :refer [reg-event-db
                          reg-event-fx
                          path]]
   [fwf.events :refer [fwf-interceptors]]
   [fwf.db :as db]
   [fwf.constants :refer [api-url]]
   [fwf.api-helpers :refer [auth-header
                            parse-user
                            parse-host]]))

;; User form

(reg-event-db
 :user-form/update-name
 fwf-interceptors
 (fn [db [new-value]]
   (assoc-in db [::db/user-form ::db/name ] new-value)))

(reg-event-db
 :user-form/update-dietary-restrictions
 [fwf-interceptors
  (path ::db/user-form ::db/dietary-restrictions)]
 (fn [restrictions [idx new-value]]
   (filterv not-empty (assoc restrictions idx new-value))))

;; Host form
(reg-event-db
 :user/set-user-id
 fwf-interceptors
 (fn [db [user-id]]
   (assoc-in db [::db/the-user ::db/user ::db/user-id]
             user-id)))

(reg-event-db
 :host-form/update-address
 fwf-interceptors
 (fn [db [new-address]]
   (assoc-in db [::db/host-form
                 ::db/host-form-fields
                 ::db/address]
             new-address)))

(reg-event-db
 :host-form/update-city
 fwf-interceptors
 (fn [db [new-city]]
   (assoc-in db [::db/host-form
                 ::db/host-form-fields
                 ::db/city]
             new-city)))

(reg-event-db
 :host-form/update-state
 fwf-interceptors
 (fn [db [new-state]]
   (assoc-in db [::db/host-form
                 ::db/host-form-fields
                 ::db/state]
             new-state)))

(reg-event-db
 :host-form/update-zipcode
 fwf-interceptors
 (fn [db [new-zipcode]]
   (assoc-in db [::db/host-form
                 ::db/host-form-fields
                 ::db/zipcode]
             new-zipcode)))

(reg-event-db
 :host-form/update-max-occupancy
 fwf-interceptors
 (fn [db [new-max-occupancy]]
   (assoc-in db [::db/host-form ::db/host-form-fields ::db/max-occupancy]
             (max new-max-occupancy 0))))

;; Create user api
(reg-event-db
 :bad-create-user-response
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [::db/user-form ::db/error-response]
                 response)
       (assoc-in [::db/user-form ::db/polling?]
                 false))))

(reg-event-db
 :create-user-success
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [::db/the-user ::db/user]
                 (parse-user response))
       (assoc-in [::db/user-form ::db/polling?]
                 false)
       (assoc ::db/page :add-host-to-user))))

;; host api
(reg-event-db
 :search-hosts-success
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [::db/host-form ::db/searched-hosts]
                 (map parse-host response))
       (assoc-in [::db/host-form
                  ::db/host-form-api
                  ::db/polling?]
                 false))))

(reg-event-db
 :bad-search-hosts-response
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [::db/host-form
                  ::db/host-form-fields
                  ::db/error-response]
                 response)
       (assoc-in [::db/host-form
                  ::db/host-form-api
                  ::db/polling?]
                 false))))

(reg-event-db
 :create-host-success
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [::db/the-user ::db/stale?] true)
       (assoc-in [::db/host-form
                  ::db/host-form-api
                  ::db/polling?]
                 false)
       (assoc ::db/page :events))))

(reg-event-db
 :bad-create-host-response
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [::db/host-form
                  ::db/host-form-api
                  ::db/error-response]
                 response)
       (assoc-in [::db/host-form
                  ::db/host-form-api
                  ::db/polling?]
                 false))))

;; event fx
(defn- access-token [db] (-> db ::db/auth0 ::db/access-token))
(defn- headers [db] (auth-header (access-token db)))
(defn- user-id [db] (-> db ::db/the-user ::db/user ::db/user-id))
(defn- host-form-polling [db] (assoc-in db [::db/host-form
                                            ::db/host-form-api
                                            ::db/polling?] true))
(reg-event-fx
 :create-user
 (fn
   [{db :db} _]
   (let [profile (-> db ::db/auth0 ::db/profile)
         profile-name (:name profile)
         user-form (::db/user-form db)]
     {:http-xhrio {:method         :put
                   :headers        (headers db)
                   :uri            (str api-url "/users/")
                   :format         (ajax/json-request-format)
                   :keywords?      true
                   :params         {:name
                                    (or profile-name
                                        (::db/name user-form))
                                    :auth0Id
                                    (::db/sub profile)
                                   :email
                                    (::db/email profile)
                                    :dietaryRestrictions
                                    (::db/dietary-restrictions
                                     user-form)}
                   :response-format (ajax/json-response-format
                                     {:keywords? true})
                   :on-success      [:create-user-success]
                   :on-failure      [:bad-create-user-response]}
      :db (assoc-in db [::db/user-form ::db/polling?] true)})))

(reg-event-fx
 :search-hosts
 (fn
   [{db :db} _]
   (let [host-form (-> db ::db/host-form ::db/host-form-fields)]
     {:http-xhrio {:method           :get
                   :headers          (headers db)
                   :uri              (str api-url "/hosts/")
                   :keywords?        true
                   :params           {:address
                                      (::db/address host-form)}
                   :response-format  (ajax/json-response-format
                                      {:keywords? true})
                   :on-success       [:search-hosts-success]
                   :on-failure       [:bad-search-hosts-response]}
      :db (host-form-polling db)})))

(reg-event-fx
 :create-host
 (fn
   [{db :db} _]
   (let [host-form (-> db ::db/host-form ::db/host-form-fields)]
     {:http-xhrio {:method           :put
                   :headers          (headers db)
                   :uri              (str api-url "/hosts/")
                   :keywords?        true
                   :format           (ajax/json-request-format)
                   :params           {:address
                                      (::db/address host-form)
                                      :city
                                      (::db/city host-form)
                                      :state
                                      (::db/state host-form)
                                      :zipcode
                                      (::db/zipcode host-form)
                                      :maxOccupancy
                                      (::db/max-occupancy
                                       host-form)
                                      :users [{:userId user-id}]}
                   :response-format  (ajax/json-response-format
                                      {:keywords? true})
                   :on-success       [:create-host-success]
                   :on-failure       [:bad-create-host-response]}
      :db (host-form-polling db)})))

(reg-event-fx
 :add-user-to-host
 (fn
   [{db :db} [_ host-id]]
   {:http-xhrio {:method           :post
                 :headers          (headers db)
                 :uri              (str
                                    api-url
                                    "/hosts/user/?hostId="
                                    host-id)
                 :keywords?        true
                 :format           (ajax/json-request-format)
                 :params           {:userId (user-id db)}
                 :response-format  (ajax/json-response-format
                                    {:keywords? true})
                 :on-success       [:create-host-success]
                 :on-failure       [:bad-create-host-response]}
    :db (host-form-polling db)}))

(reg-event-fx
 :add-user-to-host
 (fn
   [{db :db} [_ host-id]]
   {:http-xhrio {:method           :post
                 :headers          (headers db)
                 :uri              (str
                                    api-url
                                    "/hosts/user/?hostId="
                                    host-id)
                 :keywords?        true
                 :format           (ajax/json-request-format)
                 :params           {:userId (user-id db)}
                 :response-format  (ajax/json-response-format
                                    {:keywords? true})
                 :on-success       [:create-host-success]
                 :on-failure       [:bad-create-host-response]}
    :db (host-form-polling db)}))
