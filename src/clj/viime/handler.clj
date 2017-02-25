(ns viime.handler
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [compojure.route :refer (resources)]
            [ring.adapter.jetty]
            [ring.middleware.logger :as logger]
            [compojure.core :refer [GET defroutes ANY] ]
            [clojure.java.io :as io]
            [liberator.dev :refer [wrap-trace]][compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [resource-response]]
            [ring.middleware.reload :refer [wrap-reload]]))

(defn write-file [file filename]
    (with-open [w (clojure.java.io/output-stream "file5.webm") ]
     (clojure.java.io/copy file w)))

(defroutes routes
  (ANY "/msg" []
       (resource
        :allowed-methods [:post]
        :available-media-types ["multipart/form-data"]
        :post! (fn [ctx] (do (prn ctx)
                              (write-file
                                 (get-in ctx [:request :params "file" :tempfile])
																"file.webm"
                                #_(get-in ctx [:request :params "file" :filename])) {:id 3} )
                 )
        ;; actually http requires absolute urls for redirect but let's
        ;; keep things simple.
        ;:post-redirect? (fn [ctx] {:location (format "/chat/%s" "3")})
         :new? (fn [_] true) ))

  (ANY "/msg/:x" [x]
       (resource
        :allowed-methods [:get]
        :available-media-types ["text/html"]
        :handle-ok "Got it"))

  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (resources "/"))

(def dev-handler (-> #'routes wrap-reload))

(def handler (-> routes
                wrap-multipart-params
                 (logger/wrap-with-logger)
                #_(wrap-trace :header :ui) ))
