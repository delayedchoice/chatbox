(ns viime.events
    (:require [re-frame.core :as re-frame]
              [viime.db :as db]
              [viime.modal :as modal]
              [easyrtc.js]))

(re-frame/reg-event-db
 :modal
 (fn [db [_ data]]
   (assoc-in db [:modal] data)))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :login
 (fn  [db [_ user-name password]]
  (-> db
      (assoc :user-name user-name)
      (assoc :password password)
      )))

(re-frame/reg-event-db
 :do-login
 (fn  [db [_ _]]
   (prn "DO LOGIN")
  (re-frame/dispatch [:initialize-easyrtc (db :user-name)])
  (assoc db :show-loader true)))


(re-frame/reg-event-db
 :perform-call
 (fn  [db [_ user]]
   (let [_ (prn "PerformCall: ")]
    (.hangupAll js/easyrtc)
    (.call js/easyrtc user #(re-frame/dispatch [:easyrtc-call-success %1])
                           #(re-frame/dispatch [:easyrtc-connect-failure %1]) ))))

(re-frame/reg-event-db
 :update-easyrtc-info
 (fn  [db [_ room-name data primary?]]
   (let [other-client-div (.getElementById js/document "otherClients")
         remote-users (js->clj data)
         _ (prn "DataUpdate: " remote-users )
         users remote-users]
     (-> db
        (assoc :users users)
        (assoc :remote-data remote-users)
        (assoc :room-name room-name)
        (assoc :primary? primary?)))))

(re-frame/reg-event-db
 :login-success
 (fn  [db [_ easyrtcid]]
   (prn "LoginSuccess: " easyrtcid)
   (-> db
       (assoc :show-loader false)
       (assoc :users {})
       (assoc :easyrtcid (.cleanId js/easyrtc easyrtcid))
       )))

(re-frame/reg-event-db
 :easyrtc-call-success
 (fn  [db [_ easyrtcid]]
   (prn "CallSuccess: " easyrtcid)
  (assoc db :show-loader false) ))

(re-frame/reg-event-db
 :easyrtc-connect-success
 (fn  [db [_ easyrtcid]]
   (prn "ConnectSuccess: " easyrtcid)
   (-> db
        (assoc :show-loader false)
        (assoc :users {})
        (assoc :easyrtcid (.cleanId js/easyrtc easyrtcid))
       )))

(re-frame/reg-event-db
 :easyrtc-connect-failure
 (fn  [db [args]]
   (prn "ConnectFailure: " args)
   (-> db
      (assoc :show-loader false)
      (assoc :users {})
      (assoc :users {})
       )))

(re-frame/reg-event-db
 :login-failure
 (fn  [db [_ error-code message]]
  (prn "LoginFailure:  " error-code  ":" message)
   (.showError js/easyrtc error-code message)
   (assoc db :show-loader false)))

(re-frame/reg-event-db
 :easyrtc-accept-stream
 (fn  [db [_ caller-easyrtc-id stream]]
  (re-frame/dispatch [:easyrtc-accept-stream2 caller-easyrtc-id stream ])
  (re-frame/dispatch [:modal {:show? true
                              :child [modal/videos]
                              :size :small}])  
  (assoc db  :current-call caller-easyrtc-id)
  ))

(re-frame/reg-event-db
 :set-loader-visible
 (fn  [db [_ show-loader?]]
  (assoc db :show-loader show-loader?)
  ))

(re-frame/reg-event-db
 :easyrtc-accept-stream2
 (fn  [db [_ caller-easyrtc-id stream]]
  (let [self-video (.getElementById js/document "self")
        caller-video (.getElementById js/document "caller") ]
(prn "TESTING ACCEPT STREAM2")
    (.setVideoObjectSrc js/easyrtc self-video (.getLocalStream js/easyrtc))
    (.setVideoObjectSrc js/easyrtc caller-video stream) )
  db))

(re-frame/reg-event-db
 :easyrtc-stream-closed
 (fn  [db [_ caller-easyrtc-id]]
  (let [video (.getElementById js/document "caller") ]
    (.setVideoObjectSrc js/easyrtc video "") )
  db))

(re-frame/reg-event-db
 :easyrtc-registrtation-success
 (fn  [db [_]]
  (let [self-video (.getElementById js/document "self")
        _ (prn "Connecting: ") ]
    (.connect js/easyrtc "default"
                        #(re-frame/dispatch [:easyrtc-connect-success %1])
                        #(re-frame/dispatch [:easyrtc-connect-failure %1])) )
  db))

(re-frame/reg-event-db
 :initialize-easyrtc
 (fn  [db [_ user]]
   (let [_ (prn "cred: " (get-in user [:auth-result :accessToken]))]
     (.setUsername js/easyrtc (get-in user [:profile :email]))
     (.setCredential js/easyrtc (clj->js {:token (get-in user [:auth-result :accessToken])}))
     (.setStreamAcceptor js/easyrtc #(re-frame/dispatch [:easyrtc-accept-stream %1 %2]))
     (.setOnStreamClosed js/easyrtc #(re-frame/dispatch [:easyrtc-stream-closed %1]))
     (.setVideoDims js/easyrtc 640 480)
     (.setRoomOccupantListener js/easyrtc #(re-frame/dispatch [:update-easyrtc-info %1 %2 %3]) )
     (if ^boolean js/goog.DEBUG 
       (.connect js/easyrtc "default"
                 #(re-frame/dispatch [:easyrtc-connect-success %1])
                 #(re-frame/dispatch [:easyrtc-connect-failure %1]))
       (.initMediaSource js/easyrtc #(re-frame/dispatch [:easyrtc-registrtation-success %1 %2 %3]) 
                         #(re-frame/dispatch [:easyrtc-connect-failure])) )
    (prn "initialized easyrtc")
		db)))
