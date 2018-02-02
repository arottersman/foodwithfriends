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
                          profile-ls-key
                          auth0-client-id
                          auth0-domain
                          auth0-audience
                          auth0-redirect-uri
                          auth0-state]]
   [fwf.utils :refer [navigate-to!]]
   [fwf.api-helpers :refer [auth-header
                            parse-user]]
   [fwf.specs]
   [fwf.db :as db :refer [default-db
                          auth0->local-store
                          wipe-local-store
                          wipe-local-store-if-401
                          id-token->auth0-profile]]))

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


; -- Event Handlers ---------------
(reg-event-fx
 :initialize-db
 [(inject-cofx :local-store-auth0)
  check-spec-interceptor]
 (fn [{:keys [db local-store-auth0]} _]
   (let [auth0-default (::db/auth0 default-db)
         auth0 (merge auth0-default local-store-auth0)]
     {:db (merge db (assoc default-db
                           ::db/auth0 auth0))})))

(reg-event-db
 :set-page
 fwf-interceptors
 (fn [db [new-page-kw]]
   (assoc db ::db/page new-page-kw)))

;; Auth
(reg-event-db
 :signout
 [fwf-interceptors
  ->wipe-local-store]
 (fn [db _]
   (assoc default-db ::db/page ::db/events)))

(reg-event-db
 :set-login-error
 fwf-interceptors
 (fn [db [error-message]]
   (assoc-in db [::db/auth0 ::db/error-response]
             {:status-text error-message})))

;; -- API Events and FX -----------

;; User api
(reg-event-db
 :set-user
 fwf-interceptors
 (fn [db [user-response]]
   (let [user (parse-user user-response)
         host-id (::db/host-id user)]
     (-> db
         (assoc-in [::db/the-user ::db/user] user)
         (assoc-in [::db/the-user ::db/stale?] false)
         (assoc-in [::db/the-user ::db/polling?] false)
         (cond-> (or (nil? host-id)
                     (= 0 host-id))
           (assoc ::db/page :add-host-to-user))))))

(reg-event-db
 :set-user-error
 [fwf-interceptors
  ->clear-auth-if-needed]
 (fn [db [error-response]]
   (let [status (:status error-response)]
     (-> db
         (assoc-in [::db/the-user ::db/stale?] false)
         (assoc-in [::db/the-user ::db/polling?] false)
         (cond-> (= status 404)
           (->
            (assoc ::db/page :create-user)
            (assoc-in [::db/the-user ::db/user]
                      :no-account)))
         (cond-> (not= status 404)
           (assoc-in [::db/the-user ::db/error]
                     error-response))
         (cond-> (= status 401)
           (-> (assoc ::db/page ::db/login)
               (assoc-in [:auth0 :access-token] "")
               (assoc-in [:auth0 :profile] {})))))))

(reg-event-db
 :set-user-polling
 fwf-interceptors
 (fn [db _]
   (assoc-in db [::db/the-user ::db/polling?] true)))

;; auth0 api
(reg-event-db
 :get-auth0-tokens-success
 [fwf-interceptors
  ->auth0-local-store
  ->events-uri]

 (fn [db [{:keys [access_token id_token]}]]
   (-> db
       (assoc-in [::db/auth0 ::db/access-token]
                 access_token)
       (assoc-in [::db/auth0 ::db/profile]
                 (id-token->auth0-profile id_token))
       (assoc-in [::db/auth0 ::db/polling?]
                 false)
       (assoc ::db/page :events))))

(reg-event-db
 :bad-get-auth0-tokens-response
 fwf-interceptors
 (fn [db [response]]
   (-> db
       (assoc-in [::db/auth0 ::db/error-response]
                 response)
       (assoc-in [::db/auth0 ::db/polling?]
                 false))))

;; api event-fx
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
    :db (assoc-in db [::db/auth0 ::db/polling?] true)}))
