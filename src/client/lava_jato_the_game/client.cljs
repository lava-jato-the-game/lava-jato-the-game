(ns lava-jato-the-game.client
  (:require [goog.dom :as gdom]
            [fulcro.client.primitives :as fp]
            [fulcro.client :as fc]
            [com.wsscode.pathom.fulcro.network :as pfn]
            [fulcro.client.dom :as dom]
            [fulcro.client.routing :as fr]
            [fulcro.client.data-fetch :as df]
            [fulcro.client.mutations :as fm]))

(fp/defsc Player [this {:player/keys [id name]}]
  {:ident [:player/id :player/id]
   :query [:player/id
           :player/name]}
  (dom/div
    (dom/hr)
    (dom/code "Player name: ") (dom/span name)
    (dom/br)
    (dom/code "ID: " (dom/code (str id)))
    (dom/hr)))




(def ui-player (fp/factory Player {:keyfn :player/id}))

(fp/defsc Party [this {:party/keys [id name description]}]
  {:query [:party/id
           :party/name
           :party/description]
   :ident [:party/id :party/id]}
  (dom/div
    (dom/h2 name)
    (dom/code id)
    (dom/div description)))

(def ui-party (fp/factory Party {:keyfn :party/id}))

(fp/defsc Character [this {:character/keys [id name player party]}]
  {:query [:character/id
           :character/name
           {:character/player (fp/get-query Player)}
           {:character/party (fp/get-query Party)}]
   :ident [:character/id :character/id]}
  (dom/div
    (dom/h2 name)
    (dom/code id)
    (ui-player player)
    (ui-party party)))

(def ui-character (fp/factory Character {:keyfn :character/id}))

(fm/defmutation player/login
  [_]
  (action [{:keys [state]}]
          (swap! state (fn [st]
                         (prn [:st st])
                         (assoc-in st [::login ::login :ui/loading?] true))))
  (remote [{:keys [ast state]}]
          (-> ast
              (fm/returning state Player))))

(fp/defsc Home [this {::keys   [page id]
                      :ui/keys [profile]}]
  {:query         [::page
                   ::id
                   {:ui/profile (fp/get-query Character)}]
   :ident         (fn [] [page id])
   :initial-state (fn [_] {::page ::home
                           ::id   ::home})}
  (dom/div
    (dom/button {:onClick #(df/load this :query/profile Character
                                    {:target [::home ::home :ui/profile]})}
                "load")
    (when profile
      (ui-character profile))))

(fp/defsc Login [this {:player/keys [username password]
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
  (let [on-login #(fp/transact! this `[(player/login ~{:username username
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

(def ui-root-router (fp/factory RootRouter))

(fp/defsc Root [this {::keys [root-router]}]
  {:query         [{::root-router (fp/get-query RootRouter)}]
   :initial-state (fn [_]
                    {::root-router (fp/get-initial-state RootRouter _)})}
  (fp/fragment
    (ui-root-router root-router)))

(defonce app (atom nil))

(defn render!
  []
  (let [target (gdom/getElement "app")]
    (swap! app fc/mount Root target)))

(defn ^:export main
  []
  (let [client (fc/make-fulcro-client
                 {:networking {:remote (pfn/graphql-network "http://localhost:8888/graphql")}})]
    (reset! app client)
    (render!)
    client))
