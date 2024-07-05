(ns lavy.main-test
  (:require [clojure.test :refer [testing is deftest]]
            [clojure.java.io :as io]
            [lavy.main :as m]))

(defn rdr-from-str
  [s]
  (-> s char-array io/reader))

(deftest parse-args-test
  (testing "Arg `-c` corresponds to :count-bytes"
    (is (= :count-bytes (m/parse-arg "-c"))))
  (testing "Arg `-l` corresponds to :count-lines"
    (is (= :count-lines (m/parse-arg "-l")))))

(deftest main-test
  (testing "Calling -main with no args results in usage string"
    (is (= m/usage (with-out-str (m/-main)))))
  (testing "Calling -main with 1 arg results in usage string"
    (is (= m/usage (with-out-str (m/-main ["-c"]))))))

(deftest exec-command-test
  (testing "Calling (exec-command :count-bytes rdr) returns bytes in rdr"
    (is (= 12
           (m/exec-command :count-bytes
                           (rdr-from-str "Hello World!")))))
  (testing "Calling (exec-command :count-lines rdr) returns num lines in rdr"
    (is (= 2
           (m/exec-command :count-lines (rdr-from-str "abc\ndef\ng h i"))))))
