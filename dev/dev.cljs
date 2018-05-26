(ns dev
    (:require
     [viime.core] ;; <<<<--- require your main namespace
     [figwheel.client :as fw]))
;; do things you don't want to happen in production
(fw/start {
  :websocket-url "ws://localhost:3449/figwheel-ws" })

