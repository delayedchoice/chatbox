(ns viime.vcr
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [re-frame.core :as re-frame]))

(defn player []
  (fn []
    (let [video (re-frame/subscribe [:video])
          state (re-frame/subscribe [:state])]
     [:div.video-container
       [:video.col-md-8 {:src @video :controls true :id "playback" :hidden (= (:key @state ) :recording)}]
       [:video.col-md-8 {:autoPlay true :id "video" :hidden (= (:key @state) :playing)}]
       [:input.btn.btn-primary.col-md-8.centered-button
          {:type "button"
           :onClick (:fn @state)
           :value (:btn-text @state)}] ])))
