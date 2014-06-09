(ns intentions.core)

(defn make-intent
  [{:keys [dispatch combine default hierarchy]
    :or   {default :default}}]
  (let [conducts (atom {})
        isa?     (if hierarchy (partial isa? hierarchy) isa?)
        find-fns (memoize (fn [cs d]
                            (let [fs (keep (fn [[k f]] (if (isa? d k) f)) cs)]
                              (or (seq fs)
                                  (list (get cs default))))))
        func     (fn [& args]
                   (->> (apply dispatch args)
                        (find-fns @conducts)
                        (map #(apply % args))
                        (reduce combine)))]
    (with-meta func
      (assoc (meta func) ::conducts conducts))))

(defn conducts [intent]
  (-> intent meta ::conducts deref))

(defmacro defintent
  [name & {:as options}]
  `(def ~name (make-intent ~options)))

(defn add-conduct! [intent dispatch-val dispatch-fn]
  (swap! (::conducts (meta intent)) assoc dispatch-val dispatch-fn))

(defmacro defconduct
  [name dispatch-val & fn-tail]
  `(add-conduct! ~name ~dispatch-val (fn ~@fn-tail)))

(defn derive-all
  ([tag parents]   (doseq [p parents] (derive tag p)))
  ([h tag parents] (reduce #(derive %1 tag %2) h parents)))
