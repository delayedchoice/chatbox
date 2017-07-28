(ns viime.handler
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [compojure.route :refer (resources)]
            [ring.adapter.jetty]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.logger :as logger]
            [ring.middleware.resource :refer [wrap-resource]]
            [compojure.core :refer [GET defroutes ANY routes make-route] ]
            [clojure.java.io :as io]
            [liberator.dev :refer [wrap-trace]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.util.response :refer [resource-response]]
            [ring.middleware.reload :refer [wrap-reload]]))

(defn write-file [file filename]
  (with-open [w (clojure.java.io/output-stream (str "file-" filename ".webm")) ]
   (clojure.java.io/copy file w)))

(def post-message-handler
  (resource
    :allowed-methods [:post]
    :available-media-types ["multipart/form-data"]
    :post! (fn [ctx] (do (prn ctx)
                         (write-file
                           (get-in ctx [:request :params "file" :tempfile])
                           (get-in ctx [:request :params "id" ]))
                         {:id 3} ))
    ;; actually http requires absolute urls for redirect but let's
    ;; keep things simple.
    ;:post-redirect? (fn [ctx] {:location (format "/chat/%s" "3")})
    :new? (fn [_] true) ) )

(def get-message-handler
      (resource
        :allowed-methods [:get]
        :available-media-types ["text/html"]
        :handle-ok "Got it"))

(defn root-handler [x]
  (resource-response "index.html" {:root "public"}))


(def route-list
  [{:method :any :src "/msg" :handler post-message-handler }
   {:method :any :src "/msg/:x" :handler get-message-handler }
   {:method :get :src "/" :handler root-handler }
   ])

(def synthetic-routes
  (apply routes
     (map #(make-route (:method %) (:src %) (:handler %)) route-list)))

(defn reset-routes [other-routes]
    (let [_ (prn "ROUTES BEING RESET")]
       (alter-var-root (var synthetic-routes) (fn [x] (routes x other-routes)))))

(def dev-handler (-> #'synthetic-routes
                     wrap-reload
                     wrap-keyword-params
                     wrap-params
                     wrap-anti-forgery
                     wrap-session
;                    (ring.middleware.defaults/wrap-defaults ring.middleware.defaults/site-defaults )
                     wrap-multipart-params
                     logger/wrap-with-logger
                     (wrap-trace :header :ui)
                      ))

(def handler (-> routes
                wrap-multipart-params
                ))
