package com.htmlism.totalize.core

import cats.Monoid

/**
  * A collection of binary preferences, sparsely defined for all pairs in a set
  *
  * If a pair exists, it definitely has a preference for one or the other
  *
  * If a pair does not exist, the no statement can be made about their preference
  *
  * @param xs
  * @tparam A
  */
case class PreferenceRelation[A](xs: Map[Pair[A], BinaryPreference]):
  def withPreference(pair: Pair[A], pref: BinaryPreference): PreferenceRelation[A] =
    PreferenceRelation(xs.updated(pair, pref))

object PreferenceRelation:
  given [A]: Monoid[PreferenceRelation[A]] =
    new Monoid[PreferenceRelation[A]]:
      def empty: PreferenceRelation[A] =
        PreferenceRelation(Map.empty)

      def combine(x: PreferenceRelation[A], y: PreferenceRelation[A]): PreferenceRelation[A] =
        PreferenceRelation(x.xs ++ y.xs)

  def empty[A]: PreferenceRelation[A] =
    Monoid[PreferenceRelation[A]].empty
