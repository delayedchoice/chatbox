(ns viime.views
  (:require [goog.events :as events]
            [secretary.core :as secretary]
            [easyrtc.js]
            [reagent.core :as reagent]
            [reagent-modals.modals :as reagent-modals]
            [re-frame.core :as rf]
            [viime.modal :as modal]
            [viime.auth0 :as auth0]
            )
  (:import [goog History]
           [goog.history EventType]))

(defn nav-bar []
 [:nav.navbar.navbar-custom
  [:div.container-fluid
   [:div.navbar-header
    [:button.navbar-toggle
     {:type "button" :data-toggle "collapse" :data-target "#myNavbar" }
     [:span.icon-bar]
     [:span.icon-bar]
     [:span.icon-bar]     ]
    [:a.navbar-brand {:href "#"} "SSi" ]]
   [:div.collapse.navbar-collapse {:id "myNavbar" }
    [:ul.nav.navbar-nav
     [:li.active [:a {:href "#"} "About"]] ]
    [:ul.nav.navbar-nav.navbar-right
     [:li [:a {:on-click #(.show auth0/lock (clj->js {:autoclose true}))}
           [:span.glyphicon.glyphicon-log-in]
           "Login"]]]]]])

(defn availiable-users []
  (fn []
    (let [data (rf/subscribe [:remote-data])
          user (rf/subscribe [:user]) 
          easyrtcid (rf/subscribe [:easyrtcid]) ]
     [:div.demoContainer
      [:div.connectControls
       [:div {:id "iam"} @easyrtcid ]
       [:div {:id "user"} (get-in @user [:profile :nickname]) ]
       [:br]
       [:strong "Connected users:"]
       [:div.otherClients {:id "otherClients"}
        (prn  "DATA: " @data)
        (doall
          (for [[user value] @data]
           ^{:key user}
           [:div.row
            [:a.btn.btn-primary.col-md-12
             {:type "button"
              :on-click #(do (rf/dispatch [:perform-call user]))
              }
             (.idToName js/easyrtc user)]
            ])) ] ]
       [:br]
      ]) ))

(defn home-panel []
  (let [pid (rf/subscribe [:peerjs-id])
        user (rf/subscribe [:logged-in-as])]
       [:title "Video-Chat"]
      [:div [nav-bar]
            [reagent-modals/modal-window]
            [availiable-users]]))

(defn main-panel []
  (fn [] [home-panel]))
