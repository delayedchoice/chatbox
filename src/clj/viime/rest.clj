(ns viime.rest
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [compojure.route :refer (resources)]
            [ring.adapter.jetty]
            [compojure.core :refer [GET defroutes ANY] ]
            [clojure.java.io :as io]
            [liberator.dev :refer [wrap-trace]]))

(defn write-file [file filename]
   (with-open [w (clojure.java.io/output-stream filename)
              i file ]
     (clojure.java.io/copy i w)))

(defroutes app
  (ANY "/msg" []
       (resource
        :allowed-methods [:post]
        :available-media-types ["multipart/form-data"]
        :post! (fn [ctx] (do (prn ctx)
                              (write-file
                                (get-in ctx [:request :params "file" :tempfile])
                                (get-in ctx [:request :params "file" :filename])) {:id 3} )
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
  ;(resources "/")
  ;(GET "/*" req (page))
)

(defn wrap-error-handling  [handler]
    (fn  [request]
     (try
      (handler request)
       (catch Exception e
         do
         ((prn e)
          {:status 500
          :body "Exception caught"})))))

(def handler
  (-> app
      wrap-multipart-params
      (wrap-trace :header :ui)))
