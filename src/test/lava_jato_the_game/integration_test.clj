(ns lava-jato-the-game.integration-test
  (:require [clojure.test :refer [deftest]]
            [midje.sweet :refer [fact => just]]
            [lava-jato-the-game.test :as test]))

(deftest index-explorer
  (let [session (test/->session)]
    (fact
      "Check if the index-explorer query returns any data."
      (test/api session `[{[:com.wsscode.pathom.viz.index-explorer/id 0]
                           [:com.wsscode.pathom.viz.index-explorer/index]}])
      => (just {[:com.wsscode.pathom.viz.index-explorer/id 0] (just {:com.wsscode.pathom.viz.index-explorer/index map?})}))))
