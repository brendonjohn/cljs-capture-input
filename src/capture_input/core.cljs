(ns capture-input.core
  (:require [cljs.core.async :refer [put! chan <! >! alts!] :as async]
            [goog.events :as events]
            [goog.events.KeyCodes :as KeyCodes]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go alt! go-loop]]))

(enable-console-print!)

(def player-controls {:jump KeyCodes/SPACE
                      :left KeyCodes/LEFT
                      :right KeyCodes/RIGHT})

(def app-state (atom {:controller {:left :off
                                   :right :off
                                   :jump :off}}))


(defn key-specific
  "Create a callback fn that can be used by events/listen"
  [keycode channel status]
  (fn [e]
    (when (= (.-keyCode e) (player-controls keycode))
      (put! channel status))))


(defn key-channel
  "Construct a channel that will say when a specific key is being pressed"
  [keycode]
  (let [channel (chan)]
    (events/listen js/window "keydown" (key-specific keycode channel :on))
    (events/listen js/window "keyup" (key-specific keycode channel :off))
    channel))


(defn update-key
  "create a fn that's used for updating the state of the player controller"
  [key-pressed key-direction]
  (fn [controller-state]
    (update-in controller-state
               [:controller key-pressed] (fn [_] key-direction))))


(defn controller-keys
  "construct a string that specifies the state of the controller keys"
  [controller]
  (str "Left: " (:left controller) ", Right: " (:right controller) ", Jump: " (:jump controller)))


(defn controller-view
  "view for displaying what the player controller is doing"
  [state owner]
  (reify
    om/IRender
    (render [_]
            (dom/div nil
                     (dom/p nil (controller-keys (:controller state)))))))


(om/root controller-view app-state
         {:target (. js/document (getElementById "controller"))})


;continually respond to left, right and space channels for the controller
(let [left-chan (key-channel :left)
      right-chan (key-channel :right)
      jump-chan (key-channel :jump)]
  (go (while true
        (let [[v c] (alts! [left-chan right-chan jump-chan])]
          (condp = c
            left-chan (swap! app-state (update-key :left v))
            right-chan (swap! app-state (update-key :right v))
            jump-chan (swap! app-state (update-key :jump v)))))))
