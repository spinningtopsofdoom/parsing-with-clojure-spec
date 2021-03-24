(ns parsing.advent.sample
  (:require [clojure.alpha.spec :as s]
            [clojure.test.check.generators :as tgen]))

;; Simplest spec first, ee color which matches a set of enums

(s/def ::eye-color (s/cat :key #{"ecl"} :color #{"amb" "blu" "brn" "gry" "grn" "hzl" "oth"}))

;; Create 5 samples of eye color

(tgen/sample (s/gen ::eye-color) 5)

;; Let's try with Personal ID

(s/def ::personal-id (s/cat :key #{"pid"} :id (fn passport [id] (re-matches #"[0-9]{9}" id))))

;; Can not gen any spec beyond the basic (e.g. int?, string?, a set, etc.)

(tgen/sample (s/gen ::personal-id) 5)

;; Check personal id generator works

(def personal-id-gen  (tgen/fmap str (tgen/choose 100000000 999999999)))
(tgen/sample personal-id-gen 5)

;; Override id part of personal id. Note the generator has to be wrapped in a function

(tgen/sample (s/gen ::personal-id {[:id] (fn wrapper [] personal-id-gen)}) 5)

;; Overrides take the path to the spec using the keys of the regex

(tgen/sample
  (s/gen
    (s/alt
      :eye-color ::eye-color
      :personal-id ::personal-id)
    {[:personal-id :id] (fn wrapper [] personal-id-gen)})
  5)

;; Use with gen to attach generator to registered spec

(s/def ::personal-id (s/cat
                       :key #{"pid"}
                       :id (s/with-gen
                             (fn passport [id] (re-matches #"[0-9]{9}" id))
                             #(tgen/fmap str (tgen/choose 100000000 999999999)))))

;; No need for overrides on the id part of personal id?

(tgen/sample (s/gen ::personal-id) 5)

(tgen/sample
  (s/gen
    (s/alt
      :eye-color ::eye-color
      :personal-id ::personal-id))
  5)

;; Can we generate invalid data, say for testing?

(def short-personal-id-gen  (tgen/fmap str (tgen/choose 100 999)))
(tgen/sample short-personal-id-gen 5)

;; Nope spec generators have an inbuilt check (such-that) requiring matching the spec

(tgen/sample (s/gen ::personal-id {[:id] (fn wrapper [] short-personal-id-gen)}) 5)