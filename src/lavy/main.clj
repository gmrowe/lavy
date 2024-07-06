(ns lavy.main
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn exec-command
  [command rdr]
  (let [text (slurp rdr)]
    (case command
      :count-bytes (count (seq (.getBytes text)))
      :count-lines (count (re-seq #"\r?\n" text))
      :count-words (count (str/split text #"\s+"))
      :count-chars (.count (.codePoints text)))))

(defn format-output
  [outputs]
  (let [output (first outputs)
        fmt (fn [opt] (if opt (format "%8d" opt) ""))]
    (format "%s%s%s%s %s"
            (fmt (:count-lines output))
            (fmt (:count-words output))
            (fmt (:count-chars output))
            (fmt (:count-bytes output))
            (:file-path output))))

(def opt-map
  {\c :count-bytes, \l :count-lines, \m :count-chars, \w :count-words})

(defn parse-opt-sequence [opts] (mapv opt-map (next opts)))

(defn parse-args
  [args]
  (let [[opts filenames] (partition-by #(str/starts-with? % "-") args)]
    {:options (mapcat parse-opt-sequence opts), :files filenames}))

(defn exec-all
  [{:keys [options files]}]
  (for [opt options
        file files]
    {opt (exec-command opt (io/reader file)), :file-path file}))

(defn exec-commands
  [opt-map]
  (map #(apply merge %)
    (->> (exec-all opt-map)
         (group-by :filename)
         vals)))

(defn parse-arg
  [arg]
  ({"-c" :count-bytes, "-l" :count-lines, "-w" :count-words, "-m" :count-chars}
   arg))

(defn run
  [args]
  (let [[command-arg file-path] args]
    (with-open [rdr (io/reader file-path)]
      (let [result (exec-command (parse-arg command-arg) rdr)]
        (printf "%d %s%n" result file-path)))))

(def usage
  (str/join \newline
            ["Usage: lavy [-clmw] [file]"
             "    -c: Count the number of bytes in the supplied file"
             "    -l: Count the number of newlines in the supplied file"
             "    -w: Count the number of words in the supplied file"
             "    -m: Count the number of characters in the supplied file" ""]))

(defn -main [& args] (if (< (count args) 2) (print usage) (run args)))

