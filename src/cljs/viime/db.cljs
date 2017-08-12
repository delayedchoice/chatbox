(ns viime.db
  (:require [re-frame.core :as re-frame]))

(def start-state {:key :recording :btn-text "Record" :name :recording :fn  #(re-frame/dispatch [:start-recording])} )
(def default-db
  {:name "re-frame"
   :easyrtcid "Nobody"
   :remote-data []
   :logged-in-as "Nobody"
   :users {:aran    {:id 1 :login "aran" :whole-name "Aran Jones" :status :offline}
           :bobi    {:id 2 :login "bobi" :whole-name "Bobby Harris" :status :offline}
           :mallory {:id 3 :login "mallory" :whole-name "Mallory Harris" :status :offline}
           :charlie {:id 4 :login "charlie" :whole-name "Charlie Harris" :status :offline}}
   :stream nil
   :states {:recording {:key :recording :btn-text "Stop" :fn  #(re-frame/dispatch [:stop-recording])}
            :playing   {:key :playing :btn-text "Stop" :fn  #(re-frame/dispatch [:stop-rocording])}}
   :frames []
   :video  nil
   :styles {:read "read message"
            :unread "unread message"}
   :state start-state
   :boxes [{:id 12}]
   :peerjs-id "UNCONNECTED"
   :contacts [{:nm "Mallory@Viime.com"
               ;:subject "Never"
               :id 3
               :status :unread
               ;:timedate "TODAY"
               }
              {:nm "Charlie@Viime.com"
               ;:subject "OK"
               :id 2
               :status :unread
               ;:timedate "TODAY"
               }
              {:nm "Bobby@Viime.com"
               ;:subject "WELCOME"
               :id 1
               :status :read
               ;:timedate "YESTERDAY"
               }] })
