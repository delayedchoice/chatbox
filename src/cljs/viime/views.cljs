(ns viime.views
  (:require [goog.events :as events]
            [secretary.core :as secretary]
            [easyrtc.js]
            [clojure.string :as string]
            [reagent.core :as reagent]
            [cljs-time.coerce :as trans]
            [cljs-time.core :as date]
            [reagent-modals.modals :as reagent-modals]
            [re-frame.core :as rf]
            )
  (:import [goog History]
           [goog.history EventType]))

(defn videos []
  (fn []
    [:div {:id "videos"}
    [:video.selfVideo.easyrtcMirror {:autoPlay "autoplay" :id "self" :muted true }]
    [:div.callerDiv
     [:video.callerVideo {:autoPlay "autoplay" :id "caller"}]]]))

(defn login []
(let [user-id (reagent/atom "")
      password (reagent/atom "")]
    (fn []
      [:div.modal-container
       [:div.row.center-block
        [:input.col-md-12.col-centered {:type "text"
                          :on-change #(reset! user-id (-> % .-target .-value))
                          :value @user-id
                          :name "login-id"}]]
       [:div.row.center-block
        [:input.col-md-12 {:type "text"
                          :on-change #(reset! password (-> % .-target .-value))
                          :value @password
                          :name "password"}]]
       [:div.row.center-block
        [:a.btn.btn-primary.col-md-12
         {:type "button"
          :on-click #(do ;(reagent-modals/close-modal!)
                         (reagent-modals/modal! [videos] {:size :sm})
                         (reagent-modals/close-modal!)
                         (rf/dispatch [:login @user-id @password]))}
         "Login"]]]))  )

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
     [:li [:a {:on-click #(reagent-modals/modal! [login] {:size :sm})}
           [:span.glyphicon.glyphicon-log-in]
           "Login"]]]]]])

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
          [:div.row
           [:a.btn.btn-primary.col-md-12
            {:type "button"
             :on-click #(do (rf/dispatch [:perform-call user])
                            (reagent-modals/modal! [videos] ))
             :id "otherClients"}
            (.idToName js/easyrtc user)]]) ] ]
       [:br]
      ]) ))

(defn home-panel []
  (let [pid (rf/subscribe [:peerjs-id])
        user (rf/subscribe [:logged-in-as])]
      [:title "SSi"]
      [:div [nav-bar]
;            [:div [:label (str "PID: " @pid)] ]
;            [:div [:label (str "USER: " @user)]]
            [reagent-modals/modal-window]
            [demo]]))

(defn main-panel []
  (fn []
  [home-panel]))
