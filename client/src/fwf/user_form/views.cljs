(ns fwf.user-form.views
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [fwf.utils :refer [>evt <sub]]
            [fwf.db :as db]))

(def close-x [:div
              {:dangerouslySetInnerHTML {:__html "&#x2613;"}}])

(defn clearable-input [{:keys [id value label on-change]}]
  (fn [{:keys [id value label on-change]}]
    [:div.clearable-input
     [:label {:for id} label]
     [:input {:type "text"
              :id id
              :value value
              :on-change #(-> % .-target .-value on-change)}]
     [:button {:type "button"
               :on-click #(on-change "")}
      close-x]]))

(defn user-form []
  (fn []
    (let [profile (<sub [:auth0/profile])
          dietary-restrictions
          (<sub [:user-form/dietary-restrictions-with-blank])
          polling? (<sub [:user-form/polling?])
          error-string (<sub [:user-form/error-string])]
      [:section.user-form-section

       (if polling?
         [:p.polling "thinking..."])
       (if (not-empty error-string)
         [:p.error error-string])

       [:form.user-form
        [:h2.title "Create your account"]
        [:label "Name"
          [:input {:value (<sub [:user-form/name])
                   :on-change #(>evt [:user-form/update-name
                                      (-> % .-target .-value)])
                   }]]
        [:label "Email"]
        [:p.prefilled-input (:email profile)]
        [:label "Dietary Restrictions"
         (map-indexed
          (fn [idx restriction]
            (let [id (str "dr" idx)]
              ^{:key id}
              [clearable-input
               {:id id
                :value restriction
                :on-change
                #(>evt
                  [:user-form/update-dietary-restrictions
                   idx %])
                :label
                (str "dietary restriction " idx)
                }]))
          dietary-restrictions)]
        [:div.user-submit-container
         [:button.done {:type "button"
                        :on-click #(>evt [:create-user])
                        :disabled polling?}
          "Next"]]]])))

(defn user-host-form []
  (fn []
    (let [valid? (<sub [:host-form/search-valid?])
          polling? (<sub [:host-form/polling?])]
      [:form.user-form
       [:h2.title "Where do you live?"]
       [:label "Address"
        [:input {:value (<sub [:host-form/address])
                 :on-change #(>evt [:host-form/update-address
                                    (-> % .-target .-value)])
                 }]]
       [:label "City"
        [:input {:value (<sub [:host-form/city])
                 :on-change #(>evt [:host-form/update-city
                                    (-> % .-target .-value)])
                 }]]
       [:label "State"
        [:input {:value (<sub [:host-form/state])
                 :on-change #(>evt [:host-form/update-state
                                    (-> % .-target .-value)])
                 }]]
       [:label "Zipcode"
        [:input {:value (<sub [:host-form/zipcode])
                 :on-change #(>evt [:host-form/update-zipcode
                                    (-> % .-target .-value)])
                 }]]
       [:div.user-submit-container
        [:button.done {:on-click #(>evt [:search-hosts])
                       :disabled (or (not valid?)
                                     polling?)
                       :type "button"}
         "Next"]]
       ]))
)

(defn host-search-results []
  (fn []
    (let [searched-hosts (<sub [:host-form/searched-hosts])]
      [:form.user-form
       (map (fn [host]
              ^{:key (str "host" (::db/host-id host))}
              [:button.host
               {:on-click
                #(>evt [:add-user-to-host (::db/host-id host)])
                :type "button"}
               [:h4 (::db/address host)]
               [:p (str (::db/city host) ", "
                        (::db/state host) ", "
                        (::db/zipcode host))]
               [:label "Home of:"]
               [:p (clojure.string/join
                         ", "
                         (map ::db/name (::db/users host)))]])
            searched-hosts)]
      )))

(defn create-host-form []
  (fn []
    (let [max-occupancy (<sub [:host-form/max-occupancy])
          valid? (<sub [:host-form/create-host-valid?])
          polling? (<sub [:host-form/polling?])]
      [:form.user-form
       [:label.question "How many people can your home fit?"
        [:input {:type "number"
                 :value max-occupancy
                 :on-change
                 #(>evt
                   [:host-form/update-max-occupancy
                    (-> % .-target .-value (js/parseInt))])}]]
       [:button {:on-click #(>evt [:create-host])
                 :disabled (every? true? [(not valid?)
                                          polling?])
                 :type "button"}
        "Submit"]])))

(defn add-host-to-user-page []
  (fn []
    (let [polling? (<sub [:host-form/polling?])
          error-string (<sub [:host-form/error-string])
          searched-hosts (<sub [:host-form/searched-hosts])]
      [:section.host-form
       (if polling?
         [:p.polling "thinking..."])
       (if (not-empty error-string)
           [:p.error error-string])
       (if (= searched-hosts :not-searched)
         [user-host-form]

         [:div
          (if (not-empty searched-hosts)
            [:div
             [host-search-results]
             [:h3.form-section-title "None of these?"]])
           [create-host-form]])
       ])))
