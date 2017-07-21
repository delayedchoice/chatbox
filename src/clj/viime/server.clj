(ns viime.server
  (:require [viime.handler :refer [handler]]
      ;      [viime.websocket-handler :refer [start!]]
            [config.core :refer [env]]
            [ring.middleware.logger :as logger]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

 (defn -main [& args]
   (let [port (Integer/parseInt (or (env :port) "3000"))]
     (run-jetty (logger/wrap-with-logger handler) {:port port :join? false})
     ))
