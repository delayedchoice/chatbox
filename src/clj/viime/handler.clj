(ns viime.handler
  (:require [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [compojure.core :refer [GET defroutes ANY routes make-route] ]
            [compojure.route :as route]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.util.response :refer [file-response response resource-response]]
            [clojure.tools.logging :as log]
            [ring.middleware.logger :as logger]
            [ring.middleware.reload :refer [wrap-reload]]))



(defroutes main-routes
  (GET "/" [] (file-response "index.html" {:root "resources/public"}))
  ;(route/resources "resources/public")
  (route/not-found "This Page not found"))

(def synthetic-routes main-routes)

(defn reset-routes [other-routes]
    alter-var-root (var synthetic-routes) (fn [x] (routes x other-routes)))

(def dev-handler (-> #'synthetic-routes
                     wrap-reload
                     (wrap-resource "public")
                     wrap-keyword-params
                     wrap-params
         ;            wrap-anti-forgery
                     wrap-session
;                    (ring.middleware.defaults/wrap-defaults ring.middleware.defaults/site-defaults )
                     wrap-multipart-params
                     logger/wrap-with-logger
            ;         (wrap-trace :header :ui)
                      ))

(def handler (-> routes
                wrap-multipart-params
                ))
