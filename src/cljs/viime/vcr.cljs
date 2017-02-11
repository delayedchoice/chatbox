(ns viime.vcr
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [re-frame.core :as re-frame]))

(defn- get-screen [] (.getElementById js/document "video"))

(defn start-recording [stream]
  (let [recorder (js/MediaRecorder. stream)
        _ (aset recorder "ondataavailable"
                #(re-frame/dispatch [:add-frame (.-data %)]))
        _ (.start recorder)]
    recorder))

(defn stop-recording [stream]
 (do (doseq [track (.getTracks stream)] (.stop track))
     (re-frame/dispatch [:recording-stopped]) ))

(defn- success [stream]
  (let [_ (println (js->clj (type stream) ) )]
    (aset (get-screen) "srcObject" stream)
    (aset (get-screen) "onplaying" #(start-recording stream))
    (aset (get-screen) "onpause"   #(stop-recording stream))
    (set! (.-muted (get-screen)) true)
    (re-frame/dispatch [:set-state :recording])))

(defn- error [err] (js/alert (str "error" err)))

(defn- get-umedia []
  #(let [m (.getUserMedia (.-mediaDevices js/navigator) #js {:audio true :video true})
         _ (.then m success)
         _ (.catch m error)]))

(defn player []
  (let [video (re-frame/subscribe [:video])
        state (re-frame/subscribe [:state])]
     [:div
      [:div.row {:hidden (= @state :recording)} [:video.col-md-4 {:src @video :controls true :id "playback"}]]
      [:div.row {:hidden (= @state :playing)}   [:video.col-md-4 {:controls true :id "video"}] ]
      [:div.row [:input.btn.btn-primary.col-md-4
                 {:type "button"
                  :onClick (get-umedia)
                  :value "Record"}]]]))
