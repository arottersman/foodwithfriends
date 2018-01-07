(ns fwf.user-form.subs
  (:require [re-frame.core :refer [reg-sub
                                   subscribe]]
            [reagent.core :as reagent]
            [reagent.ratom]
            [fwf.db :as db]))

(reg-sub
 :user-form/user-input-name
 (fn [db _]
   (-> db ::db/user-form ::db/name)))

(reg-sub
 :user-form/dietary-restrictions
 (fn [db _]
   (-> db ::db/user-form ::db/dietary-restrictions)))

(reg-sub
 :user-form/error-response
 (fn [db _]
   (-> db ::db/user-form ::db/error-response)))

(reg-sub
 :user-form/polling?
 (fn [db _]
   (-> db ::db/user-form ::db/polling?)))

(reg-sub
 :host-form/address
 (fn [db _]
   (-> db ::db/host-form ::db/host-form-fields ::db/address)))

(reg-sub
 :host-form/city
 (fn [db _]
   (-> db ::db/host-form ::db/host-form-fields ::db/city)))

(reg-sub
 :host-form/state
 (fn [db _]
   (-> db ::db/host-form ::db/host-form-fields ::db/state)))

(reg-sub
 :host-form/zipcode
 (fn [db _]
   (-> db ::db/host-form ::db/host-form-fields ::db/zipcode)))

(reg-sub
 :host-form/max-occupancy
 (fn [db _]
   (-> db ::db/host-form ::db/host-form-fields ::db/max-occupancy)))

(reg-sub
 :host-form/searched-hosts
 (fn [db _]
   (-> db ::db/host-form ::db/searched-hosts)))

(reg-sub
 :host-form/polling?
 (fn [db _]
   (-> db ::db/host-form ::db/host-form-api ::db/polling?)))

(reg-sub
 :host-form/error-response
 (fn [db _]
   (-> db ::db/host-form ::db/host-form-api ::db/error-response)))

;; -- Subscription handlers
(reg-sub
 :user-form/dietary-restrictions-with-blank
 (fn [_ _]
   [(subscribe [:user-form/dietary-restrictions])])
 (fn [[user-form/dietary-restrictions] _]
   (conj dietary-restrictions "")))

(reg-sub
 :user-form/name
 (fn [_ _]
   [(subscribe [:auth0/profile])
    (subscribe [:user-form/user-input-name])])
 (fn [[auth0/profile user-form/user-input-name] _]
   (or (::db/name profile)
       user-input-name)))

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
