(ns viime.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))


(re-frame/reg-sub-raw
 :modal
 (fn [db _] (reaction (:modal @db))))


(re-frame/reg-sub
 :frames
 (fn [db]
   (:frames db)))

(re-frame/reg-sub
 :remote-data
 (fn [db]
   (:remote-data db)))

(re-frame/reg-sub
 :show-loader
 (fn [db]
   (:show-loader db)))


(re-frame/reg-sub
 :current-call
 (fn [db]
   (:current-call db)))


(re-frame/reg-sub
 :easyrtcid
 (fn [db]
   (:easyrtcid db)))

(re-frame/reg-sub
 :peerjs-id
 (fn [db]
   (:peerjs-id db)))

(re-frame/reg-sub
 :video
 (fn [db]
   (:video db)))

(re-frame/reg-sub
  :stream
  (fn [db]
    (:stream db)))

(re-frame/reg-sub
  :state
  (fn [db]
    (get-in db [:state] )))

(re-frame/reg-sub
 :styles
 (fn [db]
   (:styles db)))

(re-frame/reg-sub
 :contacts
 (fn [db]
   (:contacts db)))

(re-frame/reg-sub
 :users
 (fn [db]
   (:users db)))

(re-frame/reg-sub
 :user
 (fn [db]
   (:user db)))


(re-frame/reg-sub
 :logged-in-as
 (fn [db]
   (:logged-in-as db)))


(re-frame/reg-sub
 :boxes
 (fn [db]
   (:boxes db)))

(re-frame/reg-sub
 :active-panel
 (fn [db _]
   (:active-panel db)))
