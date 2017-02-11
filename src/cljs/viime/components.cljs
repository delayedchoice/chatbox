(ns viime.components
    (:require [re-frame.core :as rf]
              [goog.events :as events])
    (:import [goog.events EventType]))

(defn nav-bar []
 [:nav.navbar.navbar-inverse
  [:div.container-fluid
   [:div.navbar-header
    [:button.navbar-toggle
     {:type "button" :data-toggle "collapse" :data-target "#myNavbar" }
     [:span.icon-bar]
     [:span.icon-bar]
     [:span.icon-bar]     ]
    [:a.navbar-brand {:href "#"} "Viime" ]]
   [:div.collapse.navbar-collapse {:id "myNavbar" }
    [:ul.nav.navbar-nav
     [:li.active [:a {:href "#"} "About"]] ]
    [:ul.nav.navbar-nav.navbar-right
     [:li [:a {:href "#"}
           [:span.glyphicon.glyphicon-log-in]
           "Login"]]]]]])

(defn side-bar []
  (let [boxes (rf/subscribe [:boxes])]
    [:div.col-sm-2.sidenav
     (for [box @boxes]
      ^{:key (:id box)}
       [:div
        [:p [:a {:href "#"} "Link"]]])]))

(defn message [{:keys [from subject timedate]} msg]
  [:div.unread.message
   [:div.col-md-4.list-group-item.message-field from]
   [:div.col-md-6.list-group-item.message-field subject]
   [:div.col-md-2.list-group-item.message-field timedate]])

(defn inbox []
  (let [messages (rf/subscribe [:messages])]
    [:div.list-group.col-md-10.inbox
     [:div
      [:div.col-md-4.text-center.list-group-item.header-field [:label "From"]]
      [:div.col-md-6.text-center.list-group-item.header-field [:label "Subject"]]
      [:div.col-md-2.text-center.list-group-item.header-field [:label "Date"]]]

      (for [msg @messages]
        ^{:key (:id msg)}
        [message msg])]
    ))
