(ns viime.modal
  (:require
   [re-frame.core :refer [dispatch subscribe]]))


(defn modal-panel
  [{:keys [child size show?]}]
  [:div {:class "modal-wrapper"}
   [:div {:class "modal-backdrop"
          :on-click (fn [event]
                      (do
                        (dispatch [:modal {:show? (not show?)
                                           :child nil
                                           :size :default}])
                        (.preventDefault event)
                        (.stopPropagation event)))}]
   [:div {:class "modal-child"
          :style {:width (case size
                           :extra-small "15%"
                           :small "30%"
                           :large "70%"
                           :extra-large "85%"
                           "50%")}} child]])

(defn modal []
  (let [modal (subscribe [:modal])]
    (fn []
      [:div
       (if (:show? @modal)
         [modal-panel @modal])])))


(defn- close-modal []
  (dispatch [:modal {:show? false :child nil}]))

(defn create-modal [caller-easyrtc-id stream]
    (reagent/create-class                 ;; <-- expects a map of functions 
                         {:component-did-mount               ;; the name of a lifecycle function
                          #(dispatch [:easyrtc-accept-stream2 caller-easyrtc-id stream ])
                          :display-name  "videos"  ;; for more helpful warnings & errors
                          :reagent-render        ;; Note:  is not :render
                           (fn []
                              [:div {:class "modal-wrapper"}
                               [:div {:class "modal-backdrop"
                                      :on-click (fn [event]
                                                  (do
                                                    #_(dispatch [:modal {:show? (not show?)
                                                                       :child nil
                                                                       :size :default}])
                                                    (.preventDefault event)
                                                    (.stopPropagation event)))}]
                               [:div {:class "modal-child"
                                      :style {:width "50%" #_(case :small
                                                       :extra-small "15%"
                                                       :small "30%"
                                                       :large "70%"
                                                       :extra-large "85%"
                                                       "50%")}} 
                                              [:div.video-container {:id "videos"}
                                                         [:video.selfVideo.easyrtcMirror {:autoPlay "autoplay" :id "self" :muted true }]
                                                         [:video.callerVideo.callerDiv  {:autoPlay "autoplay" :id "caller"}]]]]                          ) })
  )
(defn videos []
  (let [caller-easyrtc-id (subscribe [:current-call])
        stream (subscribe :stream) ]
    (reagent/create-class                 ;; <-- expects a map of functions 
                         {:component-did-mount               ;; the name of a lifecycle function
                          #(dispatch [:easyrtc-accept-stream2 caller-easyrtc-id stream ])
                          :display-name  "videos"  ;; for more helpful warnings & errors
                          :reagent-render        ;; Note:  is not :render
                          (fn []
                            [:div.video-container {:id "videos"}
                             [:video.selfVideo.easyrtcMirror {:autoPlay "autoplay" :id "self" :muted true }]
                             [:video.callerVideo.callerDiv  {:autoPlay "autoplay" :id "caller"}]]) }))
  )

(defn- hello []
  [:div
   {:style {:background-color "white"
            :padding          "16px"
            :border-radius    "6px"
            :text-align "center"}} "Hello modal!"])


(defn- hello-bootstrap []
  [:div {:class "modal-content panel-danger"}
   [:div {:class "modal-header panel-heading"}
    [:button {:type "button" :title "Cancel"
              :class "close"
              :on-click #(close-modal)}
     [:i {:class "material-icons"} "close"]]
    [:h4 {:class "modal-title"} "Hello Bootstrap modal!"]]
   [:div {:class "modal-body"}
    [:div [:b (str "You can close me by clicking the Ok button, the X in the"
                   " top right corner, or by clicking on the backdrop.")]]]
   [:div {:class "modal-footer"}
    [:button {:type "button" :title "Ok"
              :class "btn btn-default"
              :on-click #(close-modal)} "Ok"]]])
