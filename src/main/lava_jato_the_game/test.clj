(ns lava-jato-the-game.test
  (:require [hickory.core :as hickory]
            [io.pedestal.test :refer [response-for]]
            [lava-jato-the-game.server :as server]
            [io.pedestal.http :as http]
            [cognitect.transit :as transit]
            [clojure.java.io :as io]))

(defn ->session
  [& {:as _}]
  (let [{::http/keys [service-fn]
         :as         app} (-> server/service
                              server/default-interceptors
                              http/dev-interceptors
                              http/create-servlet)
        {:keys [body headers]} (response-for service-fn :get "/")
        cookie (-> headers (get "Set-Cookie") first)
        x-csrf-token (->> body
                          hickory/parse
                          hickory/as-hickory
                          :content
                          first
                          :content
                          (keep (fn [{:keys [type tag attrs]}]
                                  (when (and (= type :element) (= tag :body))
                                    (:data-csrf-token attrs))))
                          first)]
    (assoc app
      ::headers {"Cookie"       cookie
                 "Content-Type" "application/transit+json"
                 "X-CSRF-Token" x-csrf-token})))

(defn api
  [{::http/keys [service-fn]
    ::keys      [headers]} eql]
  (let [{:keys [body]} (response-for service-fn :post "/api"
                                     :body (server/pr-transit-str :json-verbose eql)
                                     :headers headers)]
    (transit/read (transit/reader (io/input-stream (.getBytes body))
                                  :json-verbose))))
