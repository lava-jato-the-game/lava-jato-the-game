(ns lava-jato-the-game.user
  (:require [lava-jato-the-game.client :as client]
            [devtools.preload]
            [fulcro.inspect.preload]))

(defn after-load
  []
  (client/render!))
