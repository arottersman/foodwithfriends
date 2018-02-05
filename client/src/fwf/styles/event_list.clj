(ns fwf.styles.event-list
  (:use [fwf.styles.utils]))

(def event-list-menu-height (px 33))
(def section-height (str "calc(100vh - "
                      header-height ")"))
(def list-height (str "calc(100% - "
                      event-list-menu-height ")"))

(def event-list
  ;; Menu
  [[:section.events-section
    {:height section-height}]
   [:nav.event-list-menu
    {:display "flex"
     :flex "0 0 auto"
     :justify-content "center"
     :height event-list-menu-height}

    (small-screen
     [:&.event-list-menu
      {:justify-content "space-around"}])

    [:.menu-item
     {:font-family fancy-font-family
      :font-size h3-font-size
      :margin "0 1rem"
      :background "none"
      :border 0
      :cursor "pointer"}
     [:&.-active
      {:color diner-red}]
     [:&:hover
      :&:focus
      {:text-decoration "underline"}]]]

   ;; event list
   [:.event-list-container
    {:padding (em 1)
     :box-sizing "border-box"
     :width (px 450)
     :margin "auto"
     :overflow-x "hidden"
     :overflow-y "auto"
     :height list-height}
    (small-screen
     [:& {:margin 0
          :width "auto"}])]
   [:.center-container
    {:display "flex"
     :text-align "center"
     :padding "0 1rem"
     :margin-top (rem 4)
     :flex-direction "column"
     :align-items "center"
     :justify-content "flex-start"}
    [:.no-events
     :.error
     {:font-size h4-font-size
      :margin-top (rem 0.7)}]]
   ;; event list item
   [:ul.events {:padding 0
                :flex "1 0 auto"
                :margin 0
                :width (pc 100)
                :display "flex"
                :justify-content "stretch"
                :align-items "stretch"
                :flex-direction "column"}
    [:li.event {:display "flex"
                :margin-bottom (em 1)
                :padding (em 0.7)
                :border-radius (px 3)
                :border (str "3px dashed" diner-red)}
     [:&.-your-event
      {:border (str "3px solid" diner-red)}]]

    [:.event-snippet-container
     {:width (pc 100)
      :display "flex"
      :flex-direction "column"}]

    [:.event-snippet
     (assoc debuttonified
            :display "flex"
            :flex-wrap "wrap"
            :justify-content "space-between")
     [:.event-hosts
      {:white-space "nowrap"
       :overflow "hidden"
       :text-overflow "ellipsis"}]]

    [:.event-detail
     {:display "flex"
      :flex-wrap "wrap"
      :justify-content "space-between"}]

    [:.event-snippet
     :.event-detail
     {:color black
      :padding (px 5)
      :width (pc 100)}
     [:.event-date
      {:font-family fancy-font-family
       :font-size h1-font-size
       :margin 0
       :width (pc 80)
       :text-align "left"
       :font-weight "normal"
       :margin-bottom (rem 0.7)}]
     [:.event-time :.event-hosts
      {:font-size h4-font-size}]
     [:.event-time
      {:width (pc 35)
       :text-align "left"}]
     [:.event-hosts
      {:width (px 195)
       :text-align "left"}]
     [:p.event-description
      {:width (px 150)}]
     [:p.event-description
      :p.event-dietary-restrictions
      :p.no-people
      {:color diner-grey
       :margin-top (rem 0.6)
       :font-style "italic"}]
     [:p.event-dietary-restrictions
      :p.no-people
      {:text-align "center"
       :flex-grow 1}]
     [:.event-location
      {:width (px 195)
       :display "flex"
       :margin-top (rem 0.6)
       :justify-content "flex-end"}
      [:.icon
       {:margin-top (px 6)}]
      [:.address-line
       {:text-align "left"
        :display "inline"
        :margin-right (rem 0.4)}]]

     (small-screen
      [:.event-hosts
       {:width (px 195)
        :text-align "left"}]
      [:.event-description
       {:flex-grow 1
        :text-align "center"}]
      [:.event-location
       {:justify-content "flex-start"
        :margin "auto"}
       [:.address-line
        {:display "block"
         :margin-right 0}]])

     [:ul.people
      {:width (pc 100)
       :text-align "center"
       :list-style-type "none"
       :padding 0}
      [:button.user
       (assoc debuttonified
              :margin "auto"
              :width (px 200)
              :padding 0
              :display "flex"
              :justify-content "flex-start")]]

     [:.icon.down-chevron
      :.close
      {:align-self "flex-start"
       :padding-right 0}]]

    [:.close
     (assoc debuttonified
            :font-size (px 25)
            :line-height (px 25))]

    [:.rsvp
     :.email-chain
     {:align-self "center"
      :border-radius (px 2)
      :border "1px solid black"
      :color white
      :background black
      :font-size h4-font-size
      :cursor "pointer"
      :width (px 200)
      :padding (em 0.3)
      :margin-top "1.5rem"}
     [:&:disabled
      {:background diner-grey
       :border-color diner-grey}]]

    [:.too-tiny-to-see
     {:width 0
      :height 0
      :opacity 0}]

    [:.email-chain-container
     {:width (pc 100)
      :text-align "center"}]]])
