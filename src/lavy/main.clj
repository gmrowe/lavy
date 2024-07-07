(ns lavy.main
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def opt-map
  {\c :count-bytes, \l :count-lines, \m :count-chars, \w :count-words})

(defn parse-opt-sequence [opts] (mapv opt-map (next opts)))

(defn parse-args
  [args]
  (let [[opts filenames] (partition-by #(str/starts-with? % "-") args)]
    {:options (mapcat parse-opt-sequence opts), :files filenames}))

(defn exec-command
  [command rdr]
  (let [text (slurp rdr)]
    (case command
      :count-bytes (count (seq (.getBytes text)))
      :count-lines (count (re-seq #"\r?\n" text))
      :count-words (count (str/split text #"\s+"))
      :count-chars (.count (.codePoints text)))))

(defn exec-commands
  [opt-map]
  (let [results (for [opt (:options opt-map)
                      file (:files opt-map)]
                  {opt (exec-command opt (io/reader file)), :file-path file})]
    (->> results
         (group-by :file-path)
         vals
         (map #(apply merge %)))))

(defn format-output
  [results]
  (let [fmt (fn [opt] (if opt (format "%8d" opt) ""))]
    (str/join \newline
              (map (fn [output]
                     (format "%s%s%s%s %s"
                             (fmt (:count-lines output))
                             (fmt (:count-words output))
                             (fmt (:count-chars output))
                             (fmt (:count-bytes output))
                             (:file-path output)))
                results))))

(defn run
  [args]
  (->> args
       parse-args
       exec-commands
       (map format-output)
       (str/join \newline)
       println))

(def usage
  (str/join \newline
            ["Usage: lavy [-clmw] [file]"
             "    -c: Count the number of bytes in the supplied file"
             "    -l: Count the number of newlines in the supplied file"
             "    -w: Count the number of words in the supplied file"
             "    -m: Count the number of characters in the supplied file" ""]))

(defn -main [& args] (if (< (count args) 2) (print usage) (run args)))

