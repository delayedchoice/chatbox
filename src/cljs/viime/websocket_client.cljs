(ns viime.websocket-client
  (:require
   [clojure.string  :as str]
   [cljs.core.async :as async  :refer (<! >! put! chan)]
   [taoensso.encore :as encore :refer-macros (have have?)]
   [taoensso.timbre :as timbre :refer-macros (tracef debugf infof warnf errorf)]
   [taoensso.sente  :as sente  :refer (cb-success?)]
   [re-frame.core :as rf] )


  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)]))

(enable-console-print!)

(defn ->output! [fmt & args]
  (let [msg (apply encore/format fmt args)]
    (prn msg)))

(->output! "ClojureScript appears to have loaded correctly.")

;(def ws-connection  (sente/make-channel-socket-client!  "/chsk" {:host "fenario.hopto.org:443"
;							;	 :protocol "https"
;							        }))
;(def chsk       (ws-connection :chsk))
;(def ch-chsk    (ws-connection :ch-recv)) ; ChannelSocket's receive channel
;(def chsk-send! (ws-connection :send-fn)) ; ChannelSocket's send API fn
;(def chsk-state (ws-connection :state))   ; Watchable, read-only atom

(defmulti -event-msg-handler :id)

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event]}]
  (->output! "Unhandled event: %s" event))

(defmethod -event-msg-handler :chsk/state
  [{:as ev-msg :keys [?data]}]
  (let [[old-state-map new-state-map] (have vector? ?data)]
    (if (:first-open? new-state-map)
      (->output! "Channel socket successfully established!: %s" new-state-map)
      (->output! "Channel socket state change: %s"              new-state-map))))

(defmethod -event-msg-handler :chsk/recv
  [{:as ev-msg :keys [?data]}]
  (->output! "Push event from server: %s" ?data)
  (let [_ (prn "Push current users event from server: %s" ?data)]
   (rf/dispatch [:update-users (second ?data)])
    ))

(defmethod -event-msg-handler :users/current
  [{:as ev-msg :keys [?data]}]
  (let [_ (prn "Push current users event from server: %s" ?data)]
   (rf/dispatch [:update-users (second ?data)])
   (prn "DISPATCHED") ))

(defmethod -event-msg-handler :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  (let [[?uid ?csrf-token ?handshake-data] ?data]
    (->output! "Handshake: %s" ?data)))

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg))

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! [ws-connection]
  (stop-router!)
  (reset! router_
    (sente/start-client-chsk-router!
      (ws-connection :ch-recv) event-msg-handler)))

(defn login [user-id ws-connection]
  (let [pid (rf/subscribe [:peerjs-id])
        _ (prn "CHANNEL STATE: " @(ws-connection :state))] (if (str/blank? user-id)
     (js/alert "Please enter a user-id first")
     (do
       (->output! "Logging in with user-id %s" user-id)
       ;     (chsk-send! [:viime/login user-id])
       (sente/ajax-lite "/login" #_"http://localhost:3450/login"
                        {:method :post
                         :headers {:X-CSRF-Token (:csrf-token @(ws-connection :state))}
                         :params  {:pid @pid :user-id (str user-id)}}

                        (fn [ajax-resp]
                          (->output! "Ajax login response: %s" ajax-resp)
                          (let [login-successful? true ; Your logic here
                                ]
                            (if-not login-successful?
                              (->output! "Login failed")
                              (do
                                (->output! "Login successful")
                                (rf/dispatch [:logged-in(str user-id)])
                                (sente/chsk-reconnect! (ws-connection :chsk)))))))
       ))))

(when-let [target-el (.getElementById js/document "btn1")]
  (.addEventListener target-el "click"
    (fn [ev]
      (->output! "Button 1 was clicked (won't receive any reply from server)")
      (chsk-send! [:example/button1 {:had-a-callback? "nope"}]))))

(when-let [target-el (.getElementById js/document "btn2")]
  (.addEventListener target-el "click"
    (fn [ev]
      (->output! "Button 2 was clicked (will receive reply from server)")
      (chsk-send! [:example/button2 {:had-a-callback? "indeed"}] 5000
        (fn [cb-reply] (->output! "Callback reply: %s" cb-reply))))))

(when-let [target-el (.getElementById js/document "btn3")]
  (.addEventListener target-el "click"
    (fn [ev]
      (->output! "Button 3 was clicked (will ask server to test rapid async push)")
      (chsk-send! [:example/test-rapid-push]))))

(when-let [target-el (.getElementById js/document "btn4")]
  (.addEventListener target-el "click"
    (fn [ev]
      (->output! "Button 4 was clicked (will toggle async broadcast loop)")
      (chsk-send! [:example/toggle-broadcast] 5000
        (fn [cb-reply]
          (when (cb-success? cb-reply)
            (let [loop-enabled? cb-reply]
              (if loop-enabled?
                (->output! "Async broadcast loop now enabled")
                (->output! "Async broadcast loop now disabled")))))))))

(when-let [target-el (.getElementById js/document "btn5")]
  (.addEventListener target-el "click"
                     (fn [ev]
                       (->output! "Disconnecting")
                       (sente/chsk-disconnect! chsk))))

(when-let [target-el (.getElementById js/document "btn6")]
  (.addEventListener target-el "click"
                     (fn [ev]
                       (->output! "Reconnecting")
                       (sente/chsk-reconnect! chsk))))
;;;; Init stuff

(defn start! [ws-connection] (start-router! ws-connection))

;(defonce _start-once (start! ))
