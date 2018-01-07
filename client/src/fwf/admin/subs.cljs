(ns fwf.admin.subs
  (:require [re-frame.core :refer [reg-sub
                                   subscribe]]
            [reagent.core :as reagent]
            [reagent.ratom]
            [fwf.db :as db]))

(reg-sub
 :send-invites/num-hosts
 (fn [db _]
   (-> db ::db/send-invites ::db/num-hosts)))

(reg-sub
 :send-invites/succeeded?
 (fn [db _]
   (-> db ::db/send-invites ::db/succeeded?)))

(reg-sub
 :send-invites/error
 (fn [db _]
   (-> db ::db/send-invites ::db/error-response :status-message)))

(reg-sub
 :send-invites/polling?
 (fn [db _]
   (-> db ::db/send-invites ::db/polling?)))
