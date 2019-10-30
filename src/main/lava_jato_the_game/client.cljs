(ns lava-jato-the-game.client
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.application :as fa]
            [goog.dom :as gdom]
            [goog.object :as gobj]
            [com.fulcrologic.fulcro.networking.http-remote :as fnh]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.routing.legacy-ui-routers :as fr]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [com.fulcrologic.fulcro.mutations :as fm]))

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
    (dom/code id)
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
    (dom/code id)
    (ui-player player)
    (ui-party party)))

(def ui-character (comp/factory Character {:keyfn :character/id}))

(fm/defmutation player/login
  [_]
  (action [{:keys [state]}]
          (swap! state (fn [st]
                         (prn [:st st])
                         (assoc-in st [::login ::login :ui/loading?] true))))
  (remote [env]
          (fm/returning env Player)))

(defsc Home [this {::keys   [page id]
                   :ui/keys [profile]}]
  {:query         [::page
                   ::id
                   {:ui/profile (comp/get-query Character)}]
   :ident         (fn [] [page id])
   :initial-state (fn [_] {::page ::home
                           ::id   ::home})}
  (dom/div
    (dom/button {:onClick #(df/load! this :lava-jato-the-game.api/me Character
                                     {:target [::home ::home :ui/profile]})}
                "load")
    (when profile
      (ui-character profile))))

(defsc Login [this {:player/keys [username password]
                    :ui/keys     [loading?]
                    ::keys       [page id]}]
  {:query         [::page
                   :player/username
                   :player/password
                   :ui/loading?
                   ::id]
   :ident         (fn [] [page id])
   :initial-state {::page           ::login
                   ::id             ::login
                   :player/username ""
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

(fr/defsc-router RootRouter [this props]
  {:router-id      :top-router
   :ident          (fn [] [(::page props)
                           (::id props)])
   :router-targets {::login Login
                    ::home  Home}
   :default-route  Home}
  "404")

(def ui-root-router (comp/factory RootRouter))

(defsc Root [this {::keys [root-router]}]
  {:query         [{::root-router (comp/get-query RootRouter)}]
   :initial-state (fn [_]
                    {::root-router (comp/get-initial-state RootRouter _)})}
  (comp/fragment
    (ui-root-router root-router)))

(defonce SPA (atom nil))

(defn ^:export main
  []
  (let [csrf-token (-> (gdom/getDocument)
                       (gobj/getValueByKeys "body" "dataset" "csrfToken"))
        app (fa/fulcro-app
              {:remotes {:remote (fnh/fulcro-http-remote {:request-middleware (-> (fnh/wrap-csrf-token csrf-token)
                                                                                  (fnh/wrap-fulcro-request))})}})]
    (fa/mount! app Root "app")
    (reset! SPA app)))


