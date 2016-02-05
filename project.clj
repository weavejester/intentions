(defproject intentions "0.2.0"
  :description "Multimethods that combine rather than override inherited behavior"
  :url "https://github.com/weavejester/intentions"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.macro "0.1.5"]]
  :plugins [[codox "0.8.13"]
            [lein-cljsbuild "1.1.2"]
            [lein-doo "0.1.6"]]
  :cljsbuild
  {:builds
   {:test
    {:source-paths ["src" "test"]
     :compiler {:output-to "target/main.js"
                :output-dir "target"
                :main intentions.test-runner
                :optimizations :simple}}}}
  :doo {:paths {:rhino "lein run -m org.mozilla.javascript.tools.shell.Main"}}
  :aliases
  {"test-cljs" ["doo" "rhino" "test" "once"]
   "test-all"  ["do" ["test"] ["test-cljs"]]}
  :profiles
  {:provided {:dependencies [[org.clojure/clojurescript "1.7.228"]]}
   :dev {:dependencies [[criterium "0.4.3"]]
         :jvm-opts ^:replace {}}})
