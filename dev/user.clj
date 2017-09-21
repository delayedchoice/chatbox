(ns user
  "Utility functions to rapidly bootstrap the REPL for interactive
  development. This file is automatically loaded by Clojure on
  startup.

  Run `(go)` to load all source code, start the component system
  running, and switch to the `dev` namespace. `(reset)` is an alias
  for `(go)`.

  Or run `(dev)` to just load code and switch to `dev` without
  starting the system."
  (:require
      [viime.system :as system]
;   [com.stuartsierra.component.user-helpers :refer [reset]]
			[clojure.java.io :as io]
      [clojure.string :as str]
      [clojure.pprint :refer (pprint)]
      [clojure.repl :refer :all]
      [clojure.test :as test]
      [figwheel-sidecar.repl-api :as figwheel]
      [clojure.tools.namespace.repl :refer (refresh)]
         ))

(def system nil)
(defn init
  "Constructs the current development system."
  []
  (alter-var-root #'system
    (constantly (system/system))))

(defn start-dev
  "Starts the current development system."
  []
  (figwheel/start-figwheel!)
  (alter-var-root #'system system/start))

(defn start
  "Starts the current test system."
  []
  (alter-var-root #'system system/start))

(defn stop
  "Shuts down and destroys the current development system."
  []
  (figwheel/stop-figwheel!)
  (alter-var-root #'system
    (fn [s] (when s (system/stop s)))))

(defn go
  "Initializes the current test system and starts it running."
  []
  (init)
  (start))

(defn go-dev
  "Initializes the current development system and starts it running."
  []
  (init)
  (start-dev))

(defn reset []
    (stop)
    (refresh :after 'user/go))
