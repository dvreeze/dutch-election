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

import eu.cdevreeze.nlelection.data.VotesSection.ReasonCode

/**
 * Votes section, either for a reporting unit or in total. It corresponds to an eml:ReportingUnitVotes element or an
 * eml:TotalVotes element, within an eml:Count context.
 *
 * @author Chris de Vreeze
 */
sealed trait VotesSection {

  def reportingUnitIdOption: Option[ReportingUnitId]

  def selectionsByAffiliationId: Map[AffiliationId, Seq[VotesSelection]]

  def votesCast: Long

  def totalCounted: Long

  def rejectedVotes: SeqMap[ReasonCode, Long]

  def uncountedVotes: SeqMap[ReasonCode, Long]

  final def selections: Seq[VotesSelection] = {
    selectionsByAffiliationId.values.flatten.toSeq.sortBy(_.sortKey)
  }

  final def filterSelectionsByAffiliationId(affiliationId: AffiliationId): Seq[VotesSelection] = {
    selectionsByAffiliationId.getOrElse(affiliationId, Seq.empty)
  }

  final def filterNonCandidateSelectionsByAffiliationId(affiliationId: AffiliationId): Seq[VotesSelection] = {
    filterSelectionsByAffiliationId(affiliationId).filter(_.candidateKeyOption.isEmpty)
  }

  final def isConsistentRegardingValidVotes(affiliationId: AffiliationId): Boolean = {
    val selectionsForAffiliation: Seq[VotesSelection] = filterSelectionsByAffiliationId(affiliationId)

    selectionsForAffiliation.filter(_.candidateKeyOption.nonEmpty).map(_.validVotes).sum ==
      selectionsForAffiliation.filter(_.candidateKeyOption.isEmpty).map(_.validVotes).sum
  }

  final def isConsistentRegardingValidVotes: Boolean = {
    selectionsByAffiliationId.keySet.forall(affId => isConsistentRegardingValidVotes(affId))
  }
}

final case class TotalVotesSection(
    selectionsByAffiliationId: Map[AffiliationId, Seq[VotesSelection]],
    votesCast: Long,
    totalCounted: Long,
    rejectedVotes: SeqMap[ReasonCode, Long],
    uncountedVotes: SeqMap[ReasonCode, Long])
    extends VotesSection {

  require(selectionsByAffiliationId.forall { case (affId, selections) => selections.forall(_.affiliationId == affId) })

  def reportingUnitIdOption: Option[ReportingUnitId] = None
}

object TotalVotesSection {

  def apply(
      selections: Seq[VotesSelection],
      votesCast: Long,
      totalCounted: Long,
      rejectedVotes: SeqMap[ReasonCode, Long],
      uncountedVotes: SeqMap[ReasonCode, Long]): TotalVotesSection = {

    apply(selections.groupBy(_.affiliationId), votesCast, totalCounted, rejectedVotes, uncountedVotes)
  }
}

final case class ReportingUnitVotesSection(
    reportingUnitId: ReportingUnitId,
    selectionsByAffiliationId: Map[AffiliationId, Seq[VotesSelection]],
    votesCast: Long,
    totalCounted: Long,
    rejectedVotes: SeqMap[ReasonCode, Long],
    uncountedVotes: SeqMap[ReasonCode, Long])
    extends VotesSection {

  require(selectionsByAffiliationId.forall { case (affId, selections) => selections.forall(_.affiliationId == affId) })

  def reportingUnitIdOption: Option[ReportingUnitId] = Some(reportingUnitId)
}

object ReportingUnitVotesSection {

  def apply(
      reportingUnitId: ReportingUnitId,
      selections: Seq[VotesSelection],
      votesCast: Long,
      totalCounted: Long,
      rejectedVotes: SeqMap[ReasonCode, Long],
      uncountedVotes: SeqMap[ReasonCode, Long]): ReportingUnitVotesSection = {

    apply(reportingUnitId, selections.groupBy(_.affiliationId), votesCast, totalCounted, rejectedVotes, uncountedVotes)
  }
}

object VotesSection {

  /**
   * ReasonCode for votes being uncounted or rejected.
   */
  final case class ReasonCode(value: String) extends AnyVal
}
