(ns viime.db
  (:require [re-frame.core :as re-frame]))

(def default-db
  {:name "re-frame"
   :user-name nil
   :password nil
   :easyrtcid "Nobody"
   :remote-data {}
   :logged-in-as "Nobody"
   :stream nil
   :states {:recording {:key :recording :btn-text "Stop" :fn  #(re-frame/dispatch [:stop-recording])}
            :playing   {:key :playing :btn-text "Stop" :fn  #(re-frame/dispatch [:stop-rocording])}}
   :current-call nil
   :peerjs-id "UNCONNECTED"
    })
