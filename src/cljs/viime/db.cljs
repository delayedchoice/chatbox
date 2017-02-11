(ns viime.db)

(def default-db
  {:name "re-frame"
   :frames []
   :video nil
   :state :recording
   :boxes [{:id 12}]
   :messages [{:from "Charlie@Viime.com"
               :subject "OK"
               :id 2
               :timedate "TODAY" }
              {:from "Bobby@Viime.com"
               :subject "WELCOME"
               :id 1
               :timedate "YESTERDAY" }] })
