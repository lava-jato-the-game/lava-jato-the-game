(ns lava-jato-the-game.ui-cards
  (:require [nubank.workspaces.card-types.fulcro3 :as f3]
            [lava-jato-the-game.client :as client]
            [nubank.workspaces.core :as ws]
            [goog.dom :as gdom]
            [goog.object :as gobj]
            [com.fulcrologic.fulcro.networking.http-remote :as fnh]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [com.wsscode.pathom.gen :as pgen]
            [com.fulcrologic.fulcro.components :as comp]))

(def csrf-token (-> (gdom/getDocument) (gobj/getValueByKeys "body" "dataset" "csrfToken")))

(def remote-gen (fnh/fulcro-http-remote {:url                "/api-gen"
                                         :request-middleware (-> (fnh/wrap-csrf-token csrf-token)
                                                                 (fnh/wrap-fulcro-request))}))


(ws/defcard fulcro-demo-card
  (f3/fulcro-card
    {::f3/root client/Character
     ::f3/app  {:remotes          {:remote remote-gen}
                :client-did-mount (fn [app]
                                    (df/load! app :>/root client/Character
                                              {:target [:ui/root]}))}}))

