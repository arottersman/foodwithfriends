(ns fwf.styles.core
  (:use [fwf.styles.utils]
        [fwf.styles.constants])
  (:require
   [garden.def :refer [defstyles]]
   [garden.selectors :as sel]

   [garden.color :as color]
   [garden.stylesheet :refer [at-keyframes]]
   [fwf.styles.header :refer [header]]
   [fwf.styles.event-list :refer [event-list]]))

;; Colors
(def primary "#3a3239")
(def secondary "#54587A")
(def uh-oh-red "#FF4040")
(def accent "#0a2e69")

(def accent-light "#7694c3");;"#5e8eda")
(def background "#fff8f4")
(def light "#dfebec")
(def tea-green "#5cce1a")

(def height-md (px 40))
(def height-header (px 50))

;; from http://typecast.com/images/uploads/modernscale.css
(def typography
  [[:body {:font-size (pc 100)
           :font-color black}]

   [:body
    :caption
    :th
    :td
    :input
    :textarea
    :select
    :option
    :legend
    :fieldset
    :h1
    :h2
    :h3
    :h4
    :h5
    :h6
    :p {:font-size-adjust 0.5
        :font-family primary-font-family}]
   [:h1
    :h2
    :h3
    :h4
    :h5
    :h6
    :p {:margin "0.3em 0"}]

   :#app {:font-size (em 1) ;; eq. 16px
          :line-height 1.25} ;; eq. 20px

   (small-screen
    [:#page {:font-size (em 1) ;; eq. 16px
             :line-height 1.375}]) ;; eq. 22px

   [:h1 {:font-size h1-font-size
         :line-height 1.25}] ;; 45px/ 36px

   (small-screen
    [:h1 {:font-size (em 2.5)
          :line-height 1.125}])
   (large-screen
    [:h1 {:font-size (em  3)
          :line-height 1.05}])

   [:h2 {:font-size h2-font-size
         :line-height 1.15384615}]
   (small-screen
    [:h2 {:font-size (em 2)
          :line-height 1.25}])
   (large-screen
             [:h2 {:font-size (em 2.25)
                   :line-height 1.25}])

   [:h3 {:font-size h3-font-size
         :line-height 1.13636364}]
   (small-screen
    [:h3 {:font-size (em 1.5)
          :line-height 1.25}])
   (large-screen
             [:h3 {:font-size (em 1.75)
                   :line-height 1.25}])

   [:h4 {:font-size h4-font-size
         :line-height 1.11111111}]
   (small-screen
    [:h4 {:line-height (em 1.22222222)}])])

(def basic-button
  {:cursor "pointer"
   :width (pc 100)
   :color white
   :border 0
   :background accent
   :padding (em 0.7)
   :border-radius (px 50)
   :margin-top (em 1)
   :box-shadow (box-shadow
                (px 2)
                (px 2)
                (px 2)
                (px 2)
                [0 0 0 0.2])
   :font-variant "small-caps"
   :text-transform "lowercase"
   :font-weight 600
   :font-size h4-font-size})

(def basic-input
  {:margin "0.5em 0"
   :padding (em 0.5)
   :border 0
   :border-bottom "2px solid black"
   :color black
   :width "calc(100% - 1em)"
   :font-size h4-font-size})

(def auth-section
  [[:section.auth
    {:display "flex"
     :flex-direction "column"
     :background-color accent
     :color white
     :border-radius (px 300)
     :max-width (px 300)
     :height (px 120)
     :margin "auto"
     :box-shadow (box-shadow (px 6)
                                 (px 6)
                                 (px 6)
                                 (px 3)
                                 [0 0 0 0.3])}]
   [(sel/> :section.auth :p.error)
    {:margin "auto"
     :border 0
     :background "none"
     :color "white"
     :max-width (px 200)}]
   [(sel/> :section.auth :.login)
    {:font-size h3-font-size
     :color "white"
     :font-family "monospace"
     :margin "auto"
     :cursor "pointer"
     :text-decoration "none"}
    [:&:visited
     {:color light}]
    [:&:hover
     :&:focus
     {:text-decoration "underline"}]]])

(def modal [:.modal-background
            {:background "rgba(0, 0, 0, 0.5)"
             :display "flex"
             :position "fixed"
             :z-index 200
             :width (pc 100)
             :height (pc 100)}
            [:.modal {:background white
                      :margin "auto"
                      :border-radius (px 5)
                      :position "relative"
                      :padding (em 3)}
             (small-screen
              [:& {:border-radius 0}])
             [:button.close {:position "absolute"
                             :top (em 0.5)
                             :right (em 0.5)
                             :border 0
                             :background "none"
                             :cursor "pointer"
                             :font-size h4-font-size}]]
            [:.user-detail :.assigned-dish
             {:height "auto"
              :width (px 250)}
             [:h3.name {:margin "0 0 0.5em 0"}]
             [:div.user-info
              {:margin-top (em 0.7)}]]])


(def event-form
  [[:form.event-form
    {:background white
     :overflow-y "auto"
     :border-radius (px 5)
     :color primary
     :margin (em 1)
     :padding (em 0.5)}]

   (medium-screen
    [:form.event-form
     {:margin "auto"
      :padding "0.5em 1.5em"}])

   (large-screen
    [:form.event-form
     {:margin "auto"
      :padding "0.5em 1.5em"}])

   [:form.event-form
    [:h2
     :h4
     :h3
     {:margin 0
      :margin-bottom (em 1)}]]
   [(sel/> :form.event-form :label)
    {:display "block"}]
   [(sel/> :form.event-form :label :input)
    (sel/> :form.event-form :label :textarea)
    basic-input]

   [(sel/> :form.event-form :div.date-picker)
    {:height (px 260)
     :overflow-y "auto"}]
   [:div.date-picker
    [:button
     {:font-size (px 14)
      :font-weight 700
      :color white
      :background accent-light
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
      :background tea-green
      :border-radius (px 5)}]
    [:table {:margin-bottom (em 0.5)}]]
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
      :background accent-light
      :height (px 30)
      :margin (em 0.7)
      :margin-top "auto"
      :margin-bottom 0
      :color white}]]
   [:p.info {:margin-top (em 1)}]
   [:button.done
    basic-button
    [:&:disabled
     {:opacity 0.3}]]])

(def user-form [[:form.user-form
    {:background white
     :border-radius (px 5)
     :border 0
     :border-color secondary
     :color primary
     :padding (em 1)
     :margin "auto"
     :margin-top (em 1)}
    (small-screen
     [:& {:max-width (px 250)}])
    (medium-screen
     [:& {:max-width (px 580)}])
    (large-screen
     [:& {:max-width (px 580)}])]

   [(sel/> :form.user-form :.title)
    {:margin-top 0
     :margin-bottom (em 0.55)}]

   [(sel/> :form.user-form :label)
    {:color secondary
     :display "flex"
     :flex-direction "column"
     :font-weight 800
     :font-size h4-font-size
     :font-variant "small-caps"
     :text-transform "lowercase"}]

   [(sel/> :form.user-form :label.question)
    {:font-variant "none"}]

   [(sel/> :form.user-form :label :input)
    {:margin-bottom (em 0.7)}]

   [(sel/> :form.user-form :.prefilled-input)
    (assoc basic-input :padding (em 0.7)
                       :margin-top (em 0.3)
                       :margin-bottom (em 0.55))]

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
           :width "calc(100% - 1.4em)")]

   [(sel/> :.clearable-input :input)
    (sel/> :form.user-form :label :input)
    (sel/> :.clearable-input :button)
    {:font-size h4-font-size
     :margin "1px 0"
     :padding (em 0.7)
     :border 0}]

   [(sel/> :.clearable-input :input)
    {:border-radius "5px 0 0 5px"}]

   [(sel/> :.clearable-input :button)
    {:background accent
     :color "white"
     :cursor "pointer"
     :border-radius "0 5px 5px 0"}]

   [(sel/> :form.user-form :button)
    basic-button]
   [(sel/> :form.user-form :button)
    [:&:disabled {:box-shadow "none"
                  :opacity 0.8}]]

   [:h3.form-section-title {:text-align "center"}]
   [(sel/> :form.user-form :button.host)
    {:text-align "left"
     :margin 0
     :margin-bottom (em 0.5)
     :border-radius (px 5)
     :font-variant "none"}]
   [(sel/> :button.host :h4)
    (sel/> :button.host :p)
    (sel/> :button.host :label)
    {:margin (em 0.3)}]

   [(sel/> :button.host :p)
    {:font-weight 400}]
  ])

(def rsvped {:background tea-green
             :color white
             :font-size (em 1)
             :text-align "center"
             :padding-top (em 0.3)
             :font-weight "bolder"})
(def rsvp {:background accent
           :font-size (em 1)
           :color white
           :font-weight "bolder"
           :font-variant "small-caps"
           :text-transform "lowercase"
           :cursor "pointer"})

(def event-tag-right
  [[:.event-tag {:border 0
                 :border-radius "0 5px 5px 0"
                 :margin-top (px 10)}]
   [:.rsvped (assoc rsvped
                    :height (px 60))]
   [:.rsvp (assoc rsvp :height (px 60))]])

(def event-tag-under
  [[:.event-tag {:border-radius "0 0 5px 5px"
                 :border 0
                 :display "flex"
                 :margin-left "auto"
                 :width (px 75)}]
   [(sel/> :.event-tag :.icon)
    {:margin "auto"}]
   [:.rsvped (assoc rsvped
                    :height (px 44))]
   [:.rsvp (assoc rsvp
                  :height (px 44))]])

(defstyles style
  [:html :body :#app :.app {:width (pc 100)
                            :overflow "hidden"
                            :margin 0}]
  [:.app {:background-color white}]
  [:section :.page {:width (pc 100)
                  :height (pc 100)}]
  [:.icon {:width (px 25)
           :height (px 25)
           :margin-right "0.7rem"}]
  [:.icon.big
   {:width (px 100)
    :height (px 100)}]
  [:.icon.medium
   {:width (px 50)
    :height (px 50)}]
  (at-keyframes "spinnerRotate"
                [:from {:transform "rotate(0deg)"}]
                [:to {:transform "rotate(360deg)"}]) 
  [:.spin
   {:-webkit-animation-name "spinnerRotate"
    :-webkit-animation-duration "4s"
    :-webkit-animation-iteration-count "infinite"
    :-webkit-animation-timing-function "linear"
    :-moz-animation-name "spinnerRotate"
    :-moz-animation-duration "4s"
    :-moz-animation-iteration-count "infinite"
    :-moz-animation-timing-function "linear"
    :-ms-animation-name "spinnerRotate"
    :-ms-animation-duration "4s"
    :-ms-animation-iteration-count "infinite"
    :-ms-animation-timing-function "linear"}]
  [:.hidden
   {:display "none"}]
  typography
  header
  event-list
  auth-section
  user-form
  event-form
  modal)
