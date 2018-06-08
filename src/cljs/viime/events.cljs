(ns viime.events
    (:require [re-frame.core :as re-frame]
              [viime.db :as db]
              [reagent-modals.modals :as reagent-modals]
              [reagent.core :as reagent]
              [viime.modal :as modal]
	          [easyrtc.js]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))


(re-frame/reg-event-db
 :perform-call
 (fn  [db [_ user]]
   (let [_ (prn "Performing Call: " user)]
    (.hangupAll js/easyrtc)
    (.call js/easyrtc user #(re-frame/dispatch [:easyrtc-call-success %1])
                           #(re-frame/dispatch [:easyrtc-connect-failure %1]) ))))

(re-frame/reg-event-db
 :update-easyrtc-info
 (fn  [db [_ room-name data primary?]]
   (let [other-client-div (.getElementById js/document "otherClients")
         remote-users (js->clj data)
         _ (prn "Data Update: " remote-users )
         users remote-users]
     (-> db
        (assoc :users users)
        (assoc :remote-data remote-users)
        (assoc :room-name room-name)
        (assoc :primary? primary?)))))

(re-frame/reg-event-db
 :easyrtc-call-success
 (fn  [db [_ easyrtcid]]
   (prn "Call Success: " easyrtcid)
   db))

(re-frame/reg-event-db
 :easyrtc-connect-success
 (fn  [db [_ easyrtcid]]
   (prn "Connect Success: " easyrtcid)
   (-> db
        (assoc :users {})
        (assoc :easyrtcid (.cleanId js/easyrtc easyrtcid))
       )))

(re-frame/reg-event-db
 :easyrtc-connect-failure
 (fn  [db [args]]
   (prn "Connect Failure: " args)
   (-> db
      (assoc :users {})
      (assoc :users {})
       )))

(re-frame/reg-event-db
 :easyrtc-accept-stream
 (fn  [db [_ caller-easyrtc-id stream]]
  (prn "Accept Stream1")
  (reagent-modals/modal! (modal/create-modal)   {:size :lg :hidden #(re-frame/dispatch [:hangup]) :shown #(re-frame/dispatch [:easyrtc-accept-stream2])}) 
  (-> 
    (assoc db  :current-call caller-easyrtc-id)
    (assoc :stream stream))))

(re-frame/reg-event-db
 :easyrtc-accept-stream2
 (fn  [db [args]]
  (let [self-video (.getElementById js/document "self")
        caller-video (.getElementById js/document "caller")
       _ (prn "stuff:" self-video ":" caller-video ":" db) ]
    (prn "Accept Stream2")
    (.setVideoObjectSrc js/easyrtc self-video (.getLocalStream js/easyrtc))
    (.setVideoObjectSrc js/easyrtc caller-video (db :stream))
    (let [
          w (.-clientWidth caller-video)
          h (.-clientHeight caller-video)
_ (prn "Dim: w:" w "h:" h )]
     (set! (.-top (.-style self-video)) (str (- h 100) "px")) 
     (set! (.-left (.-style self-video))(str (- w 150) "px") )) )
    
  db))

(re-frame/reg-event-db
 :easyrtc-stream-closed
 (fn  [db [_ caller-easyrtc-id]]
  (reagent-modals/close-modal! )
  (prn "Stream Closed -remote")
  db))

(re-frame/reg-event-db
 :hangup
 (fn  [db [_ caller-easyrtc-id]]
 (prn "Window Closed - hangup")
 (.hangupAll js/easyrtc caller-video (db :stream) ) 
 db)) 

(re-frame/reg-event-db
 :easyrtc-registrtation-success
 (fn  [db [_]]
    (prn "Registered with EasyRTC") 
    (.connect js/easyrtc "default" #(re-frame/dispatch [:easyrtc-connect-success %1])
                        ``         #(re-frame/dispatch [:easyrtc-connect-failure %1])) 
  db))

(re-frame/reg-event-db
 :initialize-easyrtc
 (fn  [db [_ user]]
   (let [_ (prn "cred: " (get-in user [:auth-result :accessToken]))]
     (prn "Initializing EasyRTC")
     ;(if ^boolean (not js/goog.DEBUG) 
       (do (.setUsername js/easyrtc (get-in user [:profile :email]))
           (.setCredential js/easyrtc (clj->js {:token (get-in user [:auth-result :accessToken])}))
           (.setStreamAcceptor js/easyrtc #(re-frame/dispatch [:easyrtc-accept-stream %1 %2]))
           (.setOnStreamClosed js/easyrtc #(re-frame/dispatch [:easyrtc-stream-closed %1]))
           (.setVideoDims js/easyrtc 640 480)
           (.setRoomOccupantListener js/easyrtc #(re-frame/dispatch [:update-easyrtc-info %1 %2 %3]) )
           (.initMediaSource js/easyrtc #(re-frame/dispatch [:easyrtc-registrtation-success %1 %2 %3]) 
                          #(re-frame/dispatch [:easyrtc-connect-failure])))
    ;(reagent-modals/modal! (modal/create-modal)   {:size :lg  :shown #(re-frame/dispatch [:easyrtc-accept-stream2])}) )
	db)))
