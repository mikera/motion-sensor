(ns mikera.sensor.core
  ;; (:require [clojure.core.matrix :as m])
  (:require [clojure.core.async :as a :refer [go chan <! >! <!! >!!]])
  (:require [mikera.cljutils.hex :as hex])
  (:require [mikera.cljutils.bytes :as bytes])
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

(defn to-bytes 
  "Converts a message piece to a byte array."
  ([msg]
    (cond 
      (instance? bytes/BYTE-ARRAY-CLASS msg) msg
      (string? msg) (hex/bytes-from-hex-string msg)
      (keyword? msg) (hex/bytes-from-hex-string (name msg))
      :else (hex/bytes-from-hex-string (hex/hex-string msg))))
  ([msg & msgs]
    (loop [bytes (to-bytes msg)
           msgs (seq msgs)]
      (if msgs
        (let [msg (first msgs)]
          (recur (bytes/join-bytes bytes (to-bytes msg)) 
                 (next msgs)))
        bytes))))

(defn send-msg [port & msgs]
  (sp/write port (apply to-bytes msgs)))

(defn connect [bda-str]
  (send ()))