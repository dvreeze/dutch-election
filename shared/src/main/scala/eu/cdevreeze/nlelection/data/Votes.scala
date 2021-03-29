/*
 * Copyright 2021-2021 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.nlelection.data

import scala.collection.immutable.SeqMap

import eu.cdevreeze.nlelection.data.Votes.ReasonCode

/**
 * Votes, either for a reporting unit or in total.
 *
 * @author Chris de Vreeze
 */
sealed trait Votes {

  def reportingUnitIdOption: Option[ReportingUnitId]

  def selections: Seq[Selection]

  def votesCast: Long

  def totalCounted: Long

  def rejectedVotes: SeqMap[ReasonCode, Long]

  def uncountedVotes: SeqMap[ReasonCode, Long]

  final def selectionsGroupedByAffiliationId: Map[AffiliationId, Seq[Selection]] = selections.groupBy(_.affiliationId)

  final def isConsistentRegardingValidVotes(affiliationId: AffiliationId): Boolean = {
    val selectionsForAffiliation: Seq[Selection] = selections.filter(_.affiliationId == affiliationId)

    selectionsForAffiliation.filter(_.candidateKeyOption.nonEmpty).map(_.validVotes).sum ==
      selectionsForAffiliation.filter(_.candidateKeyOption.isEmpty).map(_.validVotes).sum
  }

  final def isConsistentRegardingValidVotes: Boolean = {
    selectionsGroupedByAffiliationId.keySet.forall(affId => isConsistentRegardingValidVotes(affId))
  }
}

final case class TotalVotes(
    selections: Seq[Selection],
    votesCast: Long,
    totalCounted: Long,
    rejectedVotes: SeqMap[ReasonCode, Long],
    uncountedVotes: SeqMap[ReasonCode, Long])
    extends Votes {

  def reportingUnitIdOption: Option[ReportingUnitId] = None
}

final case class ReportingUnitVotes(
    reportingUnitId: ReportingUnitId,
    selections: Seq[Selection],
    votesCast: Long,
    totalCounted: Long,
    rejectedVotes: SeqMap[ReasonCode, Long],
    uncountedVotes: SeqMap[ReasonCode, Long])
    extends Votes {

  def reportingUnitIdOption: Option[ReportingUnitId] = Some(reportingUnitId)
}

object Votes {

  /**
   * ReasonCode for votes being uncounted or rejected.
   */
  final case class ReasonCode(value: String) extends AnyVal
}
