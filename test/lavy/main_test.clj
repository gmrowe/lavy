(ns lavy.main-test
  (:require [clojure.test :refer [testing is deftest]]
            [clojure.java.io :as io]
            [lavy.main :as m]))

(deftest parse-args-test
  (testing "Arg `-c` corresponds to :count-bytes"
    (is (= :count-bytes (m/parse-arg "-c")))))

(deftest main-test
  (testing "Calling -main with no args results in usage string"
    (is (= m/usage (with-out-str (m/-main)))))
  (testing "Calling -main with 1 arg results in usage string"
    (is (= m/usage (with-out-str (m/-main ["-c"]))))))

(deftest exec-command-test
  (testing "Calling (run :count-bytes rdr) returns bytes in rdr"
    (is (= 12
           (m/exec-command :count-bytes
                           (-> "Hello World!" char-array io/reader))))))
