(ns capture-input.player)

(defn by-id [id]
  "find an element on the dom by id"
  (.getElementById js/document id))


(def pressed-controls (by-id "output"))
(def player-container (by-id "response"))

(defn set-html! [element source]
  "set the html on a specific element"
  (set! (.-innerHTML element) source))

(defn player-html [x y]
  (str "<div class=\"player\" style=\"left:" x "; top:" y "\"></div>"))


(defn set-position [x y]
  (let [player-container (by-id "response")]
    (set-html! player-container (player-html x y))))


(defn new-position [x y direction]
  (condp = direction
    :jump {:x x :y (+ y 0.1)}
    :left {:x (- x 0.1) :y y}
    :right {:x (+ x 0.1) :y y}))
