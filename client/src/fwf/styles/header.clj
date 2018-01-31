(ns fwf.styles.header
  (:use fwf.styles.utils
        fwf.styles.constants)
  (:require [garden.selectors :as sel]))

(def hexagon-width (px 200))

(def hexagon-height (px 42))
(def hexagon-border-radius (px 4))
(def hexagon-box-shadow (box-shadow 0 0 (px 20) 0
                                    [0 0 0 0.6]))
(def triangle-y-scale 0.1574)
(def triangle-w-h-val 141.42)
(def triangle-w-h (px triangle-w-h-val))
(def triangle-offset (str "-" (/ triangle-w-h-val 2) "px"))
(def triangle-left (px 29.2893))

(def stripe-height (px 10))

(defn- transform-hexagon [y-scale]
  (str "scaleY(" y-scale ") rotate(-45deg)"))

(def header
  [[:.hexagon
    {:position "relative"
     :width hexagon-width
     :height hexagon-height
     :background-color diner-red
     :margin "57.74px 0"
     :box-shadow hexagon-box-shadow
     :border-radius hexagon-border-radius}]

   [(sel/div :.hexagon (sel/before))
    (sel/div :.hexagon (sel/after))
    {:content "''"
     :position "absolute"
     :z-index 1
     :width triangle-w-h
     :height triangle-w-h
     :-webkit-transform (transform-hexagon triangle-y-scale)
     :-ms-transform (transform-hexagon triangle-y-scale)
     :transform (transform-hexagon triangle-y-scale)
     :background-color "inherit"
     :left triangle-left
     :box-shadow hexagon-box-shadow
     :border-radius hexagon-border-radius}]

   [(sel/div :.hexagon (sel/before))
    {:top triangle-offset}]

   [(sel/div :.hexagon (sel/after))
    {:bottom triangle-offset}]

   [:.hexagon
    [:span
     {:display "block"
      :position "absolute"
      :top (px 0)
      :left (px 0)
      :width hexagon-width
      :height hexagon-height
      :line-height hexagon-height
      :z-index 2
      :background "inherit"
      :border-radius hexagon-border-radius
      :text-align "center"
      :vertical-align "middle"
      :color white
      :font-size h1-font-size
      :font-weight 600}]]

   [:.header
    {:display "flex"
     :align-items "center"
     :justify-content "center"
     :width (pc 100)
     :height header-height}]

   [:.stripes
    {:position "absolute"
     :width (pc 100)}]

   [:.stripe
    {:height stripe-height
     :background-color diner-red
     :width (pc 100)}]
   [:.stripe.-middle
    {:background-color diner-grey}]])
