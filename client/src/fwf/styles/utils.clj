(ns fwf.styles.utils
  (:require [garden.stylesheet :refer [at-media]]))

;; -- Colors
(def diner-red "#d13833")
(def diner-grey "#808080")
(def diner-grey-light "#dddddd")
(def black "#000000")
(def white "#FFFFFF")

(def primary-font-family "'Dosis', sans-serif")
(def fancy-font-family "'Damion', cursive")

(defn em [num]
  (str num "em"))
(defn pc [num]
  (str num "%"))
(defn pt [num]
  (str num "pt"))
(defn px [num]
  (str num "px"))
(defn css-rem [num]
  (str num "rem"))

(defn box-shadow [w x y z [r g b a]]
  (str w " " x " " y " " z " rgba(" r "," g "," b "," a ")"))

;; -- Typography
(def h1-font-size (em 2)) ;; 2x body copy size = 32px
(def h2-font-size (em 1.625))
(def h3-font-size (em 1.375))
(def h4-font-size (em 1.125))
(def p-font-size h4-font-size)

;; -- Heights
(def header-height (px 85))

;; -- Breakpoints
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

(def debuttonified
  {:border 0
   :background "none"
   :cursor "pointer"
   :font-size (pc 100)
   :font-family primary-font-family})

(def basic-input
  {:margin "0.5em 0"
   :padding (em 0.5)
   :border 0
   :border-bottom "2px solid black"
   :color black
   :width "calc(100% - 1em)"
   :font-size h4-font-size})
