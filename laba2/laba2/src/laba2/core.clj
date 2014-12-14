(ns laba2.core
  (:gen-class)
  (:require [net.cgrand.enlive-html :as html]))

(use '[clojure.java.io :only (reader)])
(require '[org.httpkit.client :as http])
(require '[clojure.string :as str])

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url))
)

(defn isUrlValid?
  [url]
  (let
    [match (re-matches #"^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$" url)]
    (not (= nil match))
  )
)

(defn hasKey?
  [atm key]
  (contains? (deref atm) key)
)

(defn addIfNotExists
  [atm key value]
  (println (str "add if not exists.\n key: " key " value: " value " storage: " (deref atm) ))
  (swap! atm #(if (contains? % key) % (assoc % key value)))
)

(defn getUrls
  [url]
  (let [
        res
        (filter isUrlValid? (map #(get-in %[:attrs :href]) (html/select (fetch-url url) [:a])))
        ]
    (println "links")
    (println res)
    res
  )
)

(def processUrl nil)

(defn getRespInfo
  [url]
  (println "enter getRespInfo")
  (let [options {:timeout 20000 :follow-redirects false}]
    (let [{:keys [status headers body error] :as resp} @(http/get url options)
          res {:status status :headers headers}]
       (println status)
       res
    )
  )
)

(defn processUrlPack
  [url depth maxDepth visitedUrls]
  (println (str "processUrlpack depth:" depth))
  (let
    [links (getUrls url)
     values (map #((println %)(processUrl % (inc depth) maxDepth visitedUrls)) links)]
    (dorun values)
    (println "store value")
    (addIfNotExists visitedUrls url values)
    (println "stored")
  )
)

(defn processBadUrl
  [url code]
  (println (str "bad: " url))
  (str url " bad" (if (= code 404) " Page not found" ""))
)

(def redirectCodes [301 302 303 305 307])
(defn processRedirectUrl
  [url depth maxDepth visitedUrls newUrl]
  (println (str "redirect " url " to " newUrl))
  (processUrl newUrl (inc depth) maxDepth visitedUrls)
  (str url " redirects to " newUrl)
)

(defn processSimpleUrl
  [url depth maxDepth visitedUrls]
  (println (str "simple: " url))
  (processUrlPack url  (inc depth) maxDepth visitedUrls)
  (println (str "finish simple: " url))
  url
)

(defn processUrl
  [url curDepth maxDepth visitedUrls]
  (println "enter processUrl")
  (let [
          respInfo (getRespInfo url)
          code (respInfo :status)
          headers (respInfo :headers)
       ]
      (println (str "depth: " curDepth " start: " url))
      (if (<= curDepth maxDepth)
        (if (or (nil? code) (= code 404))
          (processBadUrl url code)
          (if (contains? redirectCodes code)
            (processRedirectUrl url (inc curDepth) maxDepth visitedUrls (:location headers))
            (when (= code 200)
              (processSimpleUrl url (inc curDepth) maxDepth visitedUrls)
            )
           )
         )
         (println "FINISH")
      )
  )
)


(defn -main
  [filePath depth]
  (let [
        file filePath
        visitedLinks (atom {})
        maxDepth depth
       ]
    (with-open [rdr (reader file)]
      (def urls (line-seq rdr))
      (dorun urls)
    )
    (doseq [url urls] (processUrlPack url 0 maxDepth visitedLinks))
  )
)

(-main "D:\\fp\\links.txt" 2)
