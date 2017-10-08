(ns fwf.utils
  (:require
   [clojure.string :as str]
   [goog.crypt.base64 :as b64]
   [re-frame.core :as re-frame]
   [secretary.core :as secretary]
   [fwf.constants :refer [auth0-state]]))

(def <sub (comp deref re-frame/subscribe))
(def >evt re-frame/dispatch)

(defn href->param-val [href param-name]
  (let [pieces (str/split href #"\?|=|&|#|/")
        val (->> pieces
                 (drop-while #(not= % param-name))
                 (drop 1)
                 (first))]
    (and val
         (js/decodeURI val))))

(defn valid-auth0-redirect? [href]
  (= auth0-state (href->param-val href "state")))

(defn parse-id-token [id-token]
  (b64/decodeString (second (str/split id-token #"\."))))

(defn navigate-to!
  [path]
  (set! (.-location js/window)
        (str "/#" path))
  (secretary/dispatch! path))
