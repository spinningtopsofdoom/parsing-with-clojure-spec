(ns parsing.advent.feedback
  (:require [parsing.advent.data :as pdata]
            [clojure.alpha.spec :as s]))

(def sample-tokens (pdata/tokenize pdata/puzzle-str))

(def first-sample (first sample-tokens))

;; Sanity Test that conform is working

(s/conform (s/* string?) first-sample)

;; Eye Color and Birth Year from parsing example

(def eye-colors #{"amb" "blu" "brn" "gry" "grn" "hzl" "oth"})
(s/def ::eye-color (s/cat :key #{"ecl"} :color eye-colors))
(s/def ::birth-year (s/cat :key #{"byr"} :color (fn birth [year] (<= 1920 (Integer/parseInt year) 2002))))

;; WHy is this invalid?

(s/conform
  (s/*
    (s/alt
      :eye-color ::eye-color
      :birth-year ::birth-year))
  first-sample)

;; Explain tells us the failure value and where the spec failed

(s/explain
  (s/*
    (s/alt
      :eye-color ::eye-color
      :birth-year ::birth-year))
  first-sample)

;; explain-data gives the very detailed data structure

(s/explain-data
  (s/*
    (s/alt
      :eye-color ::eye-color
      :birth-year ::birth-year))
  first-sample)

;; Flesh out parsing in REPL for issue year. Try out invalid issue year

(s/def
  ::passport-data
  (s/*
    (s/alt
      :eye-color ::eye-color
      :birth-year ::birth-year
      :issue-year (s/cat :key #{"iyr"} :year (fn issue [year] (<= 2000 (Integer/parseInt year) 2009)))
      :extra (s/cat :key string? :val string?))))

;; Explain what happened for the first invalid parse

(let [invalid-token (first (drop-while #(s/valid? ::passport-data %) sample-tokens))]
  (if (empty? invalid-token)
    :success
    (s/explain ::passport-data invalid-token)))

;; The extra catch all clause caught issue year instead of testing it

(s/conform ::passport-data first-sample)

;; Modify Catch all not to capture issue year validation

(s/def
  ::testing-data
  (s/*
    (s/alt
      :eye-color ::eye-color
      :birth-year ::birth-year
      :issue-year (s/cat :key #{"iyr"} :year (fn issue [year] (<= 2001 (Integer/parseInt year) 2009)))
      :extra (s/cat :key #(not (#{"iyr"} %)) :val string?))))

;; Now explain captures the parsing error

(let [invalid-token (first (drop-while #(s/valid? ::testing-data %) sample-tokens))]
  (if (empty? invalid-token)
    :success
    (s/explain ::testing-data invalid-token)))

;; Valid issue years re between 2010 and 2020 not 2001 and 2009

(s/def
  ::testing-data
  (s/*
    (s/alt
      :eye-color ::eye-color
      :birth-year ::birth-year
      :issue-year (s/cat :key #{"iyr"} :year (fn issue [year] (<= 2010 (Integer/parseInt year) 2020)))
      :extra (s/cat :key #(not (#{"iyr"} %)) :val string?))))

(let [invalid-token (first (drop-while #(s/valid? ::testing-data %) sample-tokens))]
  (if (empty? invalid-token)
    :success
    (s/explain ::testing-data invalid-token)))