(ns viime.views
  (:require [goog.events :as events]
            [secretary.core :as secretary]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [cljs-time.coerce :as trans]
            [cljs-time.core :as date]
            [viime.components :refer [inbox nav-bar side-bar modal]]
            [viime.vcr :refer [player]])
  (:import [goog History]
           [goog.history EventType]))

(defn home-panel []
  (let []
      [:title "ViiMe"]
      [:div [nav-bar][side-bar] [modal] [inbox] #_[player]]))

(defn main-panel []
  (fn []
   [home-panel]))
