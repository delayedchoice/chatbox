(ns viime.events
    (:require [re-frame.core :as re-frame]
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
 :recording-stopped
 (fn [db [_ frame]]
   (let  [data (reduce conj [] (:frames db))
          _ (println (str (count data) " reduced") )
          blob (js/Blob. (clj->js data)  (clj->js { "type" "video/webm" }) )]
          (assoc db :video (.createObjectURL js/URL blob)))))


(re-frame/reg-event-db
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))
