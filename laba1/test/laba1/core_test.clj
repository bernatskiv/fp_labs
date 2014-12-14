(ns laba1.core-test
  (:require [clojure.test :refer :all]
            [laba1.core :refer :all]))

(deftest test1
  (with-redefs [parseAttribute]
    (is (= (parseAttribute "1, 3, 4") (list 1 3)))
    (is (= (parseAttribute "1, 2, sdf") (list 1 2)))
    (is (not (= (parseAttribute "2, 2, sdf") (list 1 2))))
  )
)

(deftest test2
  (with-redefs [hammingDistance]
    (is (= 1 (hammingDistance [1 1] [1 0])))
    (is (= 2 (hammingDistance [1 1] [0 2])))
    (is (= 0 (hammingDistance [1 1] [1 1])))
  )
)

(deftest test2
  (with-redefs [evklidDistance]
    (is (= 1.0 (evklidDistance [1 1] [1 0])))
    (is (= 5.0 (evklidDistance [1 1] [4 5])))
    (is (= 0.0 (evklidDistance [1 1] [1 1])))
  )
)

(run-tests 'laba1.core-test)
