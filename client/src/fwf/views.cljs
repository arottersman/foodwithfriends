(ns fwf.views
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [cljs-time.core]
            [cljs-time.format]
            [cljs-time.periodic]
            [fwf.constants :refer [auth0-authorize-url]]
            [fwf.utils :refer [>evt <sub]]))

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

(def days-of-the-week ["Sun"
                       "Mon"
                       "Tue"
                       "Wed"
                       "Thu"
                       "Fri"
                       "Sat"])

(def months ["January"
             "February"
             "March"
             "April"
             "May"
             "June"
             "July"
             "August"
             "September"
             "October"
             "November"
             "December"])

(defn days-of-week-col-headers [month]
  (fn [month]
    [:thead
     (for [day days-of-the-week]
       ^{:key (str month "day" day)}[:th day])]))

(defn week-rows [{:keys [month
                         weeks
                         selected
                         on-select]}]
  (fn [{:keys [month
               weeks
               selected
               on-select]}]
    [:tbody
     (map-indexed
      (fn [week-num week]
        ^{:key (str month "week" week-num)}
        [:tr
         (map-indexed
          (fn [day-num date]
            ^{:key (str month week-num "date" day-num)}
            [:td
             (cond
               (= date :not-in-month)
               [:div.blank-date "x"]
               (cljs-time.core/= date selected)
               [:div.selected-date (cljs-time.core/day date)]
               :else
               [:button {:type "button"
                         :on-click #(on-select date)}
                (cljs-time.core/day date)])])
          week)])
       weeks)]))

(defn multi-month-datepicker []
  (fn []
    (let [selected-date (<sub [:event-form/happening-at-date])
          on-select #(>evt [:event-form/update-happening-at-date %])
          grouped-date-options (<sub [:event-form/grouped-date-options])]
      [:div.date-picker
       (map (fn [[month weeks]]
              ^{:key (str "month" month)}
              [:div.calendar
               [:h3.month (months (- month 1))]
               [:table.calendar
                [days-of-week-col-headers month]
                [week-rows {:month month
                            :weeks weeks
                            :selected selected-date
                            :on-select on-select}]]])
            grouped-date-options)])))

(defn timepicker [{:keys [hour minute time-of-day on-change]}]
  (fn [{:keys [hour minute time-of-day on-change]}]
    [:div.timepicker
     [:label.unit "hour"
      [:input {:type "tel"
               :value hour
               :on-change #(on-change
                            (-> % .-target .-value) minute time-of-day)}]]
     [:label.unit "minutes"
      [:input {:type "tel"
               :value minute
               :on-change #(on-change
                            hour (-> % .-target .-value) time-of-day)}]]
     [:label.hidden {:for "am-pm"} "am-pm"]
     [:select.am-pm {:value time-of-day
               :id "am-pm"
               :on-change #(on-change
                            hour minute (-> % .-target .-value))}
      [:option {:value :am} "AM"]
      [:option {:value :pm} "PM"]]]))

(defn event-form []
  (fn []
    (let [title (<sub [:event-form/title])
          description (<sub [:event-form/description])
          {:keys [hour minute time-of-day]}
          (<sub [:event-form/happening-at-time-strs])
          valid? (<sub [:event-form/valid?])
          polling? (<sub [:event-form/polling?])
          error (<sub [:event-form/error-string])]
      [:form.event-form
       [:h2 "create your pot*uck!"]
       [:label "What should we call your pot*uck?"
        [:input {:value title
                 :on-change #(>evt [:event-form/update-title
                                    (-> % .-target .-value)])
                 }]]
       [:label "Add a description, if you want"
        [:textarea {:value description
                    :on-change #(>evt [:event-form/update-description
                                       (-> % .-target .-value)])
                    }]]
       [:h4 "when will it be?"]
       [multi-month-datepicker]
       [:h4 "what time?"]
       [timepicker {:hour hour
                    :minute minute
                    :time-of-day time-of-day
                    :on-change (fn [hour-str minute-str new-time-of-day]
                                 (>evt [:event-form/update-happening-at-time
                                        hour-str
                                        minute-str
                                        new-time-of-day]))}]
       [:p.info
        "after you're done, make sure to rsvp to your"
        " event."]
       (if error
         [:p.error error])
       [:button.done {:type "button"
                      :on-click #(>evt [:create-event])
                      :disabled (or (not valid?)
                                    polling?)}
        "Done!"]
       ])))

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
    (let [{:keys [name
                  email
                  dietary-restrictions
                  assigned-dish
                  ]} (<sub [:user-detail])]
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
             [:p (clojure.string/join ", "
                                      dietary-restrictions)]])
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
          (:name user)]]))

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
                     (:user-id user))
           } [user-link user])])))

(defn event-detail [event dismiss]
  (fn [event]
    (let [participants (:participants event)
          host-users (-> event
                         :host :users)
          dietary-restrictions (flatten
                                (map :dietary-restrictions participants))]
      (println dietary-restrictions)
      [:div.event-detail
       [:button.close {:on-click dismiss} close-x]
       [:h3 (:title event)]
       [:p.description (:description event)]
       [:div.time calendar-icon (:happening-at event)]
       [:div.location map-icon [:a.location {:href (:google-maps-url event)}
                                (:address-str event)]]
       (if (not-empty dietary-restrictions)
         [:p.list-label "What can't people eat?"])
       [:p (clojure.string/join
            ", "
            dietary-restrictions)]
       [:p.list-label "Who's house is this?"]
       [host-user-list host-users]
       [:p.list-label "Who's going?"]
       [participant-list  participants event]])))

(defn event-snippet [event on-click]
  (fn [event]
    [:button.event-snippet {:on-click on-click}
     arrow-down
     [:h3 (:title event)]
     [:p.time calendar-icon (:happening-at event)]
     map-icon [:a {:href (:google-maps-url event)}
               (:address-str event)]]))

(defn upcoming-event [event detail?]
  (fn [event detail?]
    (let [rsvping? (<sub [:upcoming-events/rsvping?])
          max-occupancy (-> event
                            :host
                            :max-occupancy)
          num-participants (-> event
                               :participants
                               count)
          max-occupancy-reached? (>= num-participants
                                     max-occupancy)
          participant-str
          (str " (" num-participants "/" max-occupancy ")")]
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
             (:event-id event)])])
       (if (:rsvped? event)
         [:div.event-tag.rsvped check-icon
          participant-str]

         [:button.event-tag.rsvp
          {:on-click #(>evt [:rsvp (:event-id event)])
           :disabled (or rsvping?
                         max-occupancy-reached?)}
          (if (:your-house? event)
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
                ^{:key (str "upcoming-event" (:event-id event))}
                [upcoming-event event (= (:event-id event)
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
           (:event-id event)])])]))

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
                ^{:key (str "user-event" (:event-id event))}
                [user-event event (= (:event-id event)
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
       ])
    ))

(defn clearable-input [{:keys [id value label on-change]}]
  (fn [{:keys [id value label on-change]}]
    [:div.clearable-input
     [:label {:for id} label]
     [:input {:type "text"
              :id id
              :value value
              :on-change #(-> % .-target .-value on-change)}]
     [:button {:type "button"
               :on-click #(on-change "")}
      close-x]]))

(defn user-form []
  (fn []
    (let [profile (<sub [:auth0/profile])
          dietary-restrictions
          (<sub [:user-form/dietary-restrictions-with-blank])
          polling? (<sub [:user-form/polling?])
          error-string (<sub [:user-form/error-string])]
      [:section.user-form-section

       (if polling?
         [:p.polling "thinking..."])
       (if (not-empty error-string)
         [:p.error error-string])

       [:form.user-form
        [:h3.title "create your account"]
        [:label "Name"]
        [:p.prefilled-input (:name profile)]
        [:label "Email"]
        [:p.prefilled-input (:email profile)]
        [:label "Dietary Restrictions"
         (map-indexed
          (fn [idx restriction]
            (let [id (str "dr" idx)]
              ^{:key id}
              [clearable-input
               {:id id
                :value restriction
                :on-change
                #(>evt
                  [:user-form/update-dietary-restrictions
                   idx %])
                :label
                (str "dietary restriction " idx)
                }]))
          dietary-restrictions)]
        [:button {:type "button"
                  :on-click #(>evt [:create-user])
                  :disabled polling?}
         "Next"]]])))

(defn user-host-form []
  (fn []
    (let [valid? (<sub [:host-form/search-valid?])
          polling? (<sub [:host-form/polling?])]
      [:form.user-form
       [:h3.title "where do you live?"]
       [:label "Address"
        [:input {:value (<sub [:host-form/address])
                 :on-change #(>evt [:host-form/update-address
                                    (-> % .-target .-value)])
                 }]]
       [:label "City"
        [:input {:value (<sub [:host-form/city])
                 :on-change #(>evt [:host-form/update-city
                                    (-> % .-target .-value)])
                 }]]
       [:label "State"
        [:input {:value (<sub [:host-form/state])
                 :on-change #(>evt [:host-form/update-state
                                    (-> % .-target .-value)])
                 }]]
       [:label "Zipcode"
        [:input {:value (<sub [:host-form/zipcode])
                 :on-change #(>evt [:host-form/update-zipcode
                                    (-> % .-target .-value)])
                 }]]
       [:button {:on-click #(>evt [:search-hosts])
                 :disabled (or (not valid?)
                               polling?)
                 :type "button"}
        "Next"]
       ]))
)

(defn host-search-results []
  (fn []
    (let [searched-hosts (<sub [:host-form/searched-hosts])]
      [:form.user-form
       (map (fn [host]
              ^{:key (str "host" (host :host-id))}
              [:button.host
               {:on-click
                #(>evt [:add-user-to-host (host :host-id)])
                :type "button"}
               [:h4 (host :address)]
               [:p (str (host :city) ", "
                        (host :state) ", "
                        (host :zipcode))]
               [:label "home of:"]
               [:p (clojure.string/join
                         ", "
                         (map :name (host :users)))]])
            searched-hosts)]
      )))

(defn create-host-form []
  (fn []
    (let [max-occupancy (<sub [:host-form/max-occupancy])
          valid? (<sub [:host-form/create-host-valid?])
          polling? (<sub [:host-form/polling?])]
      [:form.user-form
       [:label.question "How many people can your home fit?"
        [:input {:type "number"
                 :value max-occupancy
                 :on-change
                 #(>evt
                   [:host-form/update-max-occupancy
                    (-> % .-target .-value (js/parseInt))])}]]
       [:button {:on-click #(>evt [:create-host])
                 :disabled (every? true? [(not valid?)
                                          polling?])
                 :type "button"}
        "Submit"]])))

(defn add-host-to-user-page []
  (fn []
    (let [polling? (<sub [:host-form/polling?])
          error-string (<sub [:host-form/error-string])
          searched-hosts (<sub [:host-form/searched-hosts])]
      [:section.host-form
       (if polling?
         [:p.polling "thinking..."])
       (if (not-empty error-string)
           [:p.error error-string])
       (if (= searched-hosts :not-searched)
         [user-host-form]

         [:div
          (if (not-empty searched-hosts)
            [:div
             [host-search-results]
             [:h3.form-section-title "None of these?"]])
           [create-host-form]])
       ])))

(defn send-invites-page []
  (fn []
    (let [polling? (<sub [:send-invites/polling?])
          succeeded? (<sub [:send-invites/succeeded?])
          error (<sub [:send-invites/error])]
      [:form.send-invites
       (if polling?
         [:p "one sec..."])
       (if succeeded?
         [:p "Thanks!"])
       (if error
         [:p.error "uh-oh" error])
       [:h2 "Get the ball rolling"]
       [:h4 "Send emails to hosts who've hosted least recently"]
       [:label "How many hosts should create events?"
        [:input {:type "number"
                 :value (<sub [:send-invites/num-hosts])
                 :on-change #(>evt [:send-invites/set-num-hosts
                                    (-> % .-target .-value)])
                 }]]
       [:button {:type "button"
                 :disabled polling?
                 :on-click #(>evt [:send-invites])}
        "Send emails"]])))

(defn app []
  (let [page (<sub [:page])
        user-detail (<sub [:user-detail])
        access-token (<sub [:auth0/access-token])
        auth0-polling? (<sub [:auth0/polling?])
        auth0-error (<sub [:auth0/error])
        {:keys [sub]} (<sub [:auth0/profile])
        user (<sub [:user])
        user-assigned-dish (<sub [:user-assigned-dish])]
    [:main.app
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
         (cond ;; main page content
           (or (= user :no-account)
               (= page :create-user)) [user-form]
           (= page :add-host-to-user) [add-host-to-user-page]
           (= page :events) [events-page]
           (= page :create-event) [event-form]
           (= page :send-invites) [send-invites-page]
           :else [:div "Page not found"]))]))
