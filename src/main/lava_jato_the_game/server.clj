(ns lava-jato-the-game.server
  (:require [io.pedestal.http :as http]
            [ring.util.mime-type :as mime]
            [hiccup.core :as h]
            [cognitect.transit :as transit]
            [com.wsscode.pathom.core :as p]
            [com.wsscode.pathom.connect :as pc]
            [io.pedestal.http.csrf :as csrf])
  (:import (java.io ByteArrayOutputStream)))

(pc/defresolver index-explorer [{::pc/keys [indexes]} _]
  {::pc/input  #{:com.wsscode.pathom.viz.index-explorer/id}
   ::pc/output [:com.wsscode.pathom.viz.index-explorer/index]}
  {:com.wsscode.pathom.viz.index-explorer/index (p/transduce-maps
                                                  (remove (comp #{::pc/resolve ::pc/mutate}
                                                                key))
                                                  indexes)})


(pc/defresolver me [this props]
  {::pc/output [:lava-jato-the-game.api/me]}
  {:lava-jato-the-game.api/me {:character/id     0
                               :character/name   "Me"
                               :character/player {:player/id   1
                                                  :player/name "my player"}
                               :character/party  {:party/id          2
                                                  :party/name        "my party"
                                                  :party/description "party description"}}})


(def register
  [me index-explorer])

(def parser
  (p/parser
    {::p/env     {::p/reader               [p/map-reader
                                            pc/reader2
                                            pc/open-ident-reader
                                            pc/index-reader
                                            p/env-placeholder-reader]
                  ::p/placeholder-prefixes #{">"}}
     ::p/mutate  pc/mutate
     ::p/plugins [(pc/connect-plugin {::pc/register register})
                  p/error-handler-plugin
                  p/elide-special-outputs-plugin
                  p/trace-plugin]}))

(defn pr-transit!
  [out type data]
  (transit/write
    (transit/writer out type)
    data)
  out)

(defn pr-transit-str
  [type data]
  (-> (ByteArrayOutputStream.)
      (pr-transit! type data)
      (str)))

(defn api
  [{:keys [transit-params]
    :as   req}]
  (let [result (parser req transit-params)]
    {:body   (fn [out]
               (pr-transit! out :json-verbose result))
     :status 200}))

(defn index
  [{::csrf/keys [anti-forgery-token]}]
  {:body    (h/html
              [:html {:lang "pt-BR"}
               [:head
                [:meta {:charset "UTF-8"}]
                [:title "Lava Jato - The Game"]
                [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
                [:meta {:name "theme-color" :content "white"}]
                [:meta {:name "Description" :content "Lava Jato - The Game (Tales Of Corruption)"}]
                [:meta {:property "og:type" :content "game"}]
                [:meta {:property "og:title" :content "Lava Jato - The Game"}]
                [:meta {:property "og:description" :content "Lava Jato - The Game (Tales Of Corruption)"}]
                [:meta {:name "twitter:card" :content "summary"}]
                [:link {:rel "icon" :href "data:image/svg+xml,%3Csvg%20xmlns=%22http://www.w3.org/2000/svg%22%3E%3C/svg%3E" :type "image/svg+xml"}]
                [:title "Lava Jato"]
                [:link {:rel "stylesheet" :href "/main.css"}]]
               [:body
                {:data-csrf-token anti-forgery-token
                 :onload          "lava_jato_the_game.client.main()"}
                [:div {:id "app"}]
                [:script {:src "/js/client/main.js"}]]])
   :headers {"Content-Security-Policy" ""
             "Cache-Control"           "no-store"
             "Content-Type"            (mime/default-mime-types "html")}
   :status  200})

(def routes
  `#{["/" :get index]
     ["/api" :post api]})

(def service
  {:env                 :prod
   ::http/type          :jetty
   ::http/port          8080
   ::http/resource-path "public"
   ::http/enable-csrf   {}
   ::http/routes        routes})

(defn default-interceptors
  [service-map]
  (http/default-interceptors service-map))