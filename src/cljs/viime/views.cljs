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
     [:div.demoContainer
      [:div.connectControls
       [:div {:id "iam"} @easyrtcid ]
       [:br]
       [:strong "Connected users:"]
       [:div.otherClients
        (prn  "DATA: " @data)
        (for [[user value] @data]
          ^{:key user}
          [:a.btn.btn-primary.col-md-4
              {:type "button"
               :on-click #(rf/dispatch [:perform-call user])}
         user]) ] ]
       [:br]
      [:div {:id "videos"}
       [:video.selfVideo.easyrtcMirror {:autoPlay "autoplay" :id "selfVideo" :muted true }]
       [:div.callerDiv
        [:video.callerVideo {:autoPlay "autoplay" :id "callerVideo"}]]]]) )
 )

;(nil {"aAG4rO2B3kXz7Ar3" {"easyrtcid" "aAG4rO2B3kXz7Ar3",
;                          "roomJoinTime" 1502845797451,
;			                    "presence" {"show" "chat",
;			                   						 "status" nil},
;                          "apiField" {"mediaIds" {"fieldName" "mediaIds",
;			                   												 "fieldValue" {"default" "{b461c0db-dfbb-9140-85c0-5f91946b0387}"}}}}}
;      {"2yR0guO57r17dF0e" {"easyrtcid" "2yR0guO57r17dF0e",
;                          "roomJoinTime" 1502805171085,
;                          "presence" {"show" "chat", "status" nil},
;                          "apiField" {"mediaIds" {"fieldName" "mediaIds",
;                          "fieldValue" {"default" "EdvYjj5BXVFtvRsxslbYrn54WEuUQ7QwZS35"}}}}})
;adding another

;({"hXarvTuJyfRfNsqV" {"easyrtcid" "hXarvTuJyfRfNsqV",
;                      "roomJoinTime" 1502846936498,
;                      "presence" {"show" "chat", "status" nil},
;                      "apiField" {"mediaIds" {"fieldName" "mediaIds",
;                                              "fieldValue" {"default" "{bd348d88-a1be-6349-9ac9-841d1bec856b}"}}}}}
;  nil
; {"08hFvfI0JdQFdrng" {"easyrtcid" "08hFvfI0JdQFdrng",
;                      "roomJoinTime" 1502846842854,
;                      "presence" {"show" "chat", "status" nil},
;                      "apiField" {"mediaIds" {"fieldName" "mediaIds",
;                                  "fieldValue" {"default" "{ca9733c6-577f-1748-8dc3-09d8d089b8af}"}}}},
;  "2yR0guO57r17dF0e" {"easyrtcid" "2yR0guO57r17dF0e",
;                      "roomJoinTime" 1502805171085,
;                      "presence" {"show" "chat", "status" nil},
;                      "apiField" {"mediaIds" {"fieldName" "mediaIds",
;                                  "fieldValue" {"default" "EdvYjj5BXVFtvRsxslbYrn54WEuUQ7QwZS35"}}}}})
;
(defn home-panel []
  (let [pid (rf/subscribe [:peerjs-id])
        user (rf/subscribe [:logged-in-as])]
      [:title "ViiMe"]
      [:div [nav-bar]
            [:div [:label (str "PID: " @pid)] ]
            [:div [:label (str "USER: " @user)]]
            [call-button]
            [login-button]
           ; [inbox]
            [demo]]))

(defn main-panel []
  (fn []
   [home-panel]))
