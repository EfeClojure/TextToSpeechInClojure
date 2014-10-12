(ns OnlySexyWords.core
  (:require 
    [clojure.java.io :as io]
    [clojure.string :as string]
    [clj-commons-exec :as second-shell]
    [clojure.java.shell :as clojure-shell :only [sh]]
    [clj-http.client :as client]
    [fs.core :as fs])
  (:import 
    javazoom.jl.player.Player
    (java.io File FilenameFilter FileOutputStream)
    (java.awt BorderLayout)
    (java.awt.event ActionListener)
    (javax.swing JButton JFrame JLabel 
                 JOptionPane JPanel JTextField))
  (:gen-class))

  
(defn festival [x]
  (clojure-shell/sh "sh" "-c" (str "echo " x " | festival --tts")))
(defn espeak [x] 
  (clojure-shell/sh "espeak" x))
(defn mac-say[x] 
  (clojure-shell/sh "say" x))
(defn my-windows-speak [x]
  (let [app-path-obj (.getCanonicalFile (io/file "."))
        app-path (.getAbsolutePath app-path-obj)
        sep (File/separator)
        path-to-speaker (str app-path sep "resources" sep "SayStatic.exe")]
    (second-shell/sh [path-to-speaker x])))

(defn google-speak [response]
  (let [google-url "http://translate.google.com/translate_tts" 
        query-params {:query-params 
                      {"ie" "UTF-8" "tl" "en" "q" response} :as :byte-array}         
        google-response (client/get google-url query-params)
        mp3-from-google (:body google-response)
        file (fs/temp-file "say" ".mp3")]
    (with-open [file (FileOutputStream. file)]
      (.write file mp3-from-google))
    (with-open [player (new Player (io/input-stream file))]
      (.play player))))


(defn check-if-installed
  [x] 
  (let [command-text 
        (str "command -v " x " >/dev/null 2>&1 || { echo >&2 \"\"; exit 1; }")]
    (:exit 
      (clojure-shell/sh "sh" "-c" command-text))))

(declare get-other-engines) 
(defn engine-check [] 
  (def engines 
    (conj ["Google" (get-other-engines)])) ;; Say is the Apple say command
  (remove nil? engines))

(defn get-other-engines []
  (if (= (check-if-installed "festival") 0) 
    "Festival")
  (if (= (check-if-installed "espeak") 0) 
    "ESpeak")
  (if (= (check-if-installed "say") 0) 
    "Say")) 

(defn speak 
  ([what-to-say] 
    (my-windows-speak what-to-say))
  ([engine-type what-to-say]
    (cond 
      (= engine-type "Google") (google-speak what-to-say)
      (= engine-type "Festival") (festival what-to-say)
      (= engine-type "ESpeak") (espeak what-to-say)
      (= engine-type "Say") (mac-say what-to-say))))


#_(set-engine "Festival") ;; set the engine


(defn message
  [text-field]
  (speak (.getText text-field)))

(defn -main [] 
  (let [name-field (JTextField. "Schnapp motherfucker!" 20)
        greet-button (JButton. "Speak damn it!")
        panel (JPanel.)
        frame (proxy [JFrame ActionListener]
                [] ; superclass constructor arguments
              (actionPerformed [e] ; nil below is the parent component
                (message name-field)))]
    (doto panel 
      (.add (JLabel. "Name:"))
      (.add name-field))
    (doto frame
      (.add panel BorderLayout/CENTER)
      (.add greet-button BorderLayout/SOUTH) 
      (.pack)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setVisible true))
    (.addActionListener greet-button frame)))
