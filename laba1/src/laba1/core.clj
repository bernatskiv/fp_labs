(ns laba1.core
  (:gen-class))

(use '[clojure.java.io :only (reader)])
(require '[clojure.string :as str])

(defn parseNumber
  [s]
  (read-string (re-find #"[\d \.]+" s))
 )

(defn parseAttribute
  [line]
  (let 
    [strAr (str/split line #",")]
    (map parseNumber (butlast strAr))
  )
)

(defn evklidDistance
  [vec1 vec2]
  (Math/sqrt (reduce + (map (fn [a b] (Math/pow (- a b) 2)) vec1 vec2)))
 )

(defn hammingDistance
  [vec1 vec2]
  (reduce #(if %2 %1 (inc %1)) 0 (map (fn [a b] (= a b)) vec1 vec2))
)

(def dist evklidDistance)

(defn ex
  [alfa i j] 
  (Math/exp (- (* alfa (Math/pow (dist i j) 2))))
)

(defn calcPotential
  [alfa xi dataPoints]  
  (reduce (fn [sum xj] (+ sum (ex alfa xi xj))) 0 dataPoints)
 )

(defn correctPotential
  [beta Pk xk Pi xi]
  (- Pi (* Pk (ex beta xi xk)))
)

(defn correctPotential2
  [beta {Pk :potential xk :point} {idi :id Pi :potential xi :point}]
  {:id idi :point xi :potential (correctPotential beta Pk xk Pi xi)}
)

(defn findClasterCenters
  [bigEpsilon smallEpsilon ra alfa beta dataPoints]
  (let 
    [
     findMax (fn [data] (apply max-key :potential data))
     removeMax (fn [data max] (filter #(not (= (% :id) (max :id))) data))
     
     initData (map-indexed (fn [idx el] {:id idx :point el :potential (calcPotential alfa el dataPoints)}) dataPoints)
     
     firstCenter (findMax initData)
     startPoints (removeMax initData firstCenter)
    ]    
     (loop  [centers (list firstCenter) 
             points startPoints
             prevCenter firstCenter]
       (let [newPoints (map #(correctPotential2 beta prevCenter %) points)
             newCenter (findMax newPoints)
             otherPoints (removeMax newPoints newCenter)]         
         (if (> (newCenter :potential) (* bigEpsilon (firstCenter :potential)))
           (recur (cons newCenter centers) otherPoints newCenter)
           (if (< (newCenter :potential) (* smallEpsilon (firstCenter :potential)))
             centers
             (let
               [
                centDists (map #(dist (newCenter :point) (% :point)) centers)
                dmin (apply min centDists)
                k (+ (/ dmin ra) (/ (newCenter :potential) (firstCenter :potential)))
               ]
               (if (>= k 1)
                 (recur (cons newCenter centers) otherPoints newCenter)
                 (recur centers (cons {:id (newCenter :id) :point (newCenter :point) :potential 0} otherPoints) prevCenter)
               )
             )
           )
         )
       )
     )
  )
)

(defn clasterize
  [centers points]
  (let [findClosest (fn 
                      [p] 
                      (apply min-key :distance 
                             (map-indexed 
                               (fn [idx ci] {:id idx :distance (dist p (ci :point))}) 
                               centers
                             )
                      )
                    )
       ]
    (map #(let[res {:point % :claster ((findClosest %) :id)}]
            res
          ) points)
  )
)

(defn -main
  [filePath distanceType]
  (let [
        file filePath
        ra 3.0
        rb (* 1.5 ra)
        alfa (/ 4 (Math/pow ra 2))
        beta (/ 4 (Math/pow rb 2))
        bigEpsilon 0.5
        smallEpsilon 0.15
       ]
    (with-open [rdr (reader file)]
      (def m (map parseAttribute (line-seq rdr)))
      (dorun m)
    )
    (def dist (if (= distanceType 1) hammingDistance evklidDistance))
    (let 
      [centers (findClasterCenters bigEpsilon smallEpsilon ra alfa beta m)
       result (clasterize centers m)]
      (println result)
      (println (count centers))
      (println centers)
    )
  )
)
