(ns lava-jato-the-game.user
  (:require [lava-jato-the-game.client :as client]
            [com.fulcrologic.fulcro.application :as fa]))

(defn after-load
  []
  (fa/force-root-render! @client/SPA))
