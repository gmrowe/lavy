(ns lavy.main
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [clojure.string :as str]))

(defn exec-command
  [command rdr]
  (cond
    (= command :count-bytes)(-> (.getBytes (slurp rdr)) seq count)
    (= command :count-lines) (->> (.getBytes (slurp rdr))
                                  seq
                                  (filter #(= (byte \newline) %))
                                  count)))
(defn parse-arg
  [arg]
  ({"-c" :count-bytes
    "-l" :count-lines} arg))

(defn run
  [command-arg file-path]
  (with-open [rdr (io/input-stream file-path)]
    (let [result (exec-command (parse-arg command-arg) rdr)]
      (printf "%d %s%n" result file-path))))

(def usage
  (str/join
   \newline
   ["Usage: lavy [-c] filename"
    "    -c: Count the number of bytes in the supplied file"
    ""]))

(defn -main
  [& args]
  (if (< (count args) 2)
    (print usage)
    (run (first args) (second args))))

