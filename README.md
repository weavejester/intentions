# Intentions

Intentions are a tool for runtime polymorphism and inheritance in
Clojure, and are broadly similar to multimethods. While multimethods
dispatch only once to the best matching method, intentions dispatch
for all matches, then collate the results using a combining function.

## Installation

Add the following dependency to your project:

```clojure
[intentions "0.1.0-SNAPSHOT"]
```

## Usage

An intention is created with `defintent`:

```clojure
(defintent valid?
  :dispatch :type
  :combine #(and %1 %2))
```

An intention needs both a dispatch function, and a combine function.

Once an intention is stated, behavior can be added using conducts.
These are analogous to methods:

```clojure
(derive ::square ::quad)

(defconduct valid? ::quad [shape]
  (= (count (:sides shape)) 4))

(defconduct valid? ::square [shape]
  (apply = (:sides shape))
```

Unlike methods, conducts with derived dispatch values inherit
functionality of their parents. Because `::square` derives from
`::quad`, both conducts are used when validating squares:

```clojure
(valid? {:type ::square, :sides [2 2 2 2]})
-> true

(valid? {:type ::square, :sides [2 2 2]})
-> false
```

## License

Copyright © 2014 James Reeves

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
