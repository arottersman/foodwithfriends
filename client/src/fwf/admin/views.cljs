(ns fwf.admin.views
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [fwf.utils :refer [>evt <sub]]))

(defn send-invites-page []
  (fn []
    (let [polling? (<sub [:send-invites/polling?])
          succeeded? (<sub [:send-invites/succeeded?])
          error (<sub [:send-invites/error])]
      [:form.send-invites
       (if polling?
         [:p "one sec..."])
       (if succeeded?
         [:p "Thanks!"])
       (if error
         [:p.error "uh-oh" error])
       [:h2 "Get the ball rolling"]
       [:h4 "Send emails to hosts who've hosted least recently"]
       [:label "How many hosts should create events?"
        [:input {:type "number"
                 :value (<sub [:send-invites/num-hosts])
                 :on-change #(>evt [:send-invites/set-num-hosts
                                    (-> % .-target .-value)])
                 }]]
       [:button {:type "button"
                 :disabled polling?
                 :on-click #(>evt [:send-invites])}
        "Send emails"]])))
