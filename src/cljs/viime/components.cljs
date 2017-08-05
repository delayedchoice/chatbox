(ns viime.components
    (:require [re-frame.core :as rf]
              [viime.vcr :refer [player]]
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

(defn modal []
  [:div {:aria-hidden "true"
         :aria-labelledby "myModalLabel"
         :role "dialog"
         :tabIndex "-1"
         :id "myModal"
         :class "modal fade"
         :style { :display "none" }
         }
   [:div.modal-dialog
    [:div.modal-content
     [:div.modal-header
      [:button {:aria-hidden true
                :data-dismiss "modal"
                :class "close"
                :type "button" }]
      [:h4.modal-title
       [:div
        [:span.textbox
         "To: "
         [:input {:type "text" :name "url" :autoFocus true}]]
        [:span.textbox
         "Subject: "
         [:input {:type "text" :name "url" }]]
        [:span.textbox
         "Tags: "
         [:input {:type "text" :name "url" }]]]]]
     [:div.modal-body
      [:form.form-horizontal{:role "form"}


       [:div.form-group
         [player] ]]]]]])

(defn side-bar []
  (let [boxes (rf/subscribe [:boxes])]
    [:div.col-sm-2.sidenav
     [:div
      [:a.btn.btn-primary.col-md-12
       {:type "button" :href "#myModal" :data-toggle "modal"} "Compose"]
      #_(for [box @boxes]
        ^{:key (:id box)}
        [:div
         [:p [:a {:href "#"} "Link"]]])]]))

(defn user-view [{:keys [whole-name status pid]} styles]
  [  :div {:class (styles status)}
   [:div.col-md-6.list-group-item.message-field whole-name]
   [:div.col-md-2.list-group-item.message-field status]
   [:div.col-md-2.list-group-item.message-field pid]
   ])

(defn inbox []
  (let [users (rf/subscribe [:users])
        styles  (rf/subscribe [:styles ])]
    [:div.list-group.col-md-12.inbox
     [:div
      [:div.col-md-6.text-center.list-group-item.header-field [:label "Contact"]]
      [:div.col-md-2.text-center.list-group-item.header-field [:label "Status"]]
      [:div.col-md-2.text-center.list-group-item.header-field [:label "PID"]]
      ]
     (doall
       (for [user (vals @users)]
        ^{:key (:login user)}
        [user-view user @styles]))]))
