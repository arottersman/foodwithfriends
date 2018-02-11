(ns fwf.event-list.views
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [secretary.core :as secretary]
            [fwf.utils :refer [>evt <sub]]
            [fwf.db :as db]
            [fwf.icons :refer [close-x
                               down-chevron
                               spinning-plate
                               map-icon
                               appetizer-big
                               side-big
                               main-big]]
            cljsjs.clipboard
            [secretary.core :as secretary]))

(def dish->icon
  {"main" [:img.icon {:src "img/007-turkey.svg"}]
   "side" [:img.icon {:src "img/010-salad.svg"}]
   "drinks" [:img.icon {:src "img/006-glass.svg"}]
   "appetizer" [:img.icon {:src "img/008-food-1.svg"}]})

(defn email-chain-button [target]
  ;; Let this live self-contained,
  ;; detached from reframe.
  ;; from https://github.com/cljsjs/packages/tree/master/clipboard
  (let [clipboard-atom (atom nil)]
    (reagent/create-class
     {:display-name "clipboard-button"
      :component-did-mount
      #(let [clipboard (new js/Clipboard (reagent/dom-node %))]
         (reset! clipboard-atom clipboard))
      :component-will-unmount
      #(when-not (nil? @clipboard-atom)
         (.destroy @clipboard-atom)
         (reset! clipboard-atom nil))
      :reagent-render
      (fn []
         [:button.email-chain
          {:data-clipboard-target target}
          "Copy emails to clipboard"])})))

(defn rsvp-button [event]
  (fn [event]
    (let [rsvped? (::db/rsvped? event)
          rsvping? (<sub [:upcoming-events/rsvping?])
          participant-str (::db/participant-str event)
          event-id (::db/event-id event)
          button-txt (if rsvped?
                       "You're going!"
                       "RSVP")]
      [:button.rsvp
       {:disabled (or rsvped? rsvping?)
        :on-click #(>evt [:rsvp event-id])}
       button-txt " " participant-str])))

(defn menu []
  (fn []
    (let [showing-events (<sub [:showing-events])]
      [:nav.event-list-menu
       [:button.menu-item
        {:class (if (= showing-events :user-events)
                  "-active")
         :on-click #(>evt [:set-showing-events
                           :user-events])}
        "past"]
       [:button.menu-item
        {:class (if (= showing-events :upcoming-events)
                  "-active")
         :on-click #(>evt [:set-showing-events
                           :upcoming-events])}
        "all events"]
       [:button.menu-item
        {:on-click #(>evt [:signout])}
        "log out"]])))

(defn assigned-dish-modal []
  (fn []
    (let [assigned-dish (<sub [:user-assigned-dish])]
      [:div.modal-background
       [:section.modal.assigned-dish
        [:button.close {:type "button"
                  :on-click #(>evt [:clear-assigned-dish-modal])}
         close-x]
        [:h3 "Great!"]
        (dish->icon assigned-dish)
        [:p.user-assigned-dish "You're assigned to bring: "
         [:strong assigned-dish]]]])))

(defn user-detail-modal []
  (fn []
    (let [{:keys [fwf.db/name
                  fwf.db/email
                  fwf.db/dietary-restrictions
                  fwf.db/assigned-dish]} (<sub [:user-detail])]
      [:div.modal-background
       [:section.modal.user-detail
        [:button.close {:type "button"
                  :on-click #(>evt
                              [:set-user-detail
                               nil])}
         close-x]

        (dish->icon assigned-dish)
        [:h3.name name]
        [:p.email email]
        (if (not-empty dietary-restrictions)
          [:p.user-dietary-rest
           "Dietary restrictions: "(clojure.string/join
                                    ", " dietary-restrictions)])
        (if assigned-dish
          [:div.user-info
           [:p.user-assigned-dish "Assigned to bring: "
            [:strong assigned-dish]]])]])))


(defn user-link [user]
  (fn [user]
    [:li.person
     [:button.user
      {:on-click #(>evt [:set-user-detail user])}
      (dish->icon (::db/assigned-dish user))
      [:p.name (::db/name user)]]]))

(defn host-user-list [users]
  (fn [users]
    [:ul.people
     (for [user users]
       ^{:key user} [user-link user])]))

(defn participant-list [participants]
  (fn [participants]
    (if (empty? participants)
      [:p.no-people "No one has RSVPed yet. Be the first!"]
      [:ul.people
       (for [user participants]
         ^{:key (str "participant"
                     (::db/user-id user))
           } [user-link user])])))

(defn event-detail [event dismiss editable?]
  (fn [event dismiss editable?]
    (let [{:keys
           [fwf.db/participants
            fwf.db/agg-dietary-restrictions
            fwf.db/dietary-restrictions
            fwf.db/title
            fwf.db/happening-at-date
            fwf.db/happening-at-time
            fwf.db/hosted-by-str
            fwf.db/address
            fwf.db/city
            fwf.db/state
            fwf.db/zipcode
            fwf.db/google-maps-url
            fwf.db/email-chain
            fwf.db/rsvped?
            fwf.db/event-id]} event]
      [:div.event-detail
       [:h1.event-date happening-at-date]
       (if editable?
         [:button.edit {:on-click (fn []
                                    (>evt [:event-form/from-event event])
                                    (js/location.assign "/#/edit-event"))}
          "Edit"])
       [:button.close {:on-click dismiss} close-x]
       [:div.event-time happening-at-time]
       [:div.event-hosts "Hosted by " hosted-by-str]
       [:p.event-description title]
       [:a.event-location {:href google-maps-url
                           :rel "noopener noreferrer"
                           :target "_blank"}
        map-icon
        [:div.event-address
         [:p.address-line address]
         [:p.address-line city ", " state]
         [:p.address-line zipcode]]]
       [:div.email-chain-container
        (if rsvped?
          [:div
           [:div.too-tiny-to-see {:id "email-chain"} email-chain]
           [email-chain-button "#email-chain"]]
          [rsvp-button event])]
       (if (not-empty agg-dietary-restrictions)
         [:p.event-dietary-restrictions
          "Dietary restrictions: "
          (clojure.string/join
           ", " agg-dietary-restrictions)])
        (if rsvped?
          [participant-list participants event])])))

(defn event-snippet [event on-click]
  (fn [{:keys
        [fwf.db/happening-at-date
         fwf.db/happening-at-time
         fwf.db/hosted-by-str]}
       on-click]
    [:button.event-snippet {:on-click on-click}
     [:h1.event-date happening-at-date]
     down-chevron
     [:div.event-time happening-at-time]
     [:div.event-hosts "Hosted by " hosted-by-str]]))

(defn upcoming-event [event detail?]
  (fn [event detail?]
    (let [{:keys [fwf.db/event-id
                  fwf.db/your-house?
                  fwf.db/rsvped?]} event]
      [:li.event
       {:class (if (or your-house? rsvped?)
                 "-your-event")}
       (cond
         detail?
         [event-detail
          event
          #(>evt [:set-upcoming-event-detail nil])
          your-house?]
         :else
         [:div.event-snippet-container
           [event-snippet
            event
            #(>evt
              [:set-upcoming-event-detail
               event-id])]
           [rsvp-button event]])])))

(defn upcoming-events []
  (fn []
    (let [polling? (<sub [:upcoming-events/polling?])
          events (<sub [:pretty-upcoming-events])
          detail-event-id (<sub [:upcoming-events/detail-id])
          error (<sub [:upcoming-events/error])]
      (cond
        polling?
        [:div.center-container
         spinning-plate]
        error
        [:div.center-container
         main-big
         [:p.error "Uh-oh, something went wrong while we were trying to get the upcoming events!"]]
        (empty? events)
        [:div.center-container
         appetizer-big
         [:p.no-events
          "Nobody has made an event yet. Check back soon, or bother your cohort to get on it!"]]
        :else
        [:ul.events
         (map (fn [event]
                ^{:key (str "upcoming-event"
                            (::db/event-id event))}
                [upcoming-event event
                 (= (::db/event-id event)
                    detail-event-id)])
              events)]))))

(defn user-event [event detail?]
  (fn [event detail?]
    [:li.event.-your-event
     (cond
       detail?
       [event-detail
        event
        #(>evt [:set-user-event-detail nil])]
       :else
       [event-snippet
        event
        #(>evt
          [:set-user-event-detail
           (::db/event-id event)])])]))

(defn user-events []
  (fn []
    (let [polling? (<sub [:user-events/polling?])
          user-polling? (<sub [:user/polling?])
          events (<sub [:pretty-user-events]) detail-event-id (<sub [:user-events/detail-id])
          error (<sub [:user-events/error])
          user-error (<sub [:user/error])]
      (cond
        (or polling? user-polling?)
        [:div.center-container
         spinning-plate]
        (or error user-error)
        [:div.center-container
         main-big
         [:p.error "...something went wrong getting your past events. Guess you should just live in the present."]]
        (empty? events)
        [:div.center-container
         side-big
         [:p.no-events "You don't have any past events. Anything you've RSVPed to, or hosted will show up here."]]
        :else
        [:ul.events
         (map (fn [event]
                ^{:key (str "user-event"
                            (::db/event-id event))}
                [user-event event (= (::db/event-id event)
                                     detail-event-id)])
              events)]))))

(defn events-page []
  (fn []
    (let [showing-events (<sub [:showing-events])
          user-detail (<sub [:user-detail])
          user-assigned-dish (<sub [:user-assigned-dish])]
      [:div.page
       (cond ;; modals
         user-detail [user-detail-modal]
         user-assigned-dish [assigned-dish-modal])
       [:section.events-section
        [menu]
        [:div.event-list-container
         (case showing-events
           :upcoming-events [upcoming-events]
           :user-events [user-events])]]
       ])))
