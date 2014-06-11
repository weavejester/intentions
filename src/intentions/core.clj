(ns intentions.core
  "Macros and functions for defining intentions."
  (:require [clojure.tools.macro :as macro]))

(defn intent?
  "Returns true if the object is an intention."
  [x]
  (and (fn? x) (::conducts (meta x))))

(defn make-intent
  "Create an anonymous intention. See: defintent."
  [& {:keys [dispatch combine default hierarchy]
      :or   {default :default}}]
  {:pre  [(ifn? dispatch) (ifn? combine)]
   :post [(intent? %)]}
  (let [conducts (atom {:fmap {} :cache {} :prefers #{}})
        isa? (if hierarchy #(isa? hierarchy %1 %2) isa?)
        func (fn [& args]
               (let [con @conducts
                     dv (apply dispatch args)
                     fs (or ((:cache con) dv)
                            (let [pref (:prefers con)
                                  cmp (fn [a b]
                                        (cond
                                         (or (isa? a b) (pref [a b])) 1
                                         (or (isa? b a) (pref [b a])) -1
                                         :else (compare (str a) (str b))))
                                  fs (->> (:fmap con)
                                          (filter #(isa? dv (key %)))
                                          (sort-by key cmp)
                                          (mapv val))]
                              (swap! conducts assoc-in [:cache dv] fs)
                              fs))]
                 (if (seq fs)
                   (loop [ret (apply (first fs) args), fs (rest fs)]
                     (if (seq fs)
                       (recur (combine ret (apply (first fs) args)) (rest fs))
                       ret))
                   (if-let [f ((:fmap con) default)]
                     (apply f args)
                     (throw (IllegalArgumentException.
                             (str "No conduct found for dispatch value: " dv)))))))]
    (with-meta func
      (assoc (meta func) ::conducts conducts))))

(defmacro defintent
  "Create a new intention with the associated options. The docstring and
  attribute map are optional.

  Options are key-value pairs and may be one of:
    :dispatch   the dispatch function, required
    :combine    the combine function, required
    :default    the default dispatch value, defaults to :default
    :hierarchy  the isa? hierarchy to use for dispatching,
                defaults to the global hierarchy"
  {:arglists '([name docstring? attr-map? & options])}
  [name & options]
  (let [[name options] (macro/name-with-attributes name options)]
    `(def ~name (make-intent ~@options))))

(defn conducts
  "Returns a map of dispatch values to conduct functions for an intention."
  [intent]
  (-> intent meta ::conducts deref :fmap))

(defn add-conduct
  "Adds a conduct function to an intention for the supplied dispatch value.
  See: defconduct."
  [intent dispatch-val conduct-fn]
  (swap! (::conducts (meta intent))
         #(-> % (assoc-in [:fmap dispatch-val] conduct-fn)
                (assoc :cache {})))
  intent)

(defn remove-conduct
  "Removes a conduct function from an intention for the supplied dispatch
  value."
  [intent dispatch-val]
  (swap! (::conducts (meta intent))
         #(-> % (update-in [:fmap] dissoc dispatch-val)
                (assoc :cache {})))
  intent)

(defn prefer-conduct
  "Causes an intention to evaluate conducts for dispatch-val-x after
  dispatch-val-y, when neither dispatch value is more specific."
  [intent dispatch-val-x dispatch-val-y]
  (swap! (::conducts (meta intent))
         #(-> % (update-in [:prefers] disj [dispatch-val-y dispatch-val-x])
                (update-in [:prefers] conj [dispatch-val-x dispatch-val-y])
                (assoc :cache {}))))

(defmacro defconduct
  "Creates and adds a new conduct function, associated with dispatch-val,
  to the supplied intention."
  [intent dispatch-val & fn-tail]
  `(add-conduct ~intent ~dispatch-val (fn ~@fn-tail)))
