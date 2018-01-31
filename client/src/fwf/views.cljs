(ns fwf.views
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [fwf.constants :refer [auth0-authorize-url]]
            [fwf.utils :refer [<sub]]
            [fwf.event-list.views :refer [events-page]]
            [fwf.event-form.views :refer [event-form]]
            [fwf.user-form.views
             :refer [user-form
                     add-host-to-user-page]]
            [fwf.admin.views :refer [send-invites-page]]))

(defn header [text]
  (fn [text]
    [:div.header
     [:div.stripes
      [:div.stripe]
      [:div.stripe.-middle]
      [:div.stripe]]
     [:div.hexagon
      [:span text]]]))

(def page->html
  {:create-user [user-form]
   :add-host-to-user [add-host-to-user-page]
   :events [events-page]
   :create-event [event-form]
   :send-invites [send-invites-page]})

(defn app []
  (let [page (<sub [:page])
        user-detail (<sub [:user-detail])
        access-token (<sub [:auth0/access-token])
        auth0-polling? (<sub [:auth0/polling?])
        auth0-error (<sub [:auth0/error])
        {:keys [fwf.db/sub]} (<sub [:auth0/profile])
        user (<sub [:user]) user-assigned-dish (<sub [:user-assigned-dish])]
    [:main.app
     [header "FORKFUL"]
     (cond
       auth0-polling?
       [:p.polling "Getting your profile..."]
       ;; needs auth?
       (or (not access-token)
           (not sub)
           (= page :login))
       [:section.auth
        (if auth0-error
          [:p.error auth0-error])
        [:a.login {:href auth0-authorize-url}
         "New phone, who this?"]]
       :else
         (or (page->html page)
             [:div "Page not found"]))]))
