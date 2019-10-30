(ns lava-jato-the-game.ui-cards
  (:require [nubank.workspaces.card-types.fulcro3 :as f3]
            [lava-jato-the-game.client :as client]
            [nubank.workspaces.core :as ws]))

(ws/defcard fulcro-demo-card
  (f3/fulcro-card
    {::f3/root          client/Character
     ::f3/initial-state {:character/id     0
                         :character/name   "Me"
                         :character/player {:player/id   1
                                            :player/name "my player"}
                         :character/party  {:party/id          2
                                            :party/name        "my party"
                                            :party/description "party description"}}}))
