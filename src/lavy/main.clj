(ns lavy.main
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn exec-command
  [command rdr]
  (cond (= command :count-bytes) (let [text (slurp rdr)]
                                   (count (seq (.getBytes text))))
        (= command :count-lines) (let [text (slurp rdr)]
                                   (count (re-seq #"\r?\n" text)))
        (= command :count-words) (let [text (slurp rdr)]
                                   (count (str/split text #"\s+")))
        (= command :count-chars) (let [text (slurp rdr)]
                                   (.count (.codePoints text)))))

(defn parse-arg
  [arg]
  ({"-c" :count-bytes, "-l" :count-lines, "-w" :count-words, "-m" :count-chars}
   arg))

(defn run
  [command-arg file-path]
  (with-open [rdr (io/reader file-path)]
    (let [result (exec-command (parse-arg command-arg) rdr)]
      (printf "%d %s%n" result file-path))))

(def usage
  (str/join \newline
            ["Usage: lavy [-c] filename"
             "    -c: Count the number of bytes in the supplied file"
             "    -l: Count the number of newlines in the supplied file"
             "    -w: Count the number of words in the supplied file" ""]))

(defn -main
  [& args]
  (if (< (count args) 2) (print usage) (run (first args) (second args))))

