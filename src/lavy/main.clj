(ns lavy.main
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def opt-map
  {\c :count-bytes,
   \l :count-lines,
   \m :count-chars,
   \w :count-words,
   \h :help})

(defn parse-opt-sequence [opts] (mapv opt-map (next opts)))

(defn parse-args
  [args]
  (let [[opts filenames] (partition-by #(str/starts-with? % "-") args)]
    (cond (or (not opts) (not (str/starts-with? (first opts) "-")))
            (recur (cons "-clw" args))
          (seq filenames) {:options (mapcat parse-opt-sequence opts),
                           :files filenames}
          :else (recur (concat args ["*in*"])))))

(defn exec-command
  [command text]
  (case command
    :count-bytes (count (seq (.getBytes text)))
    :count-lines (count (re-seq #"\r?\n" text))
    :count-words (count (str/split text #"\s+"))
    :count-chars (.count (.codePoints text))))

(defn compile-results
  [opt-map]
  (for [file (:files opt-map)
        :let [rdr (if (= file "*in*") *in* (io/reader file))
              text (slurp rdr)]
        opt (:options opt-map)]
    {opt (exec-command opt text), :file-path file}))

(defn exec-commands
  [opt-map]
  (->> opt-map
       compile-results
       (group-by :file-path)
       vals
       (mapv #(apply merge %))))

(defn format-output-line
  [line]
  (letfn [(fmt [opt] (if opt (format "%8d" opt) ""))]
    (format "%s%s%s%s %s"
            (fmt (:count-lines line))
            (fmt (:count-words line))
            (fmt (:count-chars line))
            (fmt (:count-bytes line))
            (if (= (:file-path line) "*in*") "" (:file-path line)))))

(defn merge-total
  [results]
  (if (> (count results) 1)
    (let [total (->> results
                     (map #(dissoc % :file-path))
                     (apply merge-with +))]
      (conj results (assoc total :file-path "total")))
    results))

(defn format-output
  [results]
  (->> results
       merge-total
       (map format-output-line)
       (str/join \newline)))

(def usage
  (str/join \newline
            ["Usage: lavy [-clmwh] [file]" "Options:"
             "    -h: Print this help message"
             "    -c: Count the number of bytes in the supplied file"
             "    -l: Count the number of newlines in the supplied file"
             "    -w: Count the number of words in the supplied file"
             "    -m: Count the number of characters in the supplied file" ""]))

(defn run
  [args]
  (let [opts (parse-args args)]
    (if (some #{:help} (:options opts))
      usage
      (-> opts
          exec-commands
          format-output))))

(defn -main [& args] (println (run args)))

