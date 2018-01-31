(ns fwf.styles.constants
  (:use [fwf.styles.utils]))

;; -- Colors
(def diner-red "#d13833")
(def diner-grey "#808080")
(def black "#000000")
(def white "#FFFFFF")

;; -- Typography
(def h1-font-size (em 2)) ;; 2x body copy size = 32px
(def h2-font-size (em 1.625))
(def h3-font-size (em 1.375))
(def h4-font-size (em 1.125))
(def p-font-size h4-font-size)

(def primary-font-family "'Dosis', sans-serif")
(def fancy-font-family "'Damion', cursive")

(def header-height (px 85))
