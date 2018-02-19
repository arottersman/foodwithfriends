(ns fwf.styles.core
  (:use [fwf.styles.utils])
  (:require
   [garden.def :refer [defstyles]]
   [garden.selectors :as sel]
   [garden.color :as color]
   [garden.stylesheet :refer [at-keyframes]]
   [fwf.styles.header :refer [header]]
   [fwf.styles.event-list :refer [event-list]]
   [fwf.styles.event-form :refer [event-form]]
   [fwf.styles.user-form :refer [user-form]]
   [fwf.styles.modals :refer [modal]]))

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

(def limbo-page
  [[:.limbo-page
    {:display "flex"
     :align-items "center"
     :flex-direction "column"
     :padding (css-rem 1)
     :text-align "center"
     :margin-top (css-rem 2)
     :font-size h2-font-size}
    [:.icon
     {:margin-bottom (css-rem 1)}]]])

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
  limbo-page
  user-form
  event-form
  modal)
