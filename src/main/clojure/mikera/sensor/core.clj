(ns mikera.sensor.core
  ;; (:require [clojure.core.matrix :as m])
  (:require [clojure.core.async :as a :refer [go chan <! >! <!! >!!]])
  (:require [mikera.cljutils.hex :as hex])
  (:require [serial-port :as sp]))


(defn ble-messages 
  "Creates a Blutooth LE listener channel that reads bytes from byte-chan
   and returns a new value when the message is complete"
  ([byte-chan]
    (go )))

(defn setup []
	(def port (sp/open "COM4"))
	
	(sp/on-byte port #(println %))


)

(defn cleanup []
  (sp/close port)  
)

(def print-chan
  (let [c (chan)]
    (go 
      (loop []
        (let [r1 (<! c)
              r2 (<! c)]
          (println (str r1 r2)))
        (recur)))
    c))