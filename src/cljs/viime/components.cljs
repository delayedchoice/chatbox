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
      [:h4.modal-title "Compose"]]
     [:div.modal-body
      [:form.form-horizontal {:role "form"}
       [:div.form-group
        [:label.col-lg-2.control-label "To"]
        [:div.col-lg-10
         [:input {:type "text"
                  :placeholder ""
                  :id :to
                  :class "form-control"}]]]
			 [:div.form-group
        [:label.col-lg-2.control-label "Subject"]
        [:div.col-lg-10
         [:input {:type "text"
                  :placeholder ""
                  :id :subject
                  :class "form-control"}]]]
       [:div.form-group
        [:label.col-lg-2.control-label "Message"]
        [:div.col-lg-10
         [player]
         #_[:textarea {:rows "10"
                     :cols "30"
                     :class "form-control"
                     :id ""
                     :name ""}]]]
       [:a.btn.btn-primary.pull-right {:type "button"} "Submit"]]]]]])

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

(defn message [{:keys [from subject timedate status style]} styles]
  [:div {:class (styles status)}
   [:div.col-md-4.list-group-item.message-field from]
   [:div.col-md-6.list-group-item.message-field subject]
   [:div.col-md-2.list-group-item.message-field timedate]])

(defn inbox []
  (let [messages (rf/subscribe [:messages])
        styles   (rf/subscribe [:styles ])]
    [:div.list-group.col-md-10.inbox
     [:div
      [:div.col-md-4.text-center.list-group-item.header-field [:label "From"]]
      [:div.col-md-6.text-center.list-group-item.header-field [:label "Subject"]]
      [:div.col-md-2.text-center.list-group-item.header-field [:label "Date"]]]
     (doall
       (for [msg @messages]
        ^{:key (:id msg)}
        [message msg @styles]))]))
