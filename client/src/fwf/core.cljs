(ns fwf.core
  (:import goog.History)

  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [dispatch-sync]]
            [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events]
            [goog.history.EventType]
            [fwf.specs]
            [fwf.db]
            [fwf.events]
            [fwf.event-list.events]
            [fwf.event-form.events]
            [fwf.admin.events]
            [fwf.user-form.events]
            [fwf.subs]
            [fwf.event-list.subs]
            [fwf.event-form.subs]
            [fwf.admin.subs]
            [fwf.user-form.subs]
            [fwf.views]
            [fwf.utils :refer [>evt
                               <sub
                               href->param-val
                               valid-auth0-redirect?]]))

(enable-console-print!)

;; --- History and Routing ---------------------
(defroute "/" []
  (>evt [:set-page :events]))

(defroute "/events" []
  (>evt [:set-page :events]))

(defroute "/create-user/host" [user-id]
  (>evt [:set-page :add-host-to-user
         :user/set-user-id user-id]))

(defroute "/send-invites" []
  (>evt [:set-page :send-invites]))

(defroute "/create-user" []
  (>evt [:set-page :create-user]))

(defroute "/edit-event" []
  (>evt [:set-page :edit-event]))

(defroute "/create-event" []
  (>evt [:set-page :create-event]))

(defroute "*login-callback" []
  (let [href js/window.location.href
        valid? (valid-auth0-redirect? href)
        code (href->param-val href "code")
        error (href->param-val href "error_description")]
    (if (and valid? code)
      (>evt [:get-auth0-tokens code])
      (>evt [:set-login-error
             (or error
                 "Sorry, something went wrong logging you in.")]))))

(defonce history
  (doto (History.)
    (goog.events/listen
     goog.history.EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -- Main -------------------------------------

(defn ^:export main
  []
  (dispatch-sync [:initialize-db])
  (reagent/render [fwf.views/app]
                  (.getElementById js/document "app")))
