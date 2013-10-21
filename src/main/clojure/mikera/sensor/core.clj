(ns mikera.sensor.core
  ;; (:require [clojure.core.matrix :as m])
  (:require [clojure.core.async :as a :refer [go chan <! >! <!! >!!]])
  (:require [mikera.cljutils.hex :as hex])
  (:require [mikera.cljutils.bytes :as bytes])
  (:require [serial-port :as sp]))


(defn ble-messages 
  "Creates a Blutooth LE listener channel that reads bytes from byte-chan
   and returns a new value when the message is complete"
  ([c]
    (let [result-chan (chan)]
      (go
        (loop []
          (let [eb (<! c)]
            (when-not (== 0x04 eb) (println (str "Unknown byte: " (hex/hex-string eb))) (recur))
            (println "event start!")
            (let [ec (<! c)
                  elen (<! c)
                  ^bytes res (byte-array (+ 3 elen))]
              (aset res 0 (unchecked-byte eb))
              (aset res 1 (unchecked-byte ec))
              (aset res 2 (unchecked-byte elen))
              (dotimes [i elen]
                (aset res (+ i 3) (unchecked-byte (<! c))))
              (>! result-chan res)
              (recur)))))
      result-chan)))

(defn print-messages
  "Prints incoming messages from from-chan, asumming they are all byte arrays"
  ([from-chan]
    (go 
      (loop [] 
        (println (bytes/to-hex-string (<! from-chan)))
        (recur)))))

(defn rcv-fn [to-chan]
  (fn [b]
    ;; (println b)
    (>!! to-chan b)))

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
          (recur (bytes/join bytes (to-bytes msg)) 
                 (next msgs)))
        bytes))))

(defn send-msg [port & msgs]
  (let [bs (apply to-bytes msgs)]
    (println (str "Sending: " (bytes/to-hex-string bs)))
    (sp/write port bs)))

(defn connect 
  "Connects to a specific BDA device"
  ([port bda-str]
  (send-msg port "01 09 FE 09 00 00 00" bda-str)))

(defn set-gatt 
  "Writes to a GATT value"
  ([port handle attr data]
    (let [^bytes data (to-bytes data)
          len (+ 4 (alength data))]
      (send-msg port "01 92 FD" (bytes/unchecked-byte-array [len]) handle attr data))))


(defn setup []
	(def port (sp/open "COM4"))
 
  (def bchan (chan)) 

  (def pchan (print-messages (ble-messages bchan))) 
	
  (def rfn (rcv-fn bchan))
  
	(sp/on-byte port rfn)

  (connect port "78 58 D5 F7 B1 34") ;; put your BDA device address here..... 
  (set-gatt port "0000" "2e 00" "0100") ;; turn on acceleometer notifications
  (set-gatt port "0000" "31 00" "01") ;; turn on acceleometer
  
  
  (set-gatt port "0000" "31 00" "00") ;; turn off acceleometer
)

(defn cleanup []
  (sp/close port)  
)