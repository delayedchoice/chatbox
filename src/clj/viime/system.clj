(ns viime.system
  (:require [viime.db :as db]
            [viime.websocket-handler :as ws]
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
  (h2 {:db "mem:db"
       :user "sa"
       :password "" }))

(defn system []
  {})


(defn start [system]
  (let [_ (prn "GOING TO MAKE SERVER")
        websocket-server (sente/make-channel-socket-server! (get-sch-adapter) )
        _ (prn "MADE SERVER")
        webserver (ws/start-web-server! websocket-server 3450)
        router (ws/start-router websocket-server)
        ds (db/new-db in-mem-data-store)
        _  (db/start ds)]
    (-> system
     (assoc :websocket-server websocket-server)
     (assoc :webserver webserver)
     (assoc :router router)
     (assoc :db ds)
     (assoc :cache (atom (cache/ttl-cache-factory {} :ttl (* 5 60 1000))))
         )))

(defn stop [system]
       (ws/stop-router! (:router system))
       (ws/stop-web-server! (:webserver system) )
       (db/close-db (:db system))
       {})
