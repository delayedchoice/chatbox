(ns viime.websocket-handler
  (:require
   [clojure.string     :as str]
   [ring.middleware.defaults]
   [compojure.core     :as comp :refer (make-route routes defroutes GET POST)]
   [compojure.route    :as route]
   [hiccup.core        :as hiccup]
   [clojure.core.async :as async  :refer (<! <!! >! >!! put! chan go go-loop)]
   [taoensso.encore    :as encore :refer (have have?)]
   [taoensso.sente     :as sente]
   [taoensso.timbre    :as timbre :refer (tracef debugf infof warnf errorf)]
   [org.httpkit.server :as http-kit]
   [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]))

;; (timbre/set-level! :trace) ; Uncomment for more logging
(reset! sente/debug-mode?_ true) ; Uncomment for extra debug info

(defn landing-pg-handler [ring-req]
  (hiccup/html
    [:h1 "Sente reference example"]
    [:p "An Ajax/WebSocket" [:strong " (random choice!)"] " has been configured for this example"]
    [:hr]
    [:p [:strong "Step 1: "] " try hitting the buttons:"]
    [:p
     [:button#btn1 {:type "button"} "chsk-send! (w/o reply)"]
     [:button#btn2 {:type "button"} "chsk-send! (with reply)"]]
    [:p
     [:button#btn3 {:type "button"} "Test rapid server>user async pushes"]
     [:button#btn4 {:type "button"} "Toggle server>user async broadcast push loop"]]
    [:p
     [:button#btn5 {:type "button"} "Disconnect"]
     [:button#btn6 {:type "button"} "Reconnect"]]
    ;;
    [:p [:strong "Step 2: "] " observe std-out (for server output) and below (for client output):"]
    [:textarea#output {:style "width: 100%; height: 200px;"}]
    ;;
    [:hr]
    [:h2 "Step 3: try login with a user-id"]
    [:p  "The server can use this id to send events to *you* specifically."]
    [:p
     [:input#input-login {:type :text :placeholder "User-id"}]
     [:button#btn-login {:type "button"} "Secure login!"]]
    ;;
    [:hr]
    [:h2 "Step 4: want to re-randomize Ajax/WebSocket connection type?"]
    [:p "Hit your browser's reload/refresh button"]
    [:script {:src "js/compiled/app.js"}] ; Include our cljs target
    ))

(defn login-handler
  "Here's where you'll add your server-side login/auth procedure (Friend, etc.).
  In our simplified example we'll just always successfully authenticate the user
  with whatever user-id they provided in the auth request."
  [ring-req]
  (let [{:keys [session params]} ring-req
        {:keys [user-id]} params]
    (debugf "Login request: %s" params)
    {:status 200 :session (assoc session :uid user-id)}))

(defn create-ring-routes! [{:keys [ajax-post-fn ajax-get-or-ws-handshake-fn]}]
  (let [ws-routes [{:method :get  :src "/"   :handler landing-pg-handler}
                	 {:method :get  :src "/chsk"  :handler ajax-get-or-ws-handshake-fn}
                	 {:method :get  :src "/chsk"  :handler ajax-post-fn}
                	 {:method :get  :src "/login" :handler login-handler}]
      ring-routes (apply routes
         						(map #(make-route (:method %) (:src %) (:handler %)) ws-routes))]
  ring-routes))


(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id ; Dispatch on event-id
  )

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg) ; Handle event-msgs on a single thread
  ;; (future (-event-msg-handler ev-msg)) ; Handle event-msgs on a thread pool
  )

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (debugf "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-from-server event}))))

(defn  stop-router! [router] (when-let [stop-fn router] (stop-fn)))
(defn start-router [{:keys [ch-recv]}]
  (sente/start-server-chsk-router!  ch-recv event-msg-handler))

(defn main-ring-handler [ring-routes]
  "**NB**: Sente requires the Ring `wrap-params` + `wrap-keyword-params`
  middleware to work. These are included with
  `ring.middleware.defaults/wrap-defaults` - but you'll need to ensure
  that they're included yourself if you're not using `wrap-defaults`."
  (ring.middleware.defaults/wrap-defaults
      ring-routes ring.middleware.defaults/site-defaults) )

(defn stop-web-server! [webserver] (when-let [stop-fn webserver] (stop-fn)))
(defn start-web-server! [ws port]
  (let [port (or port 0) ; 0 => Choose any available port
        ring-routes (create-ring-routes! ws)
        ;_ (def ring-routes ring-routes-created)
        _ (prn "STARTING WEB SERVER")
;        to-be-var (ring.middleware.defaults/wrap-defaults
;                   ring-routes
;                   ring.middleware.defaults/site-defaults)
;        ring-handler (ring.middleware.defaults/wrap-defaults
;                      ring-routes
;                      ring.middleware.defaults/site-defaults)
        ring-handler (main-ring-handler ring-routes)
        stop-fn (http-kit/run-server ring-handler {:port port})
        port    (:local-port (meta stop-fn))
        stop-fn (fn [] (let [_ (prn "STOPPING WEB SERVER")] (stop-fn)))
        uri (format "http://localhost:%s/" port)]
    (infof "Web server is running at `%s`" uri)
    stop-fn))
