(ns intentions.core
  "Macros and functions for defining intentions."
  #?(:clj (:require [clojure.tools.macro :as macro])))

(defn intent?
  "Returns true if its argument is an intention."
  [x]
  (and (fn? x) (::state (meta x))))

(defn- dispatch-comparator [isa? prefers]
  (fn [a b]
    (cond
     (or (isa? a b) (prefers [a b])) 1
     (or (isa? b a) (prefers [b a])) -1
     :else (compare (str a) (str b)))))

(defn- find-conducts [state dispatch-val isa?]
  (->> (:conducts state)
       (filter #(isa? dispatch-val (key %)))
       (sort-by key (dispatch-comparator isa? (:prefers state)))
       (mapv val)))

(defn- combine-conducts [combine fs]
  (fn [& args]
    (reduce combine (map #(apply % args) fs))))

(defn- conduct-fn [combine default state dispatch-val isa?]
  (if-let [fs (seq (find-conducts state dispatch-val isa?))]
    (combine-conducts combine fs)
    (get-in state [:conducts default])))

(defn- cached-conduct-fn [combine default state dispatch-val isa?]
  (let [st @state]
    (or (get-in st [:cache dispatch-val])
        (let [f (conduct-fn combine default st dispatch-val isa?)]
          (swap! state assoc-in [:cache dispatch-val] f)
          f))))

(defn- no-conduct-error [dispatch-val]
  (#?(:clj IllegalArgumentException. :cljs js/Error.)
   (str "No conduct found for dispatch value: " dispatch-val)))

(defn make-intent
  "Create an anonymous intention. See: [[defintent]]."
  [& {:keys [dispatch combine default hierarchy]
      :or   {default :default}}]
  {:pre  [(ifn? dispatch) (ifn? combine)]
   :post [(intent? %)]}
  (let [state (atom {:conducts {} :cache {} :prefers #{}})
        isa?  (if hierarchy #(isa? hierarchy %1 %2) isa?)
        func  (fn [& args]
                (let [dv (apply dispatch args)]
                  (if-let [f (cached-conduct-fn combine default state dv isa?)]
                    (apply f args)
                    (throw (no-conduct-error dv)))))]
    (with-meta func
      (assoc (meta func) ::state state))))

#?(:clj (defmacro defintent
  "Create a new intention with the associated options. The docstring and
  attribute map are optional.

  Options are key-value pairs and may be one of:

  :dispatch
  : the dispatch function, required

  :combine
  : the combine function, required

  :default
  : the default dispatch value, defaults to `:default`

  :hierarchy
  : the isa? hierarchy to use for dispatching, defaults to the global hierarchy"
  {:arglists '([name docstring? attr-map? & options])}
  [name & options]
  (let [[name options] (macro/name-with-attributes name options)]
    `(defonce ~name (make-intent ~@options)))))

(defn conducts
  "Returns a map of dispatch values to conduct functions for an intention."
  [intent]
  (-> intent meta ::state deref :conducts))

(defn add-conduct
  "Adds a conduct function to an intention for the supplied dispatch value.
  See: [[defconduct]]."
  [intent dispatch-val conduct-fn]
  (swap! (::state (meta intent))
         #(-> % (assoc-in [:conducts dispatch-val] conduct-fn)
                (assoc :cache {})))
  intent)

(defn remove-conduct
  "Removes a conduct function from an intention for the supplied dispatch
  value."
  [intent dispatch-val]
  (swap! (::state (meta intent))
         #(-> % (update-in [:conducts] dissoc dispatch-val)
                (assoc :cache {})))
  intent)

(defn prefer-conduct
  "Causes an intention to evaluate conducts for `dispatch-val-x` after
  `dispatch-val-y`, when neither dispatch value is more specific."
  [intent dispatch-val-x dispatch-val-y]
  (swap! (::state (meta intent))
         #(-> % (update-in [:prefers] disj [dispatch-val-y dispatch-val-x])
                (update-in [:prefers] conj [dispatch-val-x dispatch-val-y])
                (assoc :cache {})))
  intent)

#?(:clj (defmacro defconduct
  "Creates and adds a new conduct function, associated with `dispatch-val`,
  to the supplied intention."
  [intent dispatch-val & fn-tail]
  `(add-conduct ~intent ~dispatch-val (fn ~@fn-tail))))
