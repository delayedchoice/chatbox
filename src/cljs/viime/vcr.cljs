(ns viime.vcr
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [goog.events :as events]
            [secretary.core :as secretary]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [cljs-time.coerce :as trans]
            [cljs-time.core :as date]
            [cljs.core.async :as async]
            [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [r]
            [rw]))


(def app-state (atom {:showing :all
                      :todos []
                      :audio-recorder nil
                      :imageArray []
                      :recording false
                      :frame-time nil}))

(defn- get-screen [] (.getElementById js/document "video"))
(defn- get-replay [] (.getElementById js/document "playback"))

(def video-canvas (.getElementById js/document "canvas"))

(defn start-recording [stream]
  (let [recorder (js/MediaRecorder. stream)
        _ (println "starting")
        _ (aset recorder "ondataavailable" #(re-frame/dispatch [:add-frame (.-data %)]))
        _ (.start recorder)]
    {}))

(defn stop-recording [stream]
 (do (doseq [track (.getTracks stream)] (.stop track))
     (re-frame/dispatch [:recording-stopped]) ))

;(defn stop-recording [stream frames]
;  (let [ _ (println "stopping")]
;    (do (doseq [track (.getTracks stream)] (.stop track))
;        (let [_ (close! frames)
;                  _ (println "in go")
;                  data (<! (async/reduce conj [] frames))
;                  _ (println (str (count data) " reduced") )
;                  blob (js/Blob. (clj->js data)  (clj->js { "type" "video/webm" }) )
;                  _ (println (str "made a blob: " (.-type blob) " " (.-size blob)))
;                  _ (println (str (count data) " frames.") )
;                  ]
;              (aset (get-replay) "src" (.createObjectURL js/URL blob) )) )
;    ))


(defn- success [stream]
  (let []
    (do (js/alert "success")
      (aset (get-screen) "srcObject" stream #_(.createObjectURL (.-URL js/window) stream))
      (aset (get-screen) "onplaying" #(start-recording stream))
      (aset (get-screen) "onpause" #(stop-recording stream))

; recording.src = URL.createObjectURL(recordedBlob);
      (set! (.-muted (get-screen)) true))))

(defn- error [err] (js/alert (str "error" err)))

(defn- get-umedia []
  #(let [m (.getUserMedia (.-mediaDevices js/navigator) #js {:audio true :video true})
         _ (.then m success)
         _ (.catch m error)]
     {}))

(defn player []
  (let [video (re-frame/subscribe [:video])]
     [:div
      [:div.row (str "size: " (.-size video) " type: " (.-type video))]
      [:div.row [:video.col-md-4 {:controls true :id "video"}] ]
      [:div.row [:input.btn.btn-primary.col-md-4
                 {:type "button"
                  :onClick (get-umedia)
                  :value "Click me!"}]]
      [:div.row [:video.col-md-4 {:src @video :controls true :id "playback"}
                 ] ]]))
