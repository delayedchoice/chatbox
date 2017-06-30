(ns viime.rest
  (:require [ajax.core :refer [
                      raw-response-format
                      POST]]))

(defn upload [media id]
  (let [form-data (doto
                    (js/FormData.)
                    (.append "id" id)
                    (.append "file" media))]
  (POST "/msg" { :body form-data
                 :response-format (raw-response-format)
                 :timeout 10000
                 :error-handler #(js/alert (str "error during post: " %))})))






