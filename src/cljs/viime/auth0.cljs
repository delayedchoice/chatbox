(ns viime.auth0
  (:require [re-frame.core :as re-frame]
            [viime.config :as config]
            [cljsjs.auth0-lock]))


(def lock
  "The auth0 lock instance used to login and make requests to Auth0"
  (let [client-id (:client-id config/auth0)
        domain (:domain config/auth0)
        options (clj->js {})]
    (js/Auth0Lock. client-id domain options)))


(defn on-authenticated
  "Function called by auth0 lock on authentication"
  [auth-result-js]
  (js/alert (str "Auth0 authentication result: "
                 (js->clj auth-result-js))))

(.on lock "authenticated" on-authenticated)
