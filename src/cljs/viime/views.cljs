(ns viime.views
  (:require [goog.events :as events]
            [secretary.core :as secretary]
            [easyrtc.js]
            [reagent.core :as reagent]
            [reagent-modals.modals :as reagent-modals]
            [re-frame.core :as rf]
            )
  (:import [goog History]
           [goog.history EventType]))

(defn loader []
  (let [show-loader (rf/subscribe [:show-loader])]
    [:div {:class (if @show-loader "loader" "")}]))


(defn videos []
  (fn []
    (let [current-call-id (rf/subscribe [:current-call])
          _ (prn  "CurrentCaller: " @current-call-id) ]
     [:div {:id "videos" :class (if (nil? @current-call-id) "hidden" "")}
      [:video.selfVideo.easyrtcMirror {:autoPlay "autoplay" :id "self" :muted true }]
      [:video.callerVideo.callerDiv  {:autoPlay "autoplay" :id "caller"}]])))

(defn login []
  (let [show-loader (rf/subscribe [:show-loader])
				user-id (reagent/atom "")
        password (reagent/atom "") ]
      (fn []
        [:div {:id "login"
							 :class (if @show-loader "loader" "")
               :on-key-press #(if (= 13 (.-charCode %))
                                  (do
                                      (rf/dispatch [:login @user-id @password])
                                      (reagent-modals/close-modal!)))}
 ;       [loader]
         [:div.row.center-block
          [:input.col-md-12.col-centered {:type "text"
                                          :on-change #(reset! user-id (-> % .-target .-value))
																				  :class (if @show-loader "hide" "")
                                          :placeholder "Username"
                                          :value @user-id
                                          :id "login-id" }]]
         [:div.row.center-block
          [:input.col-md-12 {:type "text"
                             :on-change #(reset! password (-> % .-target .-value))
														 :class (if @show-loader "hide" "")
                             :placeholder "Password"
                             :value @password
                             :id "password"}]]])))

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
     [:li [:a {:on-click #(reagent-modals/modal!
                            [login]
                            {:size :sm
                             :hide (fn [] (rf/dispatch [:do-login]))
                             :shown (fn [] (.focus (.getElementById js/document "login-id")))
                             })}
           [:span.glyphicon.glyphicon-log-in]
           "Login"]]]]]])

(defn availiable-users []
  (fn []
    (let [data (rf/subscribe [:remote-data])
          easyrtcid (rf/subscribe [:easyrtcid]) ]
     [:div.demoContainer
      [:div.connectControls
       [:div {:id "iam"} @easyrtcid ]
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
      [:title "SSi"]
      [:div [nav-bar]
            [reagent-modals/modal-window]
            [availiable-users]
            [videos]]))

(defn main-panel []
  (fn []
  [home-panel]))
