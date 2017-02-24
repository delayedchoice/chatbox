(ns viime.rest
  (:require [ajax.core :refer [
                      raw-response-format
                      POST]]))

(defn upload [media filename]
  (let [form-data (doto
                    (js/FormData.)
                    (.append "id" "10")
                    (.append "file" media  filename))
        _ (println media)]
  (POST "/msg" { :body form-data
                 :response-format (raw-response-format)
                 :timeout 10000
                 :error-handler #(js/alert (str "error during post: " %))})))






