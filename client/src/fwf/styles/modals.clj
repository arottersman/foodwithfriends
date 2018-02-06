(ns fwf.styles.modals
  (:use [fwf.styles.utils]))

(def modal [:.modal-background
            {:background "rgba(0, 0, 0, 0.5)"
             :top 0
             :display "flex"
             :position "fixed"
             :z-index 200
             :width (pc 100)
             :height (pc 100)}
            [:.modal {:background white
                      :margin "auto"
                      :border-radius (px 5)
                      :position "relative"
                      :padding (css-rem 2)}
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
              :display "flex"
              :flex-direction "column"
              :align-items "center"
              :width (px 250)}
             [:h3.name {:margin "0.3rem 0 0.1rem 0"}]
             [:.icon
              {:height (px 70)
               :width (px 70)}]
             [:.user-dietary-rest
              {:font-style "italic"
               :font-size h4-font-size
               :color diner-grey}]
             [:.user-assigned-dish
              {:font-size h3-font-size
               :text-align "center"}]
             [:div.user-info
              {:margin-top (css-rem 0.7)}]]])
