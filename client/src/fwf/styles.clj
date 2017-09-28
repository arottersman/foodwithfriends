(ns fwf.styles
  (:require
   [garden.def :refer [defstyles]]
   [garden.selectors :as sel]
   [garden.color :as color]
   [garden.stylesheet :refer [at-media]]))


;; Type utils
(defn em [num]
  (str num "em"))
(defn pc [num]
  (str num "%"))
(defn pt [num]
  (str num "pt"))
(defn px [num]
  (str num "px"))
(defn box-shadow-val [w x y z [r g b a]]
   (str w " " x " " y " " z " rgba(" r "," g "," b "," a ")"))

;; Breakpoints

(def breakpoint-text-sm {:min-width (em 43.75)})

(def breakpoint-text-lg {:min-width (em 56.25)})

;; Colors

;; (def buff "#F3DE8A")
;; (def buff-rgb [243 222 138])
;; (def raisin-black "#241623")
;; (def french-pink "#FF729F")
;; (def pale-blue "#517aaf")
;; (def japanese-indigo "#274060")


;; (def primary "#241623")
(def primary "#3a3239")
(def secondary "#54587A")
(def accent "#FF4040")
;; (def background "#FFDDCA")
(def background "#fbf0ea")
;;(def light "#CAE7B9")
(def light "#dfe7ec")
(def white "#FFFFFF")
(def tea-green "#5cce1a")
;; (def tea-green "#CAE7B9")

;; Font Sizes

(def h1-font-size (em 2)) ;; 2x body copy size = 32px
(def h2-font-size (em 1.625))
(def h3-font-size (em 1.375))
(def h4-font-size (em 1.125))

(def height-md (px 40))

;; from http://typecast.com/images/uploads/modernscale.css
(def typography
  [[:body {:font-size (pc 100)
           :font-color primary}]

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
    :h6 {:font-size-adjust 0.5}]

   :#app {:font-size (em 1) ;; eq. 16px
          :line-height 1.25} ;; eq. 20px

   (at-media breakpoint-text-sm
             [:#page {:font-size (em 1) ;; eq. 16px
                      :line-height 1.375}]) ;; eq. 22px

   [:h1 {:font-size h1-font-size
         :line-height 1.25}] ;; 45px/ 36px
   (at-media breakpoint-text-sm
             [:h1 {:font-size (em 2.5)
                   :line-height 1.125}])
   (at-media breakpoint-text-lg
             [:h1 {:font-size (em  3)
                   :line-height 1.05}])

   [:h2 {:font-size h2-font-size
         :line-height 1.15384615}]
   (at-media breakpoint-text-sm
             [:h2 {:font-size (em 2)
                   :line-height 1.25}])
   (at-media breakpoint-text-lg
             [:h2 {:font-size (em 2.25)
                   :line-height 1.25}])

   [:h3 {:font-size h3-font-size
         :line-height 1.13636364}]
   (at-media breakpoint-text-sm
             [:h3 {:font-size (em 1.5)
                   :line-height 1.25}])
   (at-media breakpoint-text-lg
             [:h3 {:font-size (em 1.75)
                   :line-height 1.25}])

   [:h4 {:font-size h4-font-size
         :line-height 1.11111111}]
   (at-media breakpoint-text-sm
             [:h4 {:line-height (em 1.22222222)}])])

(def auth-section
  [[:section.auth
    {:display "flex"
     :flex-direction "column"
     :background-color white
     :color secondary
     :border "2px solid"
     :border-radius (px 50)
     :max-width (px 400)
     :height (px 120)
     :margin "auto"
     :box-shadow (box-shadow-val (px 6)
                                 (px 6)
                                 (px 6)
                                 (px 3)
                                 [0 0 0 0.3])}]
   [(sel/> :section.auth :p.error)
    {:font-family "sans-serif"
     :margin "auto"
     :max-width (px 300)
     :color accent}]
   [(sel/> :section.auth :.login)
    {:font-family "monospace"
     :font-size h2-font-size
     :margin "auto"
     :cursor "pointer"
     :text-decoration "none"}
    [:&:visited
     {:color primary}]
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
                      :padding (em 1)}
             [:button.close {:position "absolute"
                             :top (em 0.5)
                             :right (em 0.5)
                             :border 0
                             :background "none"
                             :cursor "pointer"
                             :font-size h4-font-size}]]
            [:.user-detail :.assigned-dish
             {:height "auto"
              :width (px 250)
              :font-family "sans-serif"}
             [:h3.name {:margin "0 0 0.5em 0"}]]])

(def events-section
  [[:section.events-section
    {:color secondary
     :display "flex"
     :flex-direction "column"}]
   [:.event-tab-group
    {:display "flex"
     :flex-grow 1
     :height (px 52)
     :margin (em 1)
     :margin-bottom 0}]
   [(sel/> :.event-tab-group :label.event-tab)
    {:background light
     :margin-right (px 5)
     :font-size h4-font-size
     :text-transform "lowercase"
     :font-variant "small-caps"
     :line-height (px 20)
     :font-weight 600
     :padding (em 0.75)
     :border-radius "5px 5px 0px 0px"
     :border-top "2px solid"
     :border-bottom "0px solid"
     :border-left "2px solid"
     :border-right "2px solid"
     :cursor "pointer"
     :font-family "sans-serif"}]
   [(sel/+ :input.tab-radio :label.event-tab)
    {:opacity 0.5}
    [:&:hover
     {:opacity 0.75}]]
   [(sel/+ :input.tab-radio:checked :label.event-tab)
    {:opacity 1
     :font-weight 600
     :z-index 100
     :height (px 16)}]
   [(sel/> :.event-tab-group :input.tab-radio)
    {:display "none"}]
   [(sel/> :section.events-section :.active-event-tab)
    {:background light
     :overflow "auto"
     :display "flex"
     :border "2px solid"
     :height (pc 100)
     :padding (em 1)
     :border-radius "0px 5px 5px 5px"
     :margin "0em 1em 1em 1em"
     :font-family "sans-serif"}
    [:.no-events :.error {:margin "auto"
                          :font-size h2-font-size
                          :text-align "center"}]
    [:ul.events {:padding 0
                 :width (pc 100)}
     [:li.event {:display "flex"
                 :padding-bottom (em 1)}
      [:.event-tag {:border 0
                    :border-radius "0 5px 5px 0"
                    :margin-top (px 10)}]
      [:.your-house {:background secondary
                     :height (px 28)
                     :width (px 33)
                     :padding "0.5em 0 0.5em 0.5em"}]
      [:.rsvped {:background tea-green
                 :height (px 28)
                 :width (px 33)
                 :padding "0.5em 0 0.5em 0.5em"}]
      [:.rsvp {:background accent
               :height (px 60)
               :font-size (em 1)
               :font-variant "small-caps"
               :text-transform "lowercase"
               :cursor "pointer"}]
      [:button.event-snippet {:cursor "pointer"
                              :width (pc 100)
                              :position "relative"}
       [:.expand {:position "absolute"
                  :top (em 0.2)
                  :right (em 0.5)
                  :font-size h3-font-size
                  :margin 0}]]
      [:button.event-snippet
       :.event-detail{:text-align "left"
                      :color primary
                      :position "relative"
                      :background white
                      :width (pc 100)
                      :padding "0.75em 0.75em"
                      :border-radius (px 5)
                      :border "0px solid"
                      :box-shadow (box-shadow-val
                                   (px 2)
                                    (px 2)
                                    (px 2)
                                    (px 2)
                                    [0 0 0 0.2])
                      :font-size h4-font-size}
       [:button:&:hover :button:&:focus {:opacity 0.8}]
       [:button.close {:position "absolute"
                       :right (em 0.5)
                       :cursor "pointer"
                       :top (em 0.5)
                       :border 0
                       :font-size h4-font-size
                       :color secondary
                       :opacity 0.9
                       :background "none"
                       :font-weight 900}
        [:&:hover :&:focus {:color primary
                            :opacity 1}]]
       [:h3 {:margin "0 0 0.25em 0"}]
       [:p.description :.location :div.time :p.no-people
        {:margin "0 0 0.5em 0"}]
       [:.list-label {:font-weight 600
                      :margin-top (em 0.75)}]
       [:ul.people {:padding-left 0}]
       [(sel/> :ul.people :li.person) {:padding-left (em 0.5)
                                       :color secondary
                                       :display "block"
                                       :margin-bottom (em 0.5)}
        [:a.user {:cursor "pointer"
                  :text-decoration "underline dotted"}
         [:&:hover :&:focus {:text-decoration "underline"
                             :color accent}]]
        [:span.accent {:color accent
                       :margin-right (px 5)
                       :font-weight 900}]]]]]]
   ])

(def site-header
  [[:nav.site-nav
    {:background-color secondary
     :display "flex"
     :border 0
     :box-shadow (box-shadow-val
                  (px 0)
                  (px 1)
                  (px 4)
                  (px 5)
                  [0 0 0 0.2])
     :flex "0 0 100%"}]
   [(sel/> :nav.site-nav :.site-title)
    {:font-family "monospace"
     :color white
     :text-transform "lowercase"}]
   [(sel/> :nav.site-nav :.signout)
    {:font-family "sans-serif"
     :font-variant "small-caps"
     :font-weight 600
     :margin-right (em 0.5)
     :margin-left "auto"
     :line-height (px 20)
     :text-transform "lowercase"
     :font-size h4-font-size
     :margin-top "auto"
     :margin-bottom "auto"
     :padding "0.5em 1.5em"
     :color secondary
     :border "0px solid"
     :border-radius (px 20)
     :cursor "pointer"
     :background white}
    [:&:hover
     :&:focus
     {:opacity 0.8}]]
   [(sel/> :nav.site-nav :.site-title)
     {:margin-left (em 1)}]])

(defstyles style
  [:html :body :#app :.app {:width (pc 100)
                            :height (pc 100)
                            :margin 0}]
  [:.app {:display "flex"
          :background-color background}]
  [:section :.page {:width (pc 100)
                    :height (pc 95)}]
  [:.icon {:margin-right (em 0.5)}]
  typography
  site-header
  auth-section
  events-section
  modal)
