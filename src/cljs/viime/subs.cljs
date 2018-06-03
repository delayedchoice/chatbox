(ns viime.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))


(re-frame/reg-sub
 :remote-data
 (fn [db]
   (:remote-data db)))

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
  :stream
  (fn [db]
    (:stream db)))

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

