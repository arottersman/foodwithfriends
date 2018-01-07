(ns fwf.subs
  (:require [re-frame.core :refer [reg-sub
                                   subscribe
                                   dispatch
                                   reg-sub-raw]]
            [reagent.core :as reagent]
            [reagent.ratom]
            [fwf.db :as db]
            [fwf.api-helpers :as api-helpers]
            [fwf.utils :refer [>evt <sub]]))

;; -- Extractors --------------------

(reg-sub
 :page
 (fn [db _]
   (::db/page db)))

(reg-sub
 :auth0/error
 (fn [db _]
   (-> db ::db/auth0 ::db/error-response :status-message)))

(reg-sub
 :auth0/profile
 (fn [db _]
   (-> db ::db/auth0 ::db/profile)))

(reg-sub
 :auth0/access-token
 (fn [db _]
   (-> db ::db/auth0 ::db/access-token)))

(reg-sub
 :auth0/polling?
 (fn [db _]
   (-> db ::db/auth0 ::db/polling?)))

(reg-sub
 :showing-events
 (fn [db _]
   (-> db ::db/showing-events)))

(reg-sub
 :user/error
 (fn [db _]
   (-> db ::db/the-user ::db/error-response :status-message)))

(reg-sub
 :user/stale?
 (fn [db _]
   (-> db ::db/the-user ::db/stale?)))

;; -- Remote Dependent Subscriptions --
(reg-sub-raw
 :user
 (fn [app-db _]
  (reagent.ratom/make-reaction
   (fn []
     (let [stale? (<sub [:user/stale?])
           access-token (<sub [:auth0/access-token])
           {:keys [fwf.db/sub]} (<sub [:auth0/profile])]
       (if (and stale? sub access-token)
         (api-helpers/fetch-user!
          {:auth0-id sub
           :access-token access-token
           :on-success #(>evt [:set-user %])
           :on-failure #(>evt [:set-user-error %])})))
      (get-in @app-db [::db/the-user ::db/user])))))
