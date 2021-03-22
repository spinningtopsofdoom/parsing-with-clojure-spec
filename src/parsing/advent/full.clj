(ns parsing.advent.full
  (:require [parsing.advent.data :as pdata]
            [clojure.alpha.spec :as s]
            [clojure.test.check.generators :as tgen]))

(s/def ::eye-color (s/cat :key #{"ecl"} :color #{"amb" "blu" "brn" "gry" "grn" "hzl" "oth"}))

(s/def ::personal-id (s/cat
                       :key #{"pid"}
                       :id (s/with-gen
                             (fn passport [id] (re-matches #"[0-9]{9}" id))
                             #(tgen/fmap str (tgen/choose 100000000 999999999)))))

(s/def ::expiration-year (s/cat
                           :key #{"eyr"}
                           :year (s/with-gen
                                   (fn expiration [year] (<= 2020 (Integer/parseInt year) 2030))
                                   #(tgen/fmap str (tgen/choose 2020 2030)))))
(s/def ::hair-color (s/cat
                      :key #{"hcl"}
                      :color (s/with-gen
                               (fn hair [hex-color] (re-matches #"#[0-9a-f]{6}" hex-color))
                               (fn [] (tgen/fmap #(format "#%06x" %) (tgen/choose 0 16777215))))))
(s/def ::birth-year (s/cat
                      :key #{"byr"}
                      :year (s/with-gen
                              (fn birth [year] (<= 1920 (Integer/parseInt year) 2002))
                              #(tgen/fmap str (tgen/choose 1920 2002)))))
(s/def ::issue-year (s/cat
                      :key #{"iyr"}
                      :year (s/with-gen
                              (fn issue [year] (<= 2010 (Integer/parseInt year) 2020))
                              #(tgen/fmap str (tgen/choose 2010 2020)))))

(s/def ::height (s/cat
                  :key #{"hgt"}
                  :height (s/with-gen
                            (fn height [length-in-units]
                              (let [[length units] (->> length-in-units (re-find #"(\d+)(cm|in)") rest)]
                                (case units
                                  "in" (<= 56 (Integer/parseInt length) 76)
                                  "cm" (<= 150 (Integer/parseInt length) 193)
                                  false)))
                            (fn [] (tgen/one-of
                                     [(tgen/fmap #(format "%din" %) (tgen/choose 56 76))
                                      (tgen/fmap #(format "%dcm" %) (tgen/choose 150 193))])))))

(s/def ::passport-data
  (s/*
    (s/alt
      :eye-color ::eye-color
      :id ::personal-id
      :expiration ::expiration-year
      :hair-color ::hair-color
      :birth-year ::birth-year
      :issue-year ::issue-year
      :height ::height
      :extra (s/cat :key #{"cid"} :val string?))))

(def puzzle-data (pdata/tokenize pdata/puzzle-file))

;; Parse Passport Information

(mapv
  #(s/conform ::passport-data %)
  (take 5 puzzle-data))

;; Show explanations of 5 failing specs

(mapcat
  #(s/explain ::passport-data %)
  (take 5 (remove #(s/valid? ::passport-data %) puzzle-data)))

;; Generate 5 samples of passport data

(tgen/sample
  (s/gen
    ::passport-data
    {[:extra :val] #(tgen/fmap str (tgen/choose 100000 999999))})
  5)

;; Generates spec data then runs conform on the data

(s/exercise ::passport-data 5)