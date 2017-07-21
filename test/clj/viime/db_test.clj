(ns viime.db-test
  (:require [clojure.test :refer :all]
            [viime.db :refer :all]))
(def test-db (atom nil))

(defn db-once-test-fixture [f]
        (reset! test-db (new-db in-mem-data-store) )
        (f))

(defn db-each-test-fixture [f]
        (create-tables @test-db)
        (populate-tables @test-db)
        (f)
        (drop-tables @test-db))

(use-fixtures :once db-once-test-fixture)
(use-fixtures :each db-each-test-fixture)

(deftest sanity
  (is (= (get-prompts @test-db)
        '({:id 1 :text "I am learning Welsh", :level 1}))))

(deftest insert-prompt
  (let [_ (add-prompt @test-db "I am testing insert" 1)
        db-prompt (get-prompts @test-db)]
    (is (= db-prompt
          '({:id 1 :text "I am learning Welsh", :level 1} {:id 2 :text "I am testing insert", :level 1})))
    (is (= (count db-prompt)
          2))))

(deftest insert-prompt-instance
  (let [_ (add-prompt-instance @test-db 1 "2.mpg")
        db-prompt-instance (get-prompt-instances @test-db)]
    (is (= db-prompt-instance
          '({:id 1, :location "1.mpg"} {:id 2, :location "2.mpg"})))
    (is (= (count db-prompt-instance)
          2))))


