(ns viime.system
  (:require [viime.db :as db]
            [viime.websocket-handler :as ws]
            [viime.handler :as base-handler]
            [taoensso.sente     :as sente #_(make-channel-socket-server!) ]
            [clojure.core.cache :as cache]
            [korma.db :refer (h2)]
            [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
            [taoensso.timbre    :as timbre :refer (tracef debugf infof warnf errorf)]
            [taoensso.sente     :as sente]
            [taoensso.encore    :as encore :refer (have have?)]
            [org.httpkit.server :as http-kit]
            ))

(timbre/set-level! :trace) ; Uncomment for more logging
(reset! sente/debug-mode?_ true) ;

(def in-mem-data-store
  (h2 {:db "mem:db;DB_CLOSE_ON_EXIT=FALSE"
       :user "sa"
       :password "" }))

(defn system []
  {})

(defn start [system]
  (let [_ (prn "GOING TO MAKE SERVER")
        websocket-server (sente/make-channel-socket-server! (get-sch-adapter) )
        _ (prn "MADE SERVER")
        ws-ring-routes (ws/create-ring-routes! websocket-server)
        ;webserver (ws/start-web-server! websocket-server 3450)
        _ (base-handler/reset-routes ws-ring-routes)
        webserver (http-kit/run-server #'base-handler/synthetic-routes {:port 3000})
        router (ws/start-router websocket-server)
        ds (db/new-db in-mem-data-store)
        _  (db/start ds)]
    (add-watch (:connected-uids websocket-server) :connected-uids
      (fn [_ _ old new]
        (when (not= old new)
          (prn  "Connected uids change old: \n" old  " new:\n"  new))))
    (-> system
     (assoc :websocket-server websocket-server)
     (assoc :webserver webserver)
     (assoc :router router)
     (assoc :db ds)
     (assoc :connected-uids (:connected-uids websocket-server))
     (assoc :user-cache (atom (cache/ttl-cache-factory {} :ttl (* 5 60 1000))))
         )))
(defn stop [system]
       (ws/stop-router! (:router system))
       (ws/stop-web-server! (:webserver system) )
       (db/close-db (:db system))
       {})
