(ns viime.handler
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [compojure.route :refer (resources)]
            [ring.adapter.jetty]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [org.httpkit.client :as http]
            [clojure.tools.logging :as log]
            [ring.middleware.logger :as logger]
            [ring.middleware.resource :refer [wrap-resource]]
            [compojure.core :refer [GET defroutes ANY routes make-route] ]
            [clojure.java.io :as io]
            [liberator.dev :refer [wrap-trace]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.util.response :refer [response resource-response]]
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

(defn build-path-and-query-string [req]
  (let [q (:query-string req)
        q-string (if (> (count q) 0)
                     (str "?" q)
                     "") ]
    (str (:uri req) q-string)))

(defn build-proxy-url [proxied-host proxied-port req]
  (str "http://"
       proxied-host
       ":"
       proxied-port
       (build-path-and-query-string req)))

(defn build-url [req]
  (build-proxy-url (or #_(env :proxy-target-host) "localhost")
                   (or #_(env :proxy-target-port) "8080")
                   req) )
(defn get-body-as-string [req]
  (if-let [body (get-in req [:body])]
    (let [_ (log/info "raw-body: " body)
          _ (log/info "type: " (type body))
          rv (condp instance? body
                    java.lang.String body
                    (slurp (io/reader body)))
          _ (log/info "extracted-body: " rv)]
      rv)))

(defn proxy-request [req]
 (response {:status 200
            :headers {"Content-Type" "text/plain"}
            :body (:remote-addr req)}) )
;(defn proxy-request [req]
;    (let [out-req {:method (:request-method req)
;                   :url (build-url req)
;                   :headers (:headers req)
;                   :follow-redirects true
;                   :throw-exceptions false
;                   :as :stream }
;				_ (log/info "forwarding-request: " out-req )
;        response @(http/request out-req)
;         _ (log/info "orig-response: " response)
;				 body (get-body-as-string response )
;         _ (log/info "body-from-response " body)
;         ]
;       body))

(def route-list
  [{:method :any :src "/msg" :handler post-message-handler }
   {:method :any :src "/msg/:x" :handler get-message-handler }
	 {:method :post :src "/socket.io/" :handler proxy-request }
	 {:method :get :src "/socket.io/" :handler proxy-request }
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
         ;            wrap-anti-forgery
                     wrap-session
;                    (ring.middleware.defaults/wrap-defaults ring.middleware.defaults/site-defaults )
                     wrap-multipart-params
                     logger/wrap-with-logger
                     (wrap-trace :header :ui)
                      ))

(def handler (-> routes
                wrap-multipart-params
                ))
