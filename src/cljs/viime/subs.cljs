(ns viime.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :frames
 (fn [db]
   (:frames db)))

(re-frame/reg-sub
 :video
 (fn [db]
   (:video db)))

(re-frame/reg-sub
 :state
 (fn [db]
   (:state db)))

(re-frame/reg-sub
 :messages
 (fn [db]
   (:messages db)))

(re-frame/reg-sub
 :boxes
 (fn [db]
   (:boxes db)))

(re-frame/reg-sub
 :active-panel
 (fn [db _]
   (:active-panel db)))
