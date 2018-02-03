(ns fwf.views
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [fwf.constants :refer [auth0-authorize-url]]
            [fwf.utils :refer [<sub]]
            [fwf.icons :refer [spinning-plate
                               appetizer-big]]
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

(defn redirect [url]
  (fn [url]
    (js/window.location.replace url)))

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
       [:div.limbo-page
        spinning-plate]
       ;; needs auth?
       (or (not access-token)
           (not sub)
           (= page :login))
       (if auth0-error
         [:div
          [:div.limbo-page
           appetizer-big
           auth0-error]
          [redirect auth0-authorize-url]])
       :else
         (or (page->html page)
             [:div.limbo-page
              appetizer-big
              "Not sure what you're looking for, so here's a lil' shrimpy."]))]))
