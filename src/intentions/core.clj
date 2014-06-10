(ns intentions.core
  (:require [clojure.tools.macro :as macro]))

(defn intent? [x]
  (and (fn? x) (::conducts (meta x))))

(defn make-intent
  [& {:keys [dispatch combine default hierarchy]
      :or   {default :default}}]
  {:pre  [(ifn? dispatch) (ifn? combine)]
   :post [(intent? %)]}
  (let [conducts (atom {:fmap {} :cache {}})
        isa?     (if hierarchy #(isa? hierarchy %1 %2) isa?)
        func     (fn [& args]
                   (let [con @conducts
                         dv  (apply dispatch args)
                         cs  (:fmap con)
                         fs  (or ((:cache con) dv)
                                 (loop [cs cs fs '()]
                                   (if (seq cs)
                                     (let [c (first cs)]
                                       (if (isa? dv (key c))
                                         (recur (rest cs) (conj fs (val c)))
                                         (recur (rest cs) fs)))
                                     (do (swap! conducts assoc-in [:cache dv] fs)
                                         fs))))]
                     (if (seq fs)
                       (loop [ret (apply (first fs) args), fs (rest fs)]
                         (if (seq fs)
                           (recur (combine ret (apply (first fs) args)) (rest fs))
                           ret))
                       (if-let [f (cs default)]
                         (apply f args)
                         (throw (IllegalArgumentException.
                                 (str "No conduct found for dispatch value: " dv)))))))]
    (with-meta func
      (assoc (meta func) ::conducts conducts))))

(defmacro defintent
  [name & options]
  (let [[name options] (macro/name-with-attributes name options)]
    `(def ~name (make-intent ~@options))))

(defn conducts [intent]
  (-> intent meta ::conducts deref :fmap))

(defn add-conduct [intent dispatch-val dispatch-fn]
  (swap! (::conducts (meta intent))
         #(-> % (assoc-in [:fmap dispatch-val] dispatch-fn)
                (assoc :cache {})))
  intent)

(defn remove-conduct [intent dispatch-val]
  (swap! (::conducts (meta intent))
         #(-> % (update-in [:fmap] dissoc dispatch-val)
                (assoc :cache {})))
  intent)

(defmacro defconduct
  [name dispatch-val & fn-tail]
  `(add-conduct ~name ~dispatch-val (fn ~@fn-tail)))
