(ns fwf.styles.utils
  (:require [garden.stylesheet :refer [at-media]]))

(defn em [num]
  (str num "em"))
(defn pc [num]
  (str num "%"))
(defn pt [num]
  (str num "pt"))
(defn px [num]
  (str num "px"))
(defn rem [num]
  (str num "rem"))

(defn box-shadow [w x y z [r g b a]]
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
