(ns fwf.styles.event-form
  (:use [fwf.styles.utils])
  (:require [garden.selectors :as sel]))

(def event-form
  [[:form.event-form
    {:overflow-y "auto"
     :max-width (px 450)
     :max-height (str "calc(100vh - " header-height " - 20px)")
     :border-radius (px 5)
     :color black
     :padding (px 20)
     :padding-top 0}]

   (medium-screen
    [:form.event-form
     {:margin "auto"}])

   (large-screen
    [:form.event-form
     {:margin "auto"}])

   [:h2
    {:font-family fancy-font-family}]

   [:h2
    :h3
    {:margin-bottom (css-rem 1)}]

   [(sel/> :form.event-form :label)
    {:display "block"
     :color diner-grey
     :font-family primary-font-family}]

   [(sel/> :form.event-form :label :input)
    (sel/> :form.event-form :label :textarea)
    basic-input]

   [(sel/> :form.event-form :div.date-picker)
    {:height (px 260)
     :overflow-y "auto"
     :border-radius (px 3)
     :border (str "3px dashed" diner-red)
     :padding (css-rem 1)
     :margin-bottom (css-rem 1)
     :display "flex"
     :flex-direction "column"
     :align-items "center"}]

   [:div.date-picker
    [:button
     {:font-size (px 14)
      :font-weight 700
      :color white
      :background black
      :border 0
      :border-radius (px 5)
      :margin (px 1)
      :box-shadow (box-shadow
                   (px 1)
                   (px 1)
                   (px 1)
                   (px 1)
                   [0 0 0 0.2])
      :width (px 32)
      :cursor "pointer"}]
    [:div.blank-date
     :div.selected-date
     {:text-align "center"}]
    [:div.selected-date
     {:font-size (px 14)
      :font-weight 800
      :line-height (px 22)
      :height (px 22)
      :color white
      :background diner-red
      :border-radius (px 5)}]
    [:table {:margin-bottom (em 0.5)}
     [:th {:font-family fancy-font-family}]]]
   [:div.timepicker
    {:display "flex"}
    [:label.unit
     {:display "block"}
     [:input
      {:font-size h3-font-size
       :margin-right (em 0.5)
       :display "block"
       :width (px 34)}]]
    [:select.am-pm
     {:font-size h4-font-size
      :border 0
      :background black
      :height (px 30)
      :margin (em 0.7)
      :margin-top "auto"
      :margin-bottom 0
      :color white}]]
   [:p.info {:margin-top (css-rem 1)
             :font-size h4-font-size
             :font-family primary-font-family
             :color diner-grey
             :text-align "center"}]

   [:p.error
    {:color diner-red
     :margin-top (css-rem 1)
     :text-align "center"}]

   [:.event-submit-container
    {:display "flex"}

    [:button.done
     {:background-color black
      :font-size h3-font-size
      :margin "auto"
      :margin-top (css-rem 1)
      :width (px 250)
      :color white
      :font-family primary-font-family}
     [:&:disabled
      {:opacity 0.3}]]]
  [:section.cant-host
   {:background diner-grey-light
    :width "auto"
    :padding (css-rem 1)
    :text-align "center"
    :margin-bottom (css-rem 1)}
   [:.cant-host-info
    {:font-size h4-font-size}]
   [:.cant-host-button
    (assoc debuttonified
           :text-decoration "underline"
           :color diner-red
           :font-weight 700)]]])
