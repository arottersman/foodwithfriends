(ns fwf.event-form.views
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [cljs-time.core]
            [cljs-time.format]
            [cljs-time.periodic]
            [fwf.utils :refer [>evt <sub]]))

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
          {:keys [fwf.db/hour
                  fwf.db/minute
                  fwf.db/time-of-day]}
          (<sub [:event-form/happening-at-time-strs])
          valid? (<sub [:event-form/valid?])
          polling? (<sub [:event-form/polling?])
          error (<sub [:event-form/error-string])]
      [:form.event-form
       [:h2.event-header "Create your event"]
       [:label "Give a description or maybe a little theme"
        [:input {:value title
                 :on-change #(>evt [:event-form/update-title
                                    (-> % .-target .-value)])
                 }]]
       [:h3 "When will it be?"]
       [multi-month-datepicker]
       [:h3 "What time?"]
       [timepicker {:hour hour
                    :minute minute
                    :time-of-day time-of-day
                    :on-change (fn [hour-str minute-str new-time-of-day]
                                 (>evt [:event-form/update-happening-at-time
                                        hour-str
                                        minute-str
                                        new-time-of-day]))}]
       [:p.info
        "After you're done, make sure to rsvp to your"
        " event."]
       (if error
         [:p.error error])
       [:div.event-submit-container
        [:button.done {:type "button"
                       :on-click #(>evt [:create-event])
                       :disabled (or (not valid?)
                                     polling?)}
         "Done!"]]
       ])))
