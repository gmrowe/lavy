(ns lavy.main-test
  (:require [clojure.test :refer [testing is deftest]]
            [clojure.java.io :as io]
            [lavy.main :as m]))

(defn rdr-from-str
  [s]
  (-> s
      char-array
      io/reader))

(deftest parse-args-test
  (testing "Arg `-c` corresponds to :count-bytes"
    (is (= :count-bytes (m/parse-arg "-c"))))
  (testing "Arg `-l` corresponds to :count-lines"
    (is (= :count-lines (m/parse-arg "-l"))))
  (testing "Arg `-w` corresponds to :count-words"
    (is (= :count-words (m/parse-arg "-w"))))
  (testing "Arg `-m` corresponds to :count-chars"
    (is (= :count-chars (m/parse-arg "-m")))))

(deftest main-test
  (testing "Calling -main with no args results in usage string"
    (is (= m/usage (with-out-str (m/-main)))))
  (testing "Calling -main with 1 arg results in usage string"
    (is (= m/usage (with-out-str (m/-main ["-c"]))))))

(deftest exec-command-test
  (testing "Calling (exec-command :count-bytes rdr) returns bytes in rdr"
    (is (= 31 (m/exec-command :count-bytes (rdr-from-str "Ḩ℮ɭιȭ 𝖂ọ𝘳ȴ𝖉!")))))
  (testing "Calling (exec-command :count-lines rdr) returns num lines in rdr"
    (is (= 2 (m/exec-command :count-lines (rdr-from-str "abc\ndef\ng h i")))))
  (testing "Calling (exec-command :count-words rdr) retruns words in rdr"
    (is (= 5
           (m/exec-command :count-words
                           (rdr-from-str "car star\nwar is bad")))))
  (testing "Calling (exec-command :count-chars rdr) returns chars in rdr"
    (is (= 12 (m/exec-command :count-chars (rdr-from-str "Hello World!"))))))
