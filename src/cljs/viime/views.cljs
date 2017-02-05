(ns viime.views
  (:require [goog.events :as events]
            [secretary.core :as secretary]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [cljs-time.coerce :as trans]
            [cljs-time.core :as date]
            [viime.vcr :refer [player]])
  (:import [goog History]
           [goog.history EventType]))


;; home

(defn home-panel []
  (let [name (re-frame/subscribe [:name])]
      player))


;; about

(defn about-panel []
  (fn []
    [:div "This is the About Page."
     [:div [:a {:href "#/"} "go to Home Page"]]]))


;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      [show-panel @active-panel])))
