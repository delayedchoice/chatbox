(ns viime.events
    (:require [re-frame.core :as re-frame]
              [cljs-uuid-utils.core :as uuid]
              [viime.db :as db]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :stop-recording
 (fn  [db _]
   (let [data (reduce conj [] (:frames db))
         blob (js/Blob. (clj->js data)  (clj->js { "type" "video/webm" }) )
         stream (:stream db)]
     (do (doseq [track (.getTracks stream)] (.stop track))
         (re-frame/dispatch [:set-state :playing])
         (assoc-in db [:video] (.createObjectURL js/URL blob))))))

;co-effect
(re-frame/reg-event-db
 :initialize-video
 (fn  [db _]
   (let [m (-> (.getUserMedia (.-mediaDevices js/navigator) #js {:audio true :video true})
               (.then  #(re-frame/dispatch [:set-stream %]) )
               (.catch #(js/alert (str "error" %))))])
  db))
;co-effect
(re-frame/reg-event-db
 :start-recording
 (fn  [db _ ]
   (let [screen (.getElementById js/document "video")
         stream (:stream db)
         recorder (js/MediaRecorder. stream)
         _ (aset recorder "ondataavailable" #(let [_ (println (str "add-frame"))] (re-frame/dispatch [:add-frame (.-data %)]) ))
         _ (.start recorder 30)
         _ (set! (.-muted screen) true)]
    (re-frame/dispatch [:set-state :recording])
    (assoc-in db [:stream] stream))))

(re-frame/reg-event-db
  :add-frame
  (fn [db [_ frame]]
    (update db :frames conj frame)))

(re-frame/reg-event-db
 :add-message
 (fn [db [_ message]]
   (update-in db :message conj (assoc message :id (uuid/make-random-uuid)))))

;co-effect
(re-frame/reg-event-db
 :set-stream
 (fn [db [_ stream]]
   (let [screen (.getElementById js/document "video")
         _ (aset screen "srcObject" stream)
         _ (set! (.-muted screen) true)]
    (assoc db :stream stream))))


(re-frame/reg-event-db
 :set-state
 (fn [db [_ state]]
   (let [s ((db :states) state)]
     (assoc-in db [:state] s))))

