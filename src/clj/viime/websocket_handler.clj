(ns viime.websocket-handler
  (:require
   [clojure.string     :as str]
   [ring.middleware.defaults]
   [compojure.core     :as comp :refer (make-route routes defroutes GET POST)]
   [compojure.route    :as route]
;   [clojure.spec.alpha :as s]
   [hiccup.core        :as hiccup]
   [clojure.core.async :as async  :refer (<! <!! >! >!! put! chan go go-loop)]
   [taoensso.encore    :as encore :refer (have have?)]
   [taoensso.sente     :as sente]
   [taoensso.timbre    :as timbre :refer (tracef debugf infof warnf errorf)]
   [org.httpkit.server :as http-kit]
   [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]))

(timbre/set-level! :trace) ; Uncomment for more logging
(reset! sente/debug-mode?_ false) ; Uncomment for extra debug info

(defn landing-pg-handler [ring-req]
  (hiccup/html
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:link {:rel "stylesheet" :href "css/bootstrap.min.css"}]
     [:script {:src "js/jquery-3.1.1.min.js" }]
     [:script {:src "js/bootstrap.min.js"}]
     [:link {:href "css/site.css" :rel "stylesheet" :type "text/css"}]]
    [:body
     [:div {:id "app" :class "container-fluid"}]
     [:script {:src "js/compiled/app.js"}]
     [:script "viime.core.init();" ]] ))

(defn login-handler [logged-in-users]
  "Here's where you'll add your server-side login/auth procedure (Friend, etc.).
  In our simplified example we'll just always successfully authenticate the user
  with whatever user-id they provided in the auth request."
  (fn [ring-req]
   (let [{:keys [session params]} ring-req
         {:keys [user-id pid]} params]
     (prn "Login request: %s" params)
     (prn "Session: %s" session)
     (swap! logged-in-users assoc user-id {:uid user-id :pid pid :status :online})
     {:status 200 :session (assoc session :uid user-id)})))

(defn test-handler [x] "TEST NEW HANDLER SUCCESS")

(defn update-remote-users-lists [users send-fn]
  (let [_ (prn "Updating Remote USERS " users)]
    (doseq [user users]
     (let [_ (prn "UPDDATING REMOTE USER: " (first user))]
       (send-fn (first user) [:users/current users])))))

(defn create-ring-routes! [{:keys [ajax-post-fn ajax-get-or-ws-handshake-fn]} logged-in-users]
  (let [ws-routes [{:method :get  :src "/landing" :handler landing-pg-handler}
                	 {:method :get  :src "/chsk"    :handler ajax-get-or-ws-handshake-fn}
                	 {:method :post :src "/chsk"    :handler ajax-post-fn}
                   {:method :get  :src "/test"    :handler test-handler}
                   {:method :post :src "/login"   :handler (login-handler logged-in-users)}]
      ring-routes (apply routes
         						(map #(make-route (:method %) (:src %) (:handler %)) ws-routes))]
  (ring.middleware.defaults/wrap-defaults ring-routes ring.middleware.defaults/site-defaults)))

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
;    (debugf "Unhandled event: %s" event)
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
  (let [port (or port 0)
        ring-routes (create-ring-routes! ws)
        _ (prn "STARTING WEB SERVER")
        ;ring-handler (main-ring-handler ring-routes)
        stop-fn (http-kit/run-server ring-routes {:port port})
        port    (:local-port (meta stop-fn))
        stop-fn (fn [] (let [_ (prn "STOPPING WEB SERVER")] (stop-fn)))
        uri (format "http://localhost:%s/" port)]
    (infof "Web server is running at `%s`" uri)
    stop-fn))
