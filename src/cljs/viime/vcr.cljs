(ns viime.vcr
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [re-frame.core :as re-frame]))

(defn player []
  (fn []
    (let [video (re-frame/subscribe [:video])
          state (re-frame/subscribe [:state])]
     [:div
      [:div.row {:hidden (= (:key @state ) :recording)}
       [:video.col-md-4 {:src @video :controls true :id "playback" }]]
      [:div.row {:hidden (= (:key @state) :playing)}
       [:video.col-md-4 {:autoPlay true :id "video"}] ]
      [:div.row [:input.btn.btn-primary.col-md-4
                 {:type "button"
                  :onClick (:fn @state)
                  :value (:btn-text @state)}]]])))
