(ns SpeechSynthesis.core
  (:require  
    [clojure.java.io :as io]
    [clojure.string :as string]
    [clj-commons-exec :as second-shell]
    [clojure.java.shell :as clojure-shell :only [sh]]
    [speech-synthesis.say :as say])
  (:import 
    (java.io File FilenameFilter)))

(defn festival [x]
  (clojure-shell/sh "sh" "-c" (str "echo " x " | festival --tts")))
(defn espeak [x] 
  (clojure-shell/sh "espeak" x))
(defn mac-say[x] 
  (clojure-shell/sh "say" x))
(defn my-windows-speak
  [x]
  (let [my-path-file-obj (.getCanonicalFile (io/file "."))
        my-path (.getAbsolutePath my-path-file-obj)
        sep (File/separator)
        path-to-speaker (str my-path sep "resources" sep "SayStatic.exe")]
    (second-shell/sh [path-to-speaker x])))


(defn check-if-installed
  [x] 
  (:exit(clojure-shell/sh "sh" "-c" 
            (str "command -v " x " >/dev/null 2>&1 || { echo >&2 \"\"; exit 1; }"))))


(defn engine-check [] 
  (def engines 
    (conj ["Google" (get-other-engines)]
          )) ;; Say is the Apple say command
  (remove nil? engines))

(defn get-other-engines []
  (if (= (check-if-installed "festival") 0) 
    "Festival")
  (if (= (check-if-installed "espeak") 0) 
    "ESpeak")
  (if (= (check-if-installed "say") 0) 
    "Say")) 

(defn set-engine
  ([] 
    (def speak my-windows-speak))
  ([eng] 
    (cond 
    (= eng "Google") (def speak say/say)
    (= eng "Festival" )(def speak festival)
    (= eng "ESpeak") (def speak espeak)
    (= eng "Say") (def speak mac-say))))


#_(set-engine "Festival") ;; set the engine
(set-engine)
(speak "A study in Scarlet - Sherlock Holmes") ;; speak your text

