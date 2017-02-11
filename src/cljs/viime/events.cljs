(ns viime.events
    (:require [re-frame.core :as re-frame]
              [cljs-uuid-utils.core :as uuid]
              [viime.db :as db]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
  :add-frame
  (fn [db [_ frame]]
    (update db :frames conj frame)))

(re-frame/reg-event-db
 :add-message
 (fn [db [_ message]]
   (update-in db :message conj (assoc message :id (uuid/make-random-uuid)))))

(re-frame/reg-event-db
 :set-state
 (fn [db [_ state]]
   (assoc db :state state)))

(re-frame/reg-event-db
 :recording-stopped
 (fn [db [_ frame]]
   (let  [data (reduce conj [] (:frames db))
          _ (println (str (count data) " reduced") )
          blob (js/Blob. (clj->js data)  (clj->js { "type" "video/webm" }) )
          stream (:stream db)]
    (re-frame/dispatch [:set-state :playing])
    (assoc db :video (.createObjectURL js/URL blob)))))


;(re-frame/reg-event-db
; :stream-created
; (fn [db [_ stream]]
;   (assoc db :stream stream)))

(re-frame/reg-event-db
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))
