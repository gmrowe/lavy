(ns lavy.main-test
  (:require [clojure.test :refer [testing is deftest]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [lavy.main :as m]))

(defn rdr-from-str
  [s]
  (-> s
      char-array
      io/reader))

(deftest parse-args-test
  (testing "Arg -c followed by single filename"
    (is (= {:options [:count-bytes], :files ["file.name"]}
           (m/parse-args ["-c" "file.name"]))))
  (testing "Arg -l followed by a single filename"
    (is (= {:options [:count-lines], :files ["file.txt"]}
           (m/parse-args ["-l" "file.txt"]))))
  (testing "Arg -w followed by a single filename"
    (is (= {:options [:count-words], :files ["file.dat"]}
           (m/parse-args ["-w" "file.dat"]))))
  (testing "Arg -m followed by a single filename"
    (is (= {:options [:count-chars], :files ["file.txt"]}
           (m/parse-args ["-m" "file.txt"]))))
  (testing "Multiple opts with single filename"
    (is (= {:options [:count-bytes :count-words], :files ["file.csv"]}
           (m/parse-args ["-c" "-w" "file.csv"]))))
  (testing "Multiple short form opts single filename"
    (is (= {:options [:count-lines :count-words], :files ["file.xls"]}
           (m/parse-args ["-lw" "file.xls"]))))
  (testing "Opts with multiple filenames"
    (is (= {:options [:count-lines :count-words],
            :files ["file-1.txt" "file-2.txt"]}
           (m/parse-args ["-lw" "file-1.txt" "file-2.txt"]))))
  (testing "If no opts provided, default is -clw"
    (is (= {:options [:count-bytes :count-lines :count-words],
            :files ["file-1.txt"]}
           (m/parse-args ["file-1.txt"]))))
  (testing "If no file is provided, default is stdin"
    (is (= {:options [:count-lines :count-words], :files ["*in*"]}
           (m/parse-args ["-lw"]))))
  (testing "If no args are provided the all defaults should be used"
    (is (= {:options [:count-bytes :count-lines :count-words], :files ["*in*"]}
           (m/parse-args nil)))))

(deftest exec-command-test
  (testing "Calling (exec-command :count-bytes rdr) returns bytes in rdr"
    (is (= 31 (m/exec-command :count-bytes "Ḩ℮ɭιȭ 𝖂ọ𝘳ȴ𝖉!"))))
  (testing "Calling (exec-command :count-lines rdr) returns num lines in rdr"
    (is (= 2 (m/exec-command :count-lines "abc\ndef\ng h i"))))
  (testing "Calling (exec-command :count-words rdr) retruns words in rdr"
    (is (= 5 (m/exec-command :count-words "car star\nwar is bad"))))
  (testing "Counting chars of an ascii string"
    (is (= 12 (m/exec-command :count-chars "Hello World!"))))
  (testing "Counting chars of an UTF-8 string"
    (is (= 12 (m/exec-command :count-chars "Ḩ℮ɭιȭ 𝖂ọ𝘳ȴ𝖉!")))))

(deftest output-results-test
  (testing "Results from single commnd single filename"
    (is (= "  174355 alice.txt"
           (m/format-output [{:file-path "alice.txt", :count-chars 174355}]))))
  (testing "Results from multiple commands single filename"
    (is (= "    3756   29564  174355 alice.txt"
           (m/format-output [{:file-path "alice.txt",
                              :count-bytes 174355,
                              :count-words 29564,
                              :count-lines 3756}]))))
  (testing "Results from multiple commands multiple files"
    (is (= (str/join \newline
                     ["    3756   29564  174355 alice.txt"
                      "    7145   58164  342190 test.txt"
                      "   10901   87728  516545 total"])
           (m/format-output [{:file-path "alice.txt",
                              :count-bytes 174355,
                              :count-words 29564,
                              :count-lines 3756}
                             {:file-path "test.txt",
                              :count-bytes 342190,
                              :count-words 58164,
                              :count-lines 7145}])))))

(deftest run-test
  (testing "Read from stdin when file = *in*"
    (is (= "       0       2      12 "
           (with-in-str "Hello World!" (m/run nil)))))
  (testing "Calling run with -h prints usage string"
    (is (= m/usage (m/run ["-h"])))))


(comment
  "The order of output always takes the form of line, word, byte, and file name.")
