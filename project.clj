(defproject intentions "0.2.0"
  :description "Multimethods that combine rather than override inherited behavior"
  :url "https://github.com/weavejester/intentions"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.macro "0.1.2"]]
  :plugins [[codox "0.8.12"]
            [lein-cljsbuild "1.0.6"]]
  :cljsbuild
  {:builds
   [{:source-paths ["src" "test"]
     :compiler {:output-to "target/main.js"
                :optimizations :whitespace}}]
   :test-commands {"unit-tests" ["phantomjs" :runner "target/main.js"]}}
  :aliases
  {"test"      ["test" "intentions.core-test"]
   "test-cljs" ["cljsbuild" "test"]
   "test-all"  ["do" ["test"] ["test-cljs"]]}
  :profiles
  {:provided {:dependencies [[org.clojure/clojurescript "0.0-3308"]]}
   :dev {:dependencies [[criterium "0.4.3"]]
         :jvm-opts ^:replace {}
         :plugins [[com.cemerick/clojurescript.test "0.3.3"]]}})
