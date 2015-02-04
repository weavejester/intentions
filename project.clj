(defproject intentions "0.1.2"
  :description "Multimethods that combine rather than override inherited behavior"
  :url "https://github.com/weavejester/intentions"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.macro "0.1.2"]]
  :plugins [[codox "0.8.10"]
            [lein-cljsbuild "1.0.4"]]
  :codox {:sources ["target/generated/src"]}
  :cljx
  {:builds
   [{:source-paths ["src"], :output-path "target/generated/src", :rules :clj}
    {:source-paths ["test"], :output-path "target/generated/test", :rules :clj}
    {:source-paths ["src"], :output-path "target/generated/src", :rules :cljs}
    {:source-paths ["test"], :output-path "target/generated/test", :rules :cljs}]}
  :source-paths ["src" "target/generated/src"]
  :test-paths   ["test" "target/generated/test"]
  :cljsbuild
  {:builds
   [{:source-paths ["target/generated/src" "target/generated/test"]
     :compiler {:output-to "target/main.js"}}]
   :test-commands {"unit-tests" ["phantomjs" :runner "target/main.js"]}}
  :aliases
  {"test-cljs" ["do" ["cljx" "once"] ["cljsbuild" "test"]]
   "test-all"  ["do" ["test"] ["cljsbuild" "test"]]}
  :profiles
  {:provided {:dependencies [[org.clojure/clojurescript "0.0-2760"]]}
   :dev {:dependencies [[criterium "0.4.3"]]
         :jvm-opts ^:replace {}
         :plugins [[com.keminglabs/cljx "0.5.0"]
                   [com.cemerick/clojurescript.test "0.3.3"]]
         :prep-tasks [["cljx" "once"]]}})
