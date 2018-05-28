(ns viime.events
    (:require [re-frame.core :as re-frame]
              [viime.db :as db]
              [easyrtc.js]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :login
 (fn  [db [_ user-name password]]
  (-> db
      (assoc :user-name user-name)
      (assoc :passwork password)
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
   (.hangupAll js/easyrtc)
   (.call js/easyrtc user #(re-frame/dispatch [:easyrtc-call-success %1])
                          #(re-frame/dispatch [:easyrtc-connect-failure]) )))

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
 (fn  [db [_]]
   (prn "ConnectFailure: ")
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
  (assoc db :current-call caller-easyrtc-id)
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
  (let [self-video (.getElementById js/document "self") ]
    (.connect js/easyrtc "default"
                        #(re-frame/dispatch [:easyrtc-connect-success %1])
                        #(re-frame/dispatch [:easyrtc-connect-failure])) )
  db))

(re-frame/reg-event-db
 :initialize-easyrtc
 (fn  [db [_ id]]
   (let []
     (.setUsername js/easyrtc id)
     (.setStreamAcceptor js/easyrtc #(re-frame/dispatch [:easyrtc-accept-stream %1 %2]))
     (.setOnStreamClosed js/easyrtc #(re-frame/dispatch [:easyrtc-stream-closed %1]))
     (.setVideoDims js/easyrtc 640 480)
     (.setRoomOccupantListener js/easyrtc #(re-frame/dispatch [:update-easyrtc-info %1 %2 %3]) )
     (.initMediaSource js/easyrtc #(re-frame/dispatch [:easyrtc-registrtation-success %1 %2 %3]) 
                                  #(re-frame/dispatch [:easyrtc-connect-failure]))
    (prn "Initialized EasyRTC")
		db)))
