(ns viime.modal
  (:require
   [reagent.core :as reagent]
   [re-frame.core :refer [dispatch subscribe]]))

(defn create-modal []
  [:div
   [:div.modal-header
    [:button.close {:type "button"  :data-dismiss "modal" }
     [:span {:dangerouslySetInnerHTML {:__html "&times;"}}]]]
   [:div.modal-body
    [:div {:id "videos"}
     [:video.selfVideo.easyrtcMirror {:autoPlay "autoplay" :id "self" :muted true }]
     [:video.callerVideo.callerDiv  {:autoPlay "autoplay" :id "caller"}]]]])
    

