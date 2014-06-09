(ns intentions.core)

(defn- conduct-keys [hierarchy dispatch-val]
  (into #{dispatch-val}
        (if hierarchy
          (ancestors hierarchy dispatch-val)
          (ancestors dispatch-val))))

(defn make-intent
  [{:keys [dispatch combine default hierarchy]
    :or   {default :default}}]
  (let [conducts (atom {})
        func     (fn [& args]
                   (->> (apply dispatch args)
                        (conduct-keys hierarchy)
                        (keep @conducts)
                        (map #(apply % args))
                        (reduce combine)))]
    (with-meta func
      (assoc (meta func) ::conducts conducts))))

(defmacro defintent
  [name & {:as options}]
  `(def ~name (make-intent ~options)))

(defn add-conduct! [intent dispatch-val dispatch-fn]
  (swap! (::conducts (meta intent)) assoc dispatch-val dispatch-fn))

(defmacro defconduct
  [name dispatch-val & fn-tail]
  `(add-conduct! ~name ~dispatch-val (fn ~@fn-tail)))
