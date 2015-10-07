(ns ^:figwheel-always note-app.core
    (:require
      [cljs.reader :refer [read-string]]
      [reagent.core :as r ]))

(enable-console-print!)

;;;; State

(defonce state
         (r/atom
           (if-let [ls (js/localStorage.getItem "todoList")]
             (read-string ls)
             {:show-note-form false
              :note-form-idx nil
              :list []})))

(add-watch state :localStorage (fn [key ref old-state new-state]
                                 (js/localStorage.setItem "todoList" (pr-str new-state))))

;;;; Helper functions

(defn- remove-idx [v idx]
  (let [arr (clj->js v)]
    (.splice arr idx 1)
    (js->clj arr)))

(defn show-note-form! []
  (swap! state assoc :show-note-form true))

(defn hide-note-form! []
  (swap! state assoc :show-note-form false))


;;;; Add/remove/update note functions

(defn create-note! [note-content]
  (let [new-list (conj (:list @state) note-content)]
    (swap! state assoc :list new-list))
  (hide-note-form!))

(defn update-note! [idx note]
  (hide-note-form!)
  (swap! state assoc :note-form-idx nil)
  (swap! state update :list #(assoc % idx note)))

(defn remove-note-from-list! [idx]
  (swap! state update :list remove-idx idx))

;;;; Handler functions

(defn handle-add-note []
  (show-note-form!))

(defn handle-edit-note [idx]
  (show-note-form!)
  (swap! state assoc :note-form-idx idx))

(defn handle-remove-note [idx]
  (remove-note-from-list! idx))

(defn handle-note-form-on-submit [e idx]
  (let [note (.-target.previousSibling.value e)]
    (if idx
      (update-note! idx note)
      (create-note! note))))

;;;; Render functions

(defn build-list []
  (map-indexed
    (fn [idx note]
      [:li {:key idx
            :class "note"}
       [:div note]
       [:button {:class "edit-button"
                 :on-click #(handle-edit-note idx)}
        "Edit"]
       [:button {:class "delete-button"
                 :on-click #(handle-remove-note idx)}
        "Delete"]])
    (:list @state)))

;;;; Components

(defn NewOrEditNote []
  (let [note-form? (:show-note-form @state)
        note-idx (:note-form-idx @state)]
    (if note-form?
      [:div {:class "note-form"}
       [:textarea (if note-idx
                    {:default-value (nth (:list @state) note-idx)}
                    {:placeholder "New note"})]
       [:button {:class "done-button"
                 :on-click #(handle-note-form-on-submit % note-idx )}
        "Done"]]
      [:div])))

(defn NoteList []
  [:div
   [:h1 "Todo list"]
   [:ul {:class "note-list"}
    (build-list)]
   [:button {:class "add-note-button"
             :on-click handle-add-note}
    "+"]
   [NewOrEditNote]])


;;;; Run it!

(r/render-component
  [NoteList]
  (js/document.getElementById "app"))

