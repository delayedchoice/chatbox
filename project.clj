(defproject viime "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]
;                  [reagent "0.6.0"]
                 [liberator "0.14.1"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [cljs-ajax "0.5.8"]
                 [re-frame "0.10.1"]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [secretary "1.2.3"]
                 [ns-tracker "0.3.0"]
                 [compojure "1.5.1"]
                 [ring.middleware.logger "0.5.0"]
                 [yogthos/config "0.8"]
                 [ring "1.5.0"]
                 [ring/ring-anti-forgery "1.1.0"]
                 [org.clojure/core.cache "0.6.4"]
                 ;sente
                 [org.clojure/core.async    "0.2.395"]
                 [org.clojure/tools.nrepl   "0.2.12"] ; Optional, for Cider
                 [com.taoensso/sente        "1.11.0"] ; <--- Sente
                 [com.taoensso/timbre       "4.7.4"]
                 [http-kit                             "2.2.0"]
                 [ring/ring-defaults        "0.2.1"]
                 [korma "0.4.1"]
                 [com.h2database/h2 "1.3.170"]
                 [overtone/at-at "1.2.0"]
                 [com.stuartsierra/component "0.3.2"]
                 [org.clojars.frozenlock/reagent-modals "0.2.3"]
                 [cljsjs/auth0-lock "11.5.2-0"]
;                 [com.datomic/datomic-free "0.9.5697"]
                ]

  :repl-options { :timeout 1200000 }
  :plugins [[lein-ring "0.12.4"]
            [lein-cljsbuild "1.1.4"]
            [lein-ancient        "0.6.10"]
            [lein-less "1.7.5"] ]

  :ring {:handler viime.handler/dev-handler}
  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "test/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"]

  :figwheel {:server-ip "100.115.92.206"
             :css-dirs ["resources/public/css"]
             :ring-handler viime.handler/dev-handler }

  :less {:source-paths ["less"]
         :target-path  "resources/public/css"}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.8.2"]
                   [com.cemerick/piggieback "0.2.1"]
                   [figwheel-sidecar "0.5.8"]
                   [org.clojure/tools.namespace "0.2.11"]
;                   [com.stuartsierra/component.repl "0.2.0"]
                   ]
    :repl-options {:timeout 120000
    :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
    :plugins      [[cider/cider-nrepl "0.12.0"]
                   [lein-figwheel "0.5.8"]
                   [lein-doo "0.1.7"]]
    :source-paths ["src/cljs" "dev"]
    }
   :uberjar
   {:dependencies [
                   [org.clojure/tools.namespace "0.2.11"]
                   ]
    :plugins      [
                   [lein-doo "0.1.7"]]
    :source-paths ["src/cljs"]
    }
  }

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:websocket-host "100.115.92.206"
                    :on-jsload "viime.core/mount-root"}
     :compiler     {:main                 viime.core
                    :foreign-libs         [{:file "src/easyrtc/socket.io.js" :provides ["socket.io.js"]},
                                           {:file "src/easyrtc/easyrtc.js" 
                                            :provides ["easyrtc.js"] 
                                            :requires ["socket.io.js"]}
                                          ]
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}
                    :optimizations   :none
                    }}

  {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main                 viime.core
                    :foreign-libs         [{:file "src/easyrtc/socket.io.js"
                                            :provides ["socket.io.js"]},
                                           {:file "src/easyrtc/easyrtc.js"
                                            :provides ["easyrtc.js"]
                                            :requires ["socket.io.js"]}
                                          ]
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out-min"
                    :asset-path           "js/compiled/out-min"
                    :source-map-timestamp true
                    }}
  {:id           "uberjar"
   :jar           true
     :source-paths ["src/cljs"]
     :compiler     {:main                 viime.core
                    :foreign-libs         [{:file "src/easyrtc/socket.io.js"
                                            :provides ["socket.io.js"]},
                                           {:file "src/easyrtc/easyrtc.js"
                                            :provides ["easyrtc.js"]
                                            :requires ["socket.io.js"]}
                                          ]
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out-uberjar"
                    :asset-path           "js/compiled/out-uberjar"
                    :source-map-timestamp true
                    :externs             [ "src/easyrtc/easyrtc.js" ] 
                    :optimizations   :advanced
                    }}
   #_{:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            viime.core
                    :foreign-libs         [{:file "src/easyrtc/socket.io.js"
                                          ;:file-min "react/react.min.js"
                                          :provides ["socket.io.js"]},
                                         {:file "src/easyrtc/easyrtc.js"
                                          ; :file-min "react/react.min.js"
                                          :provides ["easyrtc.js"]
                                          :requires ["socket.io.js"]}
                                          ]
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                   :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :none
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}

   #_{:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:main          viime.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :foreign-libs    [{:file "resources/public/js/recorder.js"
                                     :provides ["rw"]
                                     :module-type :commonjs}
                                     {:file "resources/public/js/recorderWorker.js"
                                     :provides ["r"]
                                     :module-type :commonjs}]
                     :output-dir    "resources/public/js/compiled/test/out"
                     :optimizations :none}}
    ]}
  :main viime.system
  :aot [clojure.tools.logging.impl viime.system]
  :uberjar-name "viime.jar"
  :prep-tasks [["cljsbuild" "once" "uberjar"] "compile"])
