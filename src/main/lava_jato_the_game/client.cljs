(ns lava-jato-the-game.client
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.application :as fa]
            [goog.dom :as gdom]
            [goog.object :as gobj]
            [goog.events :as gevt]
            [goog.history.EventType :as history.EventType]
            [com.fulcrologic.fulcro.networking.http-remote :as fnh]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [com.fulcrologic.fulcro.mutations :as fm]
            [clojure.string :as string])
  (:import (goog.history Html5History)))

(defsc Player [this {:player/keys [id name]}]
  {:ident [:player/id :player/id]
   :query [:player/id
           :player/name]}
  (dom/div
    (dom/hr)
    (dom/code "Player name: ") (dom/span name)
    (dom/br)
    (dom/code "ID: " (dom/code (str id)))
    (dom/hr)))

(def ui-player (comp/factory Player {:keyfn :player/id}))

(defsc Party [this {:party/keys [id name description]}]
  {:query [:party/id
           :party/name
           :party/description]
   :ident [:party/id :party/id]}
  (dom/div
    (dom/h2 name)
    (dom/code (str id))
    (dom/div description)))

(def ui-party (comp/factory Party {:keyfn :party/id}))

(defsc Character [this {:character/keys [id name player party]}]
  {:query [:character/id
           :character/name
           {:character/player (comp/get-query Player)}
           {:character/party (comp/get-query Party)}]
   :ident [:character/id :character/id]}
  (dom/div
    (dom/h2 name)
    (dom/code (str id))
    (ui-player player)
    (ui-party party)))

(def ui-character (comp/factory Character {:keyfn :character/id}))

(fm/defmutation player/login
  [_]
  (action [{:keys [state]}]
          (swap! state (fn [st]
                         (assoc-in st [::login ::login :ui/loading?] true))))
  (remote [env]
          (fm/returning env Player)))

(defsc Home [this {:ui/keys [profile]}]
  {:query         [{:ui/profile (comp/get-query Character)}]
   :ident         (fn [] [::home ::home])
   :route-segment ["home"]
   :initial-state (fn [_] {})}
  (dom/div
    (dom/button {:onClick #(df/load! this :lava-jato-the-game.api/me Character
                                     {:target [::home ::home :ui/profile]})}
                "load")
    (when profile
      (ui-character profile))))

(defsc Login [this {:player/keys [username password]
                    :ui/keys     [loading?]}]
  {:query         [:player/username
                   :player/password
                   :ui/loading?]
   :ident         (fn [] [::login ::login])
   :route-segment ["login"]
   :initial-state {:player/username ""
                   :ui/loading?     false
                   :player/password ""}}
  (let [on-login #(comp/transact! this `[(player/login ~{:username username
                                                         :password password})])]
    (dom/form
      {:onSubmit (fn [e]
                   (.preventDefault e)
                   (on-login))}
      (dom/label "username")
      (dom/input {:value    username
                  :disabled loading?
                  :onChange #(fm/set-value! this :player/username (-> % .-target .-value))})
      (dom/br)
      (dom/label "password")
      (dom/input {:value    password
                  :disabled loading?
                  :type     "password"
                  :onChange #(fm/set-value! this :player/password (-> % .-target .-value))})
      (dom/br)
      (dom/button
        {:disabled loading?
         #_#_:onClick on-login}
        "login"))))

(dr/defrouter RootRouter [this props]
  {:router-targets [Home Login]})

(def ui-root-router (comp/factory RootRouter))

(defsc Root [this {:>/keys [root-router]}]
  {:query         [{:>/root-router (comp/get-query RootRouter)}]
   :initial-state (fn [_]
                    {:>/root-router (comp/get-initial-state RootRouter _)})}
  (ui-root-router root-router))

(defonce SPA (atom nil))

(defn ^:export main
  []
  (let [csrf-token (-> (gdom/getDocument)
                       (gobj/getValueByKeys "body" "dataset" "csrfToken"))
        history (new Html5History)
        client-did-mount (fn [app]
                           (doto history
                             (gevt/listen history.EventType/NAVIGATE #(when-let [token (.-token %)]
                                                                        (dr/change-route app (-> (string/split token #"/")
                                                                                                 rest
                                                                                                 vec))))
                             (.setEnabled true)))
        app (fa/fulcro-app
              {:client-did-mount client-did-mount
               :remotes          {:remote (fnh/fulcro-http-remote {:request-middleware (-> (fnh/wrap-csrf-token csrf-token)
                                                                                           (fnh/wrap-fulcro-request))})}})]
    (fa/mount! app Root "app")
    (reset! SPA app)))


