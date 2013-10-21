(ns mikera.sensor.async-test
  ;; (:require [clojure.core.matrix :as m])
  (:require [clojure.core.async :as a :refer [go chan <! >! <!! >!!]]))


(def print-chan
  (let [c (chan)]
    (go 
      (loop []
        (let [r1 (<! c)
              r2 (<! c)]
          (println (str r1 r2)))
        (recur)))
    c))
