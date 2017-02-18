(ns viime.db
  (:require [re-frame.core :as re-frame]))

(def start-state {:key :recording :btn-text "Record" :name :recording :fn  #(re-frame/dispatch [:start-recording])} )
(def default-db
  {:name "re-frame"
   :stream nil
   :states {:recording {:key :recording :btn-text "Stop" :fn  #(re-frame/dispatch [:stop-recording])}
            :playing   {:key :playing :btn-text "Stop" :fn  #(re-frame/dispatch [:stop-rocording])}}
   :frames []
   :video  nil
   :styles {:read "read message"
            :unread "unread message"}
   :state start-state
   :boxes [{:id 12}]
   :messages [{:from "Mallory@Viime.com"
               :subject "Never"
               :id 3
               :status :unread
               :timedate "TODAY" }
              {:from "Charlie@Viime.com"
               :subject "OK"
               :id 2
               :status :unread
               :timedate "TODAY" }
              {:from "Bobby@Viime.com"
               :subject "WELCOME"
               :id 1
               :status :read
               :timedate "YESTERDAY" }] })
