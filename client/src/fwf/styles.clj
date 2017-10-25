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
;; http://noprompt.github.io/clojurescript/2014/02/10/media-query-breakpoints-with-garden.html

(defmacro defbreakpoint [name media-params]
  `(defn ~name [& rules#]
     (at-media ~media-params
               [:& rules#])))

(defbreakpoint small-screen
  {:screen true
   :min-width (px 320)
   :max-width (px 480)})

(defbreakpoint medium-screen
  {:screen true
   :min-width (px 481)
   :max-width (px 1023)})

(defbreakpoint large-screen
  {:screen true
   :min-width (px 1024)})

;; Colors
(def primary "#3a3239")
(def secondary "#54587A")
(def uh-oh-red "#FF4040")
(def accent "#0a2e69")

(def accent-light "#7694c3");;"#5e8eda")
(def background "#fff8f4")
(def light "#dfebec")
(def white "#FFFFFF")
(def tea-green "#5cce1a")

;; Font Sizes

(def h1-font-size (em 2)) ;; 2x body copy size = 32px
(def h2-font-size (em 1.625))
(def h3-font-size (em 1.375))
(def h4-font-size (em 1.125))

(def height-md (px 40))
(def height-header (px 50))

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
    :h6
    :p {:font-size-adjust 0.5
        :font-family "sans-serif"}]
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
   :box-shadow (box-shadow-val
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
   :border-radius (px 5)
   :border 0
   :background accent-light
   :color white
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
     :box-shadow (box-shadow-val (px 6)
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
      :box-shadow (box-shadow-val
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
    (sel/> :.form.user-form :label :input)
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

(def events-section
  [[:section.events-section
    {:color secondary
     :display "flex"
     :height (str "calc(100% - " height-header ")")
     :flex-direction "column"}]

   [:.event-tab-group
    {:display "flex"
     :flex-grow 1
     :height (px 55)
     :margin-left "auto"
     :margin-right "auto"
     :margin-top (em 1)
     :margin-bottom 0}
    (small-screen
     [:& {:margin "1em 1em 0 1em"}])]

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
     :cursor "pointer"}]

   [(sel/+ :input.tab-radio :label.event-tab)
    {:opacity 0.5}
    [:&:hover
     {:opacity 0.75}]]

   [(sel/+ :input.tab-radio:checked :label.event-tab)
    {:opacity 1
     :font-weight 600
     :z-index 100
     :height (px 20)}]

   [(sel/> :.event-tab-group :input.tab-radio)
    {:display "none"}]

   [(sel/> :section.events-section :.active-event-tab)
    {:background light
     :overflow "auto"
     :display "flex"
     :height (pc 100)
     :padding (em 1)
     :width (px 580)
     :border-radius "0px 5px 5px 5px"
     :margin "auto"}

    (small-screen
     [:& {:margin 0
          :width "auto"}])

    [:.no-events :.error {:margin "auto"
                          :font-size h2-font-size
                          :text-align "center"}]

    [:ul.events {:padding 0
                 :width (pc 100)}

     [:li.event {:display "flex"
                 :padding-bottom (em 1)}

      (small-screen [:& {:flex-direction "column"}
                     event-tag-under])
      (medium-screen event-tag-right)
      (large-screen event-tag-right)

      [:button.event-snippet {:cursor "pointer"
                              :width (pc 100)
                              :position "relative"}
       [:.expand {:position "absolute"
                  :top (em 0.2)
                  :right (em 0.5)
                  :font-size h3-font-size
                  :margin 0}]]
      [:button.event-snippet
       :.event-detail {:text-align "left"
                       :flex-grow 1
                       :width (pc 100)
                       :color primary
                       :position "relative"
                       :background white
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
       [:div.bottom-row {:display "flex"
                         :flex "0 0 100%"}]
       [:a.email-chain (assoc basic-button
                              :padding (em 0.3)
                              :text-decoration "none"
                              :text-align "center"
                              :margin-right "auto"
                              :margin-left "auto"
                              :line-height (px 30))]
       [:a.email-chain.-rsvped {:background tea-green}]
       [(sel/> :ul.people :li.person) {:padding-left (em 0.5)
                                       :color secondary
                                       :display "block"
                                       :margin-bottom (em 0.5)}
        [:a.user {:cursor "pointer"}
         [:&:hover :&:focus {:text-decoration "underline"
                             :color accent}]]
        [:span.accent {:color accent
                       :margin-right (px 5)
                       :font-weight 900}]]]]]]
   ])

(def site-header
  [[:nav.site-nav
    {:background-color accent
     :display "flex"
     :height height-header
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
     :margin "auto"
     :color white
     :text-transform "lowercase"}]
   [(sel/> :nav.site-nav :.signout)
    {:font-variant "small-caps"
     :font-weight 600
     :margin-right (em 0.5)
     :margin-left "auto"
     :line-height (px 20)
     :text-transform "lowercase"
     :font-size h4-font-size
     :margin-top "auto"
     :margin-bottom "auto"
     :padding "0.5em 1.5em"
     :color accent
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
                  :height (pc 100)}]
  [:.icon {:margin-right (em 0.5)}]
  [:.error
   :.polling
   {:margin "auto"
    :padding "0.5em 2em"
    :background white
    :text-align "center"
    :max-width (px 250)
    :border "2px solid"
    :border-radius (px 50)
    :font-weight 800}]
  [:.error
   {:background uh-oh-red
    :margin-top (em 0.2)
    :color "white !important"}]
  [:.hidden
   {:display "none"}]
  typography
  site-header
  auth-section
  user-form
  events-section
  event-form
  modal)
