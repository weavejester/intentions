# Intentions

[![Build Status](https://travis-ci.org/weavejester/intentions.svg?branch=master)](https://travis-ci.org/weavejester/intentions)

Intentions are a tool for runtime polymorphism in Clojure and
ClojureScript. They behave much same as multimethods, with one key
exception: where multimethods *override* inherited behavior,
intentions *combine* it.

## Installation

Add the following dependency to your project:

```clojure
[intentions "0.2.1"]
```

## Usage

An intention is created with `defintent`:

```clojure
(defintent valid?
  :dispatch :type
  :combine #(and %1 %2))
```

An intention needs both a dispatch function, and a combine function.
The combine function combines two return values into one. In this
case, we perform a logical AND on the values.

Once an intention is stated, behavior can be added using conducts.
These are analogous to methods:

```clojure
(derive ::square ::quad)

(defconduct valid? ::quad [shape]
  (= (count (:sides shape)) 4))

(defconduct valid? ::square [shape]
  (apply = (:sides shape))
```

Unlike methods, conducts with derived dispatch values combine the
functionality of their parents. Because `::square` derives from
`::quad`, both conducts are applied, then combined using `and`:

```clojure
(valid? {:type ::square, :sides [2 2 2 2]})
-> true

(valid? {:type ::square, :sides [2 2 2]})
-> false
```

Conducts are combined in a fixed order down the inheritance tree, with
parents evaluated before children. So in the above case, the conduct
for `::quad` is evaluated before `::square`. This ordering is
particularly useful when using a combining function like `merge`.

When the inheritance order is ambiguous (such as in the case of a
[diamond dependency][1]), dependencies are converted to strings and
ordered alphanumerically, so as to always provide a consistent
ordering. This can be overridden by using the `prefer-conduct`
function.

For example:

```clojure
;; Diamond dependency graph
(derive ::b ::a)
(derive ::c ::a)
(derive ::d ::b)
(derive ::d ::a)

(defintent order-example
  :dispatch identity
  :combine  concat)

(defconduct ::b [_] '(b))
(defconduct ::c [_] '(c))
```

If we were to dispatch on `::d`, the order in which to apply `::b` and
`::c` is ambiguous, so we default to alphanumeric ordering:

```clojure
(order-example ::d)
-> (b c)
```

To explicit specify an ordering, use `prefer-conduct`:

```clojure
(prefer-conduct order-example ::c ::b)
(order-example ::d)
-> (c b)
```

[1]: https://en.wikipedia.org/wiki/Multiple_inheritance#The_diamond_problem

## Further Documentation

* [API Docs](https://weavejester.github.io/intentions/intentions.core.html)

## License

Copyright © 2016 James Reeves

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
