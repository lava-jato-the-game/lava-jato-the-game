(ns user
  (:require [shadow.cljs.devtools.api :as shadow.api]
            [shadow.cljs.devtools.server :as shadow.server]
            [lava-jato-the-game.server :as server]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [ring.util.mime-type :as mime]
            [hiccup.core :as h]))

(defn workspaces
  [_]
  {:body    (h/html [:html {:lang "pt-BR"}
                     [:head
                      [:meta {:charset "UTF-8"}]
                      [:title "Workspaces"]]
                     [:body
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
                                                 (route/expand-routes (conj server/routes `["/workspaces" :get workspaces])))
                                 ::http/file-path "target/public")
                          server/default-interceptors
                          http/dev-interceptors
                          http/create-server
                          http/start))))
