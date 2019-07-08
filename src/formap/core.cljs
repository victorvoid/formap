(ns formap.core
    (:require
      [reagent.core :as r]))

(defn get-error-messages
  "Returns array of errors from validators executed in `value`."
  [validators value]
  (reduce (fn [acc validator]
            (let [fn-validator (:validate validator)
                  valid?       (fn-validator value)]

              (if valid?
                acc
                (conj acc (:message validator)))))

          [] validators))

(defn some-field-touched?
  "Returns true if some of the `fields` have been touched."
  [fields]
  (some (fn [[_ value]]
          (:touched value)) fields))

(defn all-fields-valid?
  "Returns true if all `fields` are valid."
  [fields]
  (every? (fn [[_ value]]
            (:valid value)) fields))

(def default-metas
  {:touched false :valid false :errors []})

(defn get-default-meta
  "Returns a default meta object according to `fields` name.
    {:field-name {:touched false :valid false :errors []}}
  "
  [fields]
  (reduce (fn [acc field]
            (let [name (keyword (:name field))]
              (merge acc {name default-metas}))) {} fields))

(defn get-field-meta
  "Returns a meta object with dynamic values according to the
   validations executed in `form-state`."
  [form-state name validators required?]
  (let [value  (get (:state @form-state) name)
        errors (if required?
                 (or
                   (when (empty? value) [required?])
                   (get-error-messages validators value)))

        valid? (empty? errors)
        field-meta {:errors errors :valid valid? :touched true}]
    field-meta))

(defn get-input-class
  "Returns an array of class name according to the :meta from `form-state`.
    :valid/:invalid and :touched/:untouched"
  [name form-state]
  (let [valid?   (if (get-in @form-state [:meta name :valid]) :valid :invalid)
        touched? (if (get-in @form-state [:meta name :touched]) :touched :untouched)]
    [valid? touched?]))

(defn on-blur
  "Update :meta from `form-state` according to the validations."
  [_ form-state {:keys [name validators required]}]
  (let [field-meta (get-field-meta form-state name validators required)]
    (swap! form-state assoc-in [:meta name] field-meta)))

(defn on-change
  "Update :state from `form-state` to input value and
   running validations when form is touched."
  [e form-state {:keys [name validators required]}]
  (swap! form-state assoc-in [:state name] (-> e .-target .-value))
  (let [form-touched? (some-field-touched? (:meta @form-state))
        new-meta      (get-field-meta form-state name validators required)]
    (when form-touched? (swap! form-state assoc-in [:meta name] new-meta))))

(defn input [attrs form-state field]
  (fn []
    (let [name         (:name attrs)
          meta-classes (get-input-class name form-state)
          errors       (get-in @form-state [:meta name :errors])]

      [:div.field {:class meta-classes}
       [:div.field-input
        [:input.input (merge attrs {:on-change #(on-change % form-state field)
                                    :value (get-in @form-state [:state name])})]]
       [:div.error-wrapper
        [:p (first errors)]]])))

(defn get-custom-elements
  "Returns a collection of customized elements according to `fields`."
  [fields form-state]
  (reduce (fn [acc field]
            (let [name         (keyword (:name field))
                  attrs        (dissoc field :name :validators)
                  type         (get attrs :type "text")
                  new-field    (assoc field :name name)
                  custom-attrs {:type type
                                :key name
                                :name name
                                :on-blur #(on-blur % form-state new-field)}
                  all-attrs    (merge attrs custom-attrs)
                  element      [input all-attrs form-state new-field]]

              (conj acc element))) [] fields))

(defn build-form [{:keys [on-submit experience class]} children]
  "Render a form according to the `experience`.
    Example:

    (def experience {:fields [{:name 'name'
                               :placeholder 'Type your name'
                               :required 'Name is required.'}]})
    [cljs-form {:experience experience
                :class 'myform'
                :on-submit #(js/console.log %)}
      [:button 'Send form']]"

  (let [fields (:fields experience)
        form-state (r/atom {:state {} :meta (get-default-meta fields)})
        elements   (get-custom-elements fields form-state)
        on-submit  (fn [e] (.preventDefault e) (on-submit (:state @form-state)))]

    (fn []
      (let [form-valid?   (all-fields-valid? (:meta @form-state))
            form-touched? (some-field-touched? (:meta @form-state))
            form-class    (str (if form-valid? " valid" " invalid")
                               (if form-touched? " touched" " untouched"))]

        [:form {:on-submit on-submit
                :class (str class form-class)}

         (for [item elements]
           ^{:key item} item)
         children]))))