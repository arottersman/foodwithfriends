(ns fwf.styles.user-form
  (:use [fwf.styles.utils])
  (:require [garden.selectors :as sel]))

(def user-form
  [[:form.user-form
    {:background white
     :padding (css-rem 1)
     :margin "auto"}

    (medium-screen
     [:& {:max-width (px 580)}])
    (large-screen
     [:& {:max-width (px 580)}])]

   [(sel/> :form.user-form :.title)
    {:margin-top 0
     :font-family fancy-font-family
     :margin-bottom (css-rem 1)}]

   [(sel/> :form.user-form :label)
    {:display "flex"
     :flex-direction "column"
     :font-family primary-font-family
     :font-size h4-font-size
     :color diner-grey}]

   [(sel/> :form.user-form :.prefilled-input)
    (assoc basic-input
           :padding (css-rem 1)
           :width "calc(100% - 1.5em)"
           :margin-top (css-rem 0.7)
           :margin-bottom (css-rem 1))]

   [:.clearable-input
    {:display "flex"
     :width (pc 100)
     :margin-bottom (em 1)
     :flex-direction "row"}]

   [(sel/> :.clearable-input :label)
    {:display "none"}]

   [(sel/> :.clearable-input :input)
    (sel/> :form.user-form :label :input)
    (assoc basic-input
           :width "calc(100% - 1.4em)"
           :padding-bottom 0
           :margin-bottom (css-rem 1))]

   [(sel/> :.clearable-input :button)
    (assoc debuttonified
           :cursor "pointer"
           :margin-bottom (css-rem 1)
           :padding-bottom 0
           :border-bottom "2px solid black")]

   [:.user-submit-container
    {:width (pc 100)
     :display "flex"
     :justify-content "center"}

    [:button.done
     {:background-color black
      :color white
      :cursor "pointer"
      :padding (css-rem 0.7)
      :border-radius (px 4)
      :font-size h4-font-size
      :font-family primary-font-family
      :width (px 250)}
     [:&:disabled {:opacity 0.8}]]]

   [:h3.form-section-title {:text-align "center"}]

   [(sel/> :form.user-form :button.host)
    (assoc debuttonified
           :width (pc 100)
           :border (str "3px dashed" diner-red)
           :text-align "left"
           :margin 0
           :margin-bottom (em 0.5)
           :border-radius (px 5)
           :font-variant "none")]

   [(sel/> :button.host :h4)
    (sel/> :button.host :p)
    (sel/> :button.host :label)
    {:margin (em 0.3)}]

   [(sel/> :button.host :p)
    {:font-weight 400}]
   [:form.user-form.search-results
    {:overflow-y "auto"
     :height (px 200)}]])
