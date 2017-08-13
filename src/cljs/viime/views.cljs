(ns viime.views
  (:require [goog.events :as events]
            [secretary.core :as secretary]
            [clojure.string :as string]
            [reagent.core :as reagent]
            [cljs-time.coerce :as trans]
            [cljs-time.core :as date]
            [viime.components :refer [inbox nav-bar side-bar modal]]
            [re-frame.core :as rf]
            )
  (:import [goog History]
           [goog.history EventType]))

(defn call-button []
  (let [remote-peer (reagent/atom "")]
    (fn []
      [:div.row
        [:a.btn.btn-primary.col-md-4
         {:type "button"
          :on-click #(rf/dispatch [:initiate-call @remote-peer])}
         "Call"]
        [:input.col-md-4 {:type "text"
                          :on-change #(reset! remote-peer (-> % .-target .-value))
                          :value @remote-peer
                          :name "remote-peer-id"}]])))
(defn login-button []
  (let [user-id (reagent/atom "")]
    (fn []
      [:div.row
        [:a.btn.btn-primary.col-md-4
         {:type "button"
          :on-click #(rf/dispatch [:login @user-id])}
         "Login"]
        [:input.col-md-4 {:type "text"
                          :on-change #(reset! user-id (-> % .-target .-value))
                          :value @user-id
                          :name "login-id"}]])))
(defn player []
  (fn []
    (let [video (rf/subscribe [:video])]
     [:div.video-container
       [:video.col-md-8 {:src @video :controls false :autoPlay true :id "video"}]])))

(defn demo []
  (fn []
    (let [data (rf/subscribe [:remote-data])
          easyrtcid (rf/subscribe [:easyrtcid])]
     [:div {:id "demoContainer"}
      [:div {:id "connectControls"}
       [:div {:id "iam"} @easyrtcid ]
       [:br]
       [:strong "Connected users:"]
       [:div {:id "otherClients"}
        (prn  "DATA: " @data)
        (for [[user value] @data]
          ^{:key user}
          [:a.btn.btn-primary.col-md-4
              {:type "button"
               :on-click #(rf/dispatch [:perform-call user])}
         user]) ] ]
       [:br]
      [:div {:id "videos"}
       [:video {:autoPlay "autoplay" :class "easyrtcMirror" :id "selfVideo" :muted true }]
       [:div :style "position:relative;float:left;"
        [:video {:autoPlay "autoplay" :id "callerVideo"}]]]]) )
 )

(defn home-panel []
  (let [pid (rf/subscribe [:peerjs-id])
        user (rf/subscribe [:logged-in-as])]
      [:title "ViiMe"]
      [:div [nav-bar]
            [:div [:label (str "PID: " @pid)] ]
            [:div [:label (str "USER: " @user)]]
            [call-button]
            [login-button]
            [inbox]
            [demo]]))

(defn main-panel []
  (fn []
   [home-panel]))
