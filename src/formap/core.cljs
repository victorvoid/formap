(ns formap.core
    (:require
      [reagent.core :as r]))

(defn home-page []
  [:div [:h2 "Welcome to Reagent"]])

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
