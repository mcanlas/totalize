package com.htmlism.totalize.core

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
case class PreferenceRelation[A](xs: Map[Pair[A], BinaryPreference])
