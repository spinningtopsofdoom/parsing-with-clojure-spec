(ns parsing.advent.parsing
  (:require [parsing.advent.data :as pdata]
            [clojure.alpha.spec :as s]))

(def sample-tokens (pdata/tokenize pdata/puzzle-str))

(def first-sample (first sample-tokens))

;; Sanity Test that conform is working

(s/conform (s/* string?) first-sample)

;; Check that eye color key value is working

(def eye-colors #{"amb" "blu" "brn" "gry" "grn" "hzl" "oth"})
(s/def ::eye-color (s/cat :key #{"ecl"} :color eye-colors))
(s/conform (s/cat :begin (s/* string?) :eye-color ::eye-color :end (s/* string?)) first-sample)

;; Check that birth year works

(s/def ::birth-year (s/cat :key #{"byr"} :color (fn birth [year] (<= 1920 (Integer/parseInt year) 2002))))
(s/conform (s/cat :begin (s/* string?) :birth-year ::birth-year :end (s/* string?)) first-sample)

;; Combine validations into additive format. Check that key value of eye color or birth year is in sample

(s/conform
  (s/*
    (s/alt
      :eye-color ::eye-color
      :birth-year ::birth-year))
  first-sample)

;; Clojure Spec regex needs to match the entire data structure not a partial match like string regular expressions

(s/conform
  (s/*
    (s/alt
      :eye-color ::eye-color
      :birth-year ::birth-year
      :extra (s/cat :key string? :val string?)))
  first-sample)

;; Parse all sample passport data structures

(mapv
  #(s/conform
     (s/*
       (s/alt
         :eye-color ::eye-color
         :birth-year ::birth-year
         :extra (s/cat :key string? :val string?)))
     %)
  sample-tokens)