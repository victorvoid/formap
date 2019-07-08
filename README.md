# Formap
A reagent library to build awesome dynamic forms. 🔨

[![Clojars Project](http://clojars.org/formap/latest-version.svg)](https://clojars.org/formap)


## Installation

To use formap in an existing project you add this to your dependencies in project.clj

```
[formap "0.x.x"]
```

## Why ?

The main objective is to build a form by a literal map that describe all fields.

- ♥️ Building form using literal map.
- 🔫 Validators support.
- ⚡️ Meta class in fields (touched|untouched|valid|invalid|etc).

## Documentation

First you need create your literal map that describe a form and use it for build.


```cljs
(ns fluany.pages.signin
  (:require
   [reagent.core :as r]
   [app.utils.validators :refer [username-or-email? password?]]
   [formap.core :refer [build-form]]))


(def signin-fields
  {:fields [{:name "login"
             :placeholder "Username or Email"
             :class "input"
             :autoFocus true
             :required "Username or Email is required"
             :validators [username-or-email?]}

            {:name "password"
             :placeholder "Password"
             :type "password"
             :required "Password is required"
             :validators [password?]}]})

(defn login []
  [build-form {:experience signin-fields
               :class "myform"
               :on-submit #(js/console.log %)} ;;{:login "Text typed" :password "Password typed"}
    [:button "Sign in"]])
```

### Validators
You can use validators and set a message error.

```cljs
(ns fluany.pages.signin
  (:require
   [reagent.core :as r]
   [formap.core :refer [build-form]]))

(defn- match-regex?
  "Check if the string matches the regex"
  [v regex]
  (boolean (re-matches regex v)))
  
(defn username-validate
  [input]
  (if (or (nil? input) (empty? input))
    true
    (match-regex? input #"^([a-zA-Z0-9.]+@){0,1}([a-zA-Z0-9.])+$")))

(def username?
  {:validate username-validate
   :message "Username invalid."})

(def signin-fields
  {:fields [{:name "login"
             :placeholder "Username"
             :class "input"
             :autoFocus true
             :required "Username is required"
             :validators [username?]}

            {:name "password"
             :placeholder "Password"
             :type "password"
             :required "Password is required"}]})

(defn login []
  [build-form {:experience signin-fields
               :class "myform"
               :on-submit #(js/console.log %)}
    [:button "Sign in"]])
```
