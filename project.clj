(defproject intentions "0.1.0-SNAPSHOT"
  :description "Multimethods that combine rather than override inherited behavior"
  :url "https://github.com/weavejester/intentions"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.macro "0.1.2"]]
  :profiles {:dev {:dependencies [[criterium "0.4.2"]]
                   :jvm-opts ^:replace {}}})
