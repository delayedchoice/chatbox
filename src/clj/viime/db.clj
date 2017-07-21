(ns viime.db
      (:require [korma.db :refer :all]
                [korma.core :refer :all]
                [clojure.string :as str]
                [clojure.java.jdbc :as j])
      )

(def in-mem-data-store
  (h2 {:db "mem:db"
       :user "sa"
       :password ""
        }))

(defn new-db [desc]
  (create-db desc))




(defentity prompt_instance  (entity-fields :id :location))
(defentity prompt (has-many prompt_instance) (entity-fields :id :text :level ))

(def create-promt-instance-script
  "CREATE TABLE \"prompt_instance\" (\"id\" int not null primary key auto_increment, \"prompt_id\" int, \"location\" varchar(1000))")

(def create-prompt-script
   "CREATE TABLE \"prompt\" (\"id\" int not null primary key auto_increment , \"text\" varchar(1000), \"level\" int)")

(def drop-script
  ["DROP TABLE \"prompt\""
   "DROP TABLE \"prompt_instance\""])

(defn drop-tables [db]
  (with-db db
    (dorun (map exec-raw drop-script))))

(defn create-tables [db]
  (with-db db
    (dorun (map exec-raw [create-prompt-script create-promt-instance-script]))
    ))

(defn populate-tables [db]
  (with-db db
    (let [pid (insert prompt (values {:text "I am learning Welsh" :level 1}))
          id (first (vals pid))
          ]
     (insert prompt_instance (values {:prompt_id id :location "1.mpg"})))))

(defn start [db]
  (create-tables db)
  (populate-tables db))

(defn get-prompts [db]
  (with-db db
    (select prompt)))

(defn get-prompt-instances [db]
  (with-db db
    (select prompt_instance)))

(defn add-prompt [db text level]
  (with-db db
    (insert prompt (values {:text text :level level}))))

(defn add-prompt-instance [db prompt-id location]
  (with-db db
    (insert prompt_instance (values {:prompt_id prompt-id :location location})) ))

(defn close-db [db]
  (with-db db
;    (drop-tables db)
    (exec-raw "SHUTDOWN")
    ))
