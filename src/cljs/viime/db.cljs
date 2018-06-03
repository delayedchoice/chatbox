(ns viime.db
  (:require [re-frame.core :as re-frame]))

(def default-db
  {:name "re-frame"
   :user-name nil
   :password nil
   :easyrtcid "Nobody"
   :remote-data {}
   :logged-in-as "Nobody"
   :users {:aran    {:id 1 :login "aran" :whole-name "Aran Jones" :status :offline}
           :bobi    {:id 2 :login "bobi" :whole-name "Bobby Harris" :status :offline}
           :mallory {:id 3 :login "mallory" :whole-name "Mallory Harris" :status :offline}
           :charlie {:id 4 :login "charlie" :whole-name "Charlie Harris" :status :offline}}
   :stream nil
   :states {:recording {:key :recording :btn-text "Stop" :fn  #(re-frame/dispatch [:stop-recording])}
            :playing   {:key :playing :btn-text "Stop" :fn  #(re-frame/dispatch [:stop-rocording])}}
   :current-call nil
   :peerjs-id "UNCONNECTED"
    })
