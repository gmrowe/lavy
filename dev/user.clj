(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [kaocha.repl :as k]))


(defn run-tests [] (refresh) (k/run-all))

