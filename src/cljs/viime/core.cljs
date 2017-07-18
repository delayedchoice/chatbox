(ns viime.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [viime.events]
							[viime.websocket-client]
              [viime.subs]
              [viime.routes :as routes]
              [viime.views :as views]
              [viime.config :as config]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (routes/app-routes)
  (re-frame/dispatch-sync [:initialize-db])
  (re-frame/dispatch [:initialize-video])
  (dev-setup)
  (mount-root))
