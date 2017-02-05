(ns viime.vcr
  (:require [goog.events :as events]
            [secretary.core :as secretary]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [cljs-time.coerce :as trans]
            [cljs-time.core :as date]
            [r]
            [rw]))


(def ENTER_KEY 13)

(def app-state (atom {:showing :all
                      :todos []
                      :audio-recorder nil
                      :imageArray []
                      :recording false
                      :frame-time nil}))

;;=====================
;;media

(defn- get-screen [] (.getElementById js/document "video"))

(def video-canvas (.getElementById js/document "canvas"))

(def video-context (.getContext video-canvas "2d"))

(def supports-media  (or (fn? (.-getusermedia js/navigator ))
                         (fn? (.-webkitgetusermedia js/navigator ))
                         (fn? (.-mozgetusermedia js/navigator ))
                         (fn? (.-msgetusermedia js/navigator ))))

(def raf (some identity [(fn? (.-requestAnimationFrame js/window ))
                         (fn? (.-webkitRequestAnimationFrame js/window ))
                         (fn? (.-mozRequestAnimationFrame js/window ))
                         (fn? (.-mozRequestAnimationFrame js/window ))
                         (fn? (.-msRequestAnimationFrame js/window ))]))

(defn- request-animation-frame [record-frame] (raf record-frame))

(defn- get-user-media [] (some identity (list (.-getUserMedia js/navigator)
                                (.-webkitGetUserMedia js/navigator)
                                (.-mozGetUserMedia js/navigator)
                                (.-msGetUserMedia js/navigator) )))

;(def window-url (.-URL js/window))

(defn- complete-recording []
        (.stop (@app-state :audio-recorder))
        (swap! app-state assoc :recording false))

(defn- record-frame []
  (let [_ (set! (.-muted (get-screen)) true)
        _ (aset (get-screen) "muted" true)
        _ (println (str "muted: " (aget (get-screen) "muted")))]
    (while (@app-state :recording)
     (do ((.drawImage video-context (get-screen) 0 0 (.-width (get-screen)) (.-height (get-screen)))
          (let [image-data (.getImageData video-context 0 0 (.-width (get-screen))(.-height (get-screen)))
                frame-duration (- (trans/to-long (date/now)) (@app-state :frame-time))]
            (swap! app-state assoc :image-array (conj (app-state :image-array)   {:image image-data :duration frame-duration})))
            (swap! app-state assoc :frame-time (trans/to-long (date/now)) )))))
  (complete-recording))

(defn- success [stream]
  (let [;audio-context (.-AudioContext js/window)
        ;audioctx (audio-context.)
        ;audio (.createMediaStreamSource audioctx stream)
        ;gainctrl (.createGain audioctx)
       ; gain (.-gain gainctrl)
        ]
    (do (js/alert "success")
        ;(.connect audio gainctrl)
        ;(.connect gainctrl (.-destination audioctx))
        ;(set! (-> gainctrl .-gain .-value) 0)
        ;(println (str "VOL:" (aget gainctrl "gain" "value")))
        (aset (get-screen) "src" (.createObjectURL (.-URL js/window) stream))
        (set! (.-muted (get-screen)) true)
        ;(swap! app-state assoc :audio-recorder (new js/Recorder audio #js {:workerPath "js/recorderWorker.js"}))
        (swap! app-state assoc :mediaStrem stream)
        (swap! app-state assoc :frame-time (trans/to-long (date/now)))
        (aset video-canvas "width" (.-width (get-screen)))
        (aset video-canvas "height" (.-height (get-screen)))
        ;(.record (@app-state :audio-recorder))
        (swap! app-state assoc :recording true))))

(defn- error [err] (js/alert (str "error" err)))

(defn- get-umedia []
  #(let [m (.getUserMedia (.-mediaDevices js/navigator) #js {:audio true :video true})
         _ (.then m success)
         _ (.catch m error)]))

(defn player []
  (fn []
    [:div [:input {:type "button"
                   :onClick (get-umedia)
                   :value "Click me!"}]
          [:video  {:autoPlay true :id "video"}]
          [:canvas {:id "canvas"}]]))
