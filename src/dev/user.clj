(ns user
  (:require [shadow.cljs.devtools.api :as shadow.api]
            [shadow.cljs.devtools.server :as shadow.server]
            [lava-jato-the-game.server :as server]
            [io.pedestal.http :as http]
            [com.wsscode.pathom.gen :as pgen]
            [io.pedestal.http.route :as route]
            [ring.util.mime-type :as mime]
            [hiccup.core :as h]
            [io.pedestal.http.csrf :as csrf]
            [cognitect.transit :as transit]
            [clojure.spec.alpha :as s]))

(s/def :character/id uuid?)
(s/def :character/name string?)
(s/def :player/id uuid?)
(s/def :player/name string?)
(s/def :party/id uuid?)
(s/def :party/name string?)
(s/def :party/description string?)


(defn api-gen
  [{:keys [transit-params]}]
  (let [result (pgen/query->props transit-params)]
    {:body   (fn [out]
               (transit/write
                 (transit/writer out :json-verbose)
                 result))
     :status 200}))

(defn workspaces
  [{::csrf/keys [anti-forgery-token]}]
  {:body    (h/html [:html {:lang "pt-BR"}
                     [:head
                      [:meta {:charset "UTF-8"}]
                      [:title "Workspaces"]]
                     [:body
                      {:data-csrf-token anti-forgery-token}
                      [:div
                       {:id "app"}]
                      [:script {:src "/js/workspaces/main.js"}]]])
   :headers {"Content-Security-Policy" ""
             "Cache-Control"           "no-store"
             "Content-Type"            (mime/default-mime-types "html")}
   :status  200})


(defonce http-state (atom nil))

(defn -main
  {:shadow/requires-server true}
  [& _]
  (shadow.server/start!)
  (shadow.api/watch :client)
  (shadow.api/watch :workspaces)
  (swap! http-state (fn [st]
                      (when st
                        (http/stop st))
                      (-> server/service
                          (assoc :env :dev
                                 ::http/join? false
                                 ::http/routes (fn []
                                                 (route/expand-routes (conj server/routes
                                                                            `["/workspaces" :get workspaces]
                                                                            `["/api-gen" :post api-gen])))
                                 ::http/file-path "target/public")
                          server/default-interceptors
                          http/dev-interceptors
                          http/create-server
                          http/start))))
