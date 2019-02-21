(ns user
  (:require [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.pedestal :as lacinia.pedestal]
            [com.walmartlabs.lacinia.schema :as lacinia.schema]
            [com.walmartlabs.lacinia.parser.schema :as lacinia.parser.schema]
            [io.pedestal.http :as http]
            [shadow.cljs.devtools.server :as server]
            [shadow.cljs.devtools.api :as shadow]
            [com.rpl.specter :as sp]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]))
(def type->gen
  {'ID     (s/gen uuid?)
   'Int    (s/gen integer?)
   'String (s/gen string?)})

(defn add-gen
  [x]
  #_(clojure.pprint/pprint x)
  (sp/transform (sp/walker :type)
                (fn [{:keys [type] :as f}]
                  (let [many? (seq? type)
                        type-id (if many? (last type) type)
                        generator (or (get type->gen type-id)
                                      (when (keyword? type-id)
                                        (gen/hash-map :id (s/gen uuid?)))
                                      (throw (ex-info (pr-str f) f)))
                        generator (if many?
                                    (gen/vector generator)
                                    generator)]
                    (assoc f :resolve (fn [_ _ _]
                                        (let [data (gen/generate generator)]
                                          data)))))
                x))

(defn schema
  [f]
  (-> (slurp f)
      (lacinia.parser.schema/parse-schema {})
      add-gen
      (lacinia.schema/compile)))

(defonce http-state (atom nil))

(defonce shadow-server
         (delay (server/start!)))

(defn -main
  [& args]
  (prn [@shadow-server])
  (shadow/watch :client)
  (shadow/watch :workspaces)
  (swap! http-state (fn [x]
                      (when x
                        (http/stop x))
                      (-> (lacinia.pedestal/service-map #(schema (io/resource "schema.graphql"))
                                                        {:graphiql true})
                          (assoc ::http/allowed-origins ["http://localhost:8080"])
                          http/default-interceptors
                          http/dev-interceptors
                          http/create-server
                          http/start)))
  nil)

(def context
  {})

(defn q
  [& [query variables options]]
  (lacinia/execute (schema (io/resource "schema.graphql"))
                   query variables context options))
