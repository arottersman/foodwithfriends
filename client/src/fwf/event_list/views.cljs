(ns fwf.event-list.views
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [fwf.utils :refer [>evt <sub]]
            [fwf.db :as db]))

(def arrow-down [:div.expand
                 {:dangerouslySetInnerHTML {:__html "&#x25BE;"}}])

(def close-x [:div
              {:dangerouslySetInnerHTML {:__html "&#x2613;"}}])

(def email-icon [:span.icon
                 {:dangerouslySetInnerHTML {:__html "&#x2709;"}}])

(def calendar-icon [:span.icon
                    {:dangerouslySetInnerHTML {:__html "&#128467;"}}])

(def map-icon [:span.icon
               {:dangerouslySetInnerHTML {:__html "&#128506;"}}])

(def house-icon [:span.icon
               {:dangerouslySetInnerHTML {:__html "&#127968;"}}])

(def check-icon [:span.icon
                 {:dangerouslySetInnerHTML {:__html "&#9989;"}}])

(def leg-icon [:span.icon
               {:dangerouslySetInnerHTML {:__html "&#127831;"}}])

(def wine-icon [:span.icon
                {:dangerouslySetInnerHTML {:__html "&#127863;"}}])

(def salad-icon [:span.icon
                 {:dangerouslySetInnerHTML {:__html "&#129367;"}}])

(def shrimp-icon [:span.icon
                  {:dangerouslySetInnerHTML {:__html "&#129424;"}}])

(def dish->icon
  {"main" leg-icon
   "side" salad-icon
   "drinks" wine-icon
   "appetizer" shrimp-icon})

(defn account-nav []
  (fn []
    [:nav.site-nav
     [:h2.site-title "Pot*uck"]
     [:button.signout {:on-click #(>evt [:signout])} "Sign out"]
     ]))

(defn assigned-dish-modal []
  (fn []
    (let [assigned-dish (<sub [:user-assigned-dish])]
      [:div.modal-background
       [:section.modal.assigned-dish
        [:button.close {:type "button"
                  :on-click #(>evt [:clear-assigned-dish-modal])}
         close-x]
        [:h3 "Great"]
        (dish->icon assigned-dish)
        [:span "You're assigned to bring a "
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
        [:h3.name name]
        [:a email]
        (if (not-empty dietary-restrictions)
            [:div.user-info
             [:h5 "Dietary Restrictions:"]
             [:p (clojure.string/join
                  ", " dietary-restrictions)]])
        (if assigned-dish
          [:div.user-info
           [:label "Assigned Dish"]
           (dish->icon assigned-dish)
           [:p assigned-dish]])]])))


(defn user-link [user]
  (fn [user]
    [:li.person
     (dish->icon (:assigned-dish user))
     [:a.user {:on-click #(>evt [:set-user-detail user])}
          (::db/name user)]]))

(defn host-user-list [users]
  (fn [users]
    [:ul.people
     (for [user users]
       ^{:key user} [user-link user])]))

(defn participant-list [participants]
  (fn [participants]
    (if (empty? participants)
      [:p.no-people "No one yet..."]
      [:ul.people
       (for [user participants]
         ^{:key (str "participant"
                     (::db/user-id user))
           } [user-link user])])))

(defn event-detail [event dismiss]
  (fn [event]
    (let [{:keys
           [fwf.db/participants
            fwf.db/agg-dietary-restrictions
            fwf.db/dietary-restrictions
            fwf.db/host
            fwf.db/title
            fwf.db/description
            fwf.db/happening-at
            fwf.db/address-str
            fwf.db/google-maps-url
            fwf.db/email-chain
            fwf.db/rsvped?]} event
          host-users (::db/users host)]
      [:div.event-detail
       [:button.close {:on-click dismiss} close-x]
       [:h3 title]
       [:p.description description]
       [:div.time calendar-icon happening-at]
       [:div.location map-icon
        [:a.location {:href google-maps-url}
         address-str]]
       (if (not-empty agg-dietary-restrictions)
         [:p.list-label "What can't people eat?"])
       [:p (clojure.string/join
            ", " dietary-restrictions)]
       [:p.list-label "Who's house is this?"]
       [host-user-list host-users]
       [:p.list-label "Who's going?"]
       [participant-list participants event]
       [:div.bottom-row
        [(if rsvped?
           :a.email-chain.-rsvped
           :a.email-chain)
         {:href (str "mailto:" email-chain)}
         "Email Everyone"]]])))

(defn event-snippet [event on-click]
  (fn [{:keys
        [fwf.db/title
         fwf.db/happening-at
         fwf.db/google-maps-url
         fwf.db/address-str]}]
    [:button.event-snippet {:on-click on-click}
     arrow-down
     [:h3 title]
     [:p.time calendar-icon happening-at]
     map-icon [:a {:href google-maps-url}
               address-str]]))

(defn upcoming-event [event detail?]
  (fn [event detail?]
    (let [rsvping? (<sub [:upcoming-events/rsvping?])
          event-id (::db/event-id event)
          participant-str (::db/participant-str event)]
      [:li.event
       (cond
         detail?
         [event-detail
          event
          #(>evt [:set-upcoming-event-detail nil])]
         :else
         [event-snippet
          event
          #(>evt
            [:set-upcoming-event-detail
             event-id])])
       (if (::db/rsvped? event)
         [:div.event-tag.rsvped check-icon
          participant-str]

         [:button.event-tag.rsvp
          {:on-click #(>evt [:rsvp event-id])
           :disabled rsvping?}
          (if (::db/your-house? event)
            house-icon)
          "RSVP" participant-str])])))

(defn upcoming-events []
  (fn []
    (let [events (<sub [:pretty-upcoming-events])
          detail-event-id (<sub [:upcoming-events/detail-id])
          error (<sub [:upcoming-events/error])]
      (cond
        (nil? events)
        [:p.polling "polling"]
        (empty? events)
        [:p.no-events "No upcoming events"]
        error
        [:p.error "Error getting the upcoming events"]
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
    [:li.event
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
    (let [events (<sub [:pretty-user-events])
          detail-event-id (<sub [:user-events/detail-id])
          error (<sub [:user-events/error])]
      (cond
        (nil? events)
        [:p.polling "polling"]
        (empty? events)
        [:p.no-events "You don't have any events"]
        error
        [:p.error "Error getting your events"]
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
       [account-nav]
       [:section.events-section
        [:div.event-tab-group
         [:input.tab-radio
          {:type "radio"
           :id "upcoming-events"
           :checked (= showing-events :upcoming-events)
           :on-change #(>evt [:set-showing-events
                              :upcoming-events])}]
         [:label.event-tab {:for "upcoming-events"}
          "upcoming events"]
         [:input.tab-radio
          {:type "radio"
           :id "user-events"
           :checked (= showing-events :user-events)
           :on-change #(>evt [:set-showing-events
                              :user-events])}]
         [:label.event-tab {:for "user-events"}
          "my events"]]
        [:div.active-event-tab
         (case showing-events
           :upcoming-events [upcoming-events]
           :user-events [user-events])]]
       ])))
