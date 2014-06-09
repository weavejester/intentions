(ns intentions.core
  (:require [clojure.tools.macro :as macro]))

(defn intent? [x]
  (and (fn? x) (::conducts (meta x))))

(defn make-intent
  [& {:keys [dispatch combine default hierarchy]
      :or   {default :default}}]
  {:pre  [(ifn? dispatch) (ifn? combine)]
   :post [(intent? %)]}
  (let [conducts (atom {})
        isa?     (if hierarchy (partial isa? hierarchy) isa?)
        find-fns (memoize
                  (fn [cs d]
                    (let [fs (keep (fn [[k f]] (if (isa? d k) f)) cs)]
                      (or (seq fs)
                          (if-let [f (get cs default)] (list f))
                          (throw (IllegalArgumentException.
                                  (str "No conduct found for dispatch value: " d)))))))
        func     (fn [& args]
                   (->> (apply dispatch args)
                        (find-fns @conducts)
                        (map #(apply % args))
                        (reduce combine)))]
    (with-meta func
      (assoc (meta func) ::conducts conducts))))

(defmacro defintent
  [name & options]
  (let [[name options] (macro/name-with-attributes name options)]
    `(def ~name (make-intent ~@options))))

(defn conducts [intent]
  (-> intent meta ::conducts deref))

(defn add-conduct [intent dispatch-val dispatch-fn]
  (swap! (::conducts (meta intent)) assoc dispatch-val dispatch-fn))

(defn remove-conduct [intent dispatch-val]
  (swap! (::conducts (meta intent)) dissoc dispatch-val))

(defmacro defconduct
  [name dispatch-val & fn-tail]
  `(add-conduct ~name ~dispatch-val (fn ~@fn-tail)))
