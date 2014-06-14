(defproject intentions "0.1.0"
  :description "Multimethods that combine rather than override inherited behavior"
  :url "https://github.com/weavejester/intentions"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.macro "0.1.2"]]
  :plugins [[codox "0.8.9"]
            [lein-cljsbuild "1.0.3"]
            [com.keminglabs/cljx "0.4.0"]]
  :codox {:sources ["target/classes"]}
  :cljx  {:builds [{:source-paths ["src"]
                    :output-path "target/classes"
                    :rules :clj}
                   {:source-paths ["src"]
                    :output-path "target/classes"
                    :rules :cljs}]}
  :hooks [cljx.hooks]
  :cljsbuild {:builds [{:source-paths ["target/classes"]
                        :compiler {:output-to "target/main.js"}}]}
  :profiles
  {:provided {:dependencies [[org.clojure/clojurescript "0.0-2234"]]}
   :dev {:dependencies [[criterium "0.4.2"]], :jvm-opts ^:replace {}}
   :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}})
