(ns viime.events
    (:require [re-frame.core :as re-frame]
              [cljs-uuid-utils.core :as uuid]
						  [clojure.set :as st]
              [viime.db :as db]
              [viime.rest :as r]
              [viime.websocket-client :as ws]
              [taoensso.sente  :as sente]
              [clojure.data :as data]
              [easyrtc.js]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :login
 (fn  [db [_ id]]
   (ws/login id (db :websocket-server))
   db))

(re-frame/reg-event-db
 :logged-in
 (fn  [db [_ id]]
  (let [peer (js/Peer. id #js  {"debug" 3
				"host" "fenario.hopto.org"
				"port" 9000
				"secure" "true"
				"config" {"iceServers" [
    					 	{ "url" "stun:fenario.hopto.org:5349" }
    						{ "url" "turn:bobi@fenario.hopto.org:5349"
                                                  "credential" "9Bergen4" } ]}  })
        _ (.on peer "open" #(re-frame/dispatch [:peer-open %]) )
        _ (.on peer "call" #(re-frame/dispatch [:peer-incoming-call %]) ) ]
		 (-> db
         (assoc-in [:peer] peer)
         (assoc :logged-in-as id))) ))


(defn map-values [m kys f & args]
  (reduce #(apply update-in %1 [%2] f args) m kys))
;db-users (map-values (:users db) [:status] (constantly :offline))

(re-frame/reg-event-db
 :update-users
 (fn  [db [_ current-users]]
   (let [_ (prn "CURRENT-USERS-FOR-UPDATE: " current-users)
         db-users (:users db)
         db-users (into {}  (for [[login db-user] db-users] [login (assoc db-user :status :offline)]))
         _ (prn "DBUSERES-ALL-OFFLINE: " db-users)
         db-users (atom db-users)]
     (doseq [[login user-info] current-users]
       (let [_ (prn "Login: " login)]
         (if-let [db-user (seq
                            (filter #(let [_ (prn "DB-USER: " %  "LOGGED-IN-USER: " login)]
                                      (= (:login (second %)) login )) @db-users)) ]
          (let [_ (prn "UPDATING_DB_USER1: " (second (first db-user)))]
            (swap! db-users assoc (keyword (:login (second (first db-user))))
                   (-> (second (first db-user))
                       (assoc :status :online)
                       (assoc :pid (:pid user-info))))))))
     (prn "NEW-DB: " @db-users)
     (assoc db :users @db-users))))

(re-frame/reg-event-db
 :initialize-sente
 (fn  [db [_ id]]
   (let [websocket-server (sente/make-channel-socket-client!  "/chsk" {:host "fenario.hopto.org:443"
							     ;  :protocol "https"
								      })
        ; _ (prn "WEBSOCKEt_SERVER: " websocket-server)
         ]
     (ws/start! websocket-server)
     (assoc-in db [:websocket-server] websocket-server))))

(re-frame/reg-event-db
 :stop-recording
 (fn  [db _]
   (let [data (reduce conj [] (:frames db))
         blob (js/Blob. (clj->js data)  (clj->js { "type" "video/webm" }) )
         stream (:stream db)]
     (do (doseq [track (.getTracks stream)] (.stop track))
         (re-frame/dispatch [:set-state :playing])
         (let [media (.createObjectURL js/URL blob) ]
              ;TODO add account id or something.
              (r/upload blob (uuid/make-random-uuid))
              (assoc-in db [:video] media))))))

;co-effect
(re-frame/reg-event-db
 :initialize-video
 (fn  [db _]
   (let [m (-> (.getUserMedia (.-mediaDevices js/navigator) #js {:audio true :video true})
               (.then  #(re-frame/dispatch [:set-stream %]) )
               (.catch #(js/alert (str "error" %))))
           ]
		db)))

(re-frame/reg-event-db
 :perform-call
 (fn  [db [_ user]]
   (.hangupAll js/easyrtc)
   (.call js/easyrtc user #() #())))

(defn update-users [db remote-users]
  (let [diffs (st/difference (keys remote-users ) (keys (:users db)))
        _ (prn "diffs:  " diffs)
        informed (into {} (for [[k v] diffs] [k (assoc v :notified true)]))
        logged-out (st/difference (keys (:users db)) (keys remote-users))
        users (into {}  (for [[k v] (db :users) :when (contains? logged-out k)] [k v] ))
        ]
    (merge (db :users) informed )))

(re-frame/reg-event-db
 :update-easyrtc-info
 (fn  [db [_ room-name data primary?]]
   (let [remote-users (js->clj data)
         _ (prn "DataUpdate: " remote-users )
         users (update-users db remote-users)]
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
       (assoc :users [])
       (assoc :easyrtcid (.cleanId js/easyrtc easyrtcid))
       )))

(re-frame/reg-event-db
 :login-failure
 (fn  [db [_ error-code message]]
  (prn "LoginFailure:  " error-code  ":" message)
   (.showError js/easyrtc error-code message)))


(re-frame/reg-event-db
 :initialize-easyrtc
 (fn  [db _]
   (let []
     (.setVideoDims js/easyrtc 640 480)
     (.setRoomOccupantListener js/easyrtc #(re-frame/dispatch [:update-easyrtc-info %1 %2 %3]) )
     (.easyApp js/easyrtc "easyrtc.audioVideoSimple"
                          "selfVideo"
                          (clj->js ["callerVideo"])
                          #(re-frame/dispatch [:login-success  %1])
                          #(re-frame/dispatch [:login-error %1 %2])
                         )
    (prn "Initialized EasyRTC")
		db)))

(re-frame/reg-event-db
 :peer-open
 (fn [db [_ id]]
    (let [_ (prn "PEERJS:OPEN:ID:" id)]
     (assoc db :peerjs-id id))))

(re-frame/reg-event-db
 :peer-incoming-call
 (fn [db [_ call]]
    (let [_ (prn "PEERJS:INCOMING:CALL" )
          _ (.on call "stream" #(re-frame/dispatch [:peer-remote-stream-connect %]))
          _ (.answer call (:stream db))]
     (assoc db :peer-call call) )))

(re-frame/reg-event-db
 :initiate-call
 (fn [db [_ remote-peer-id]]
    (let [_ (prn "PEERJS:PLACING:CALL " )
          peer (:peer db)
          _ (prn "PEERJS:PLACING:CALL 1" )
          stream (:stream db)
          _ (prn "PEERJS:PLACING:CALL 2" )
          call (.call peer remote-peer-id stream )
          _ (prn "PEERJS:PLACING:CALL 3" )
          _ (.on call "stream" #(let [ _ (prn "STREAM:IS:" %)] (re-frame/dispatch [:peer-remote-stream-connect %])))
          _ (prn "PEERJS:PLACING:CALL 4" )

          ]
     (assoc db :peer-call call) )))

(re-frame/reg-event-db
 :peer-remote-stream-connect
 (fn [db [_ remote-stream]]
    (let [_ (prn "PEERJS:INCOMING:REMOTE-STREAM" )
          screen (.getElementById js/document "video") ]
      (-> (assoc-in db [:video] (.createObjectURL js/URL remote-stream))
          (assoc :remote-stream remote-stream)))))

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
   (let [screen (.getElementById js/document "video") ]
    (assoc db :stream stream))))


(re-frame/reg-event-db
 :set-state
 (fn [db [_ state]]
   (let [s ((db :states) state)]
     (assoc-in db [:state] s))))

