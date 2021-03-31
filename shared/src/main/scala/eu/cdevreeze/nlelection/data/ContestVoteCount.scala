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

/**
 * The equivalent of an eml:Contest element within an eml:Count context. The eml:Contest elements in this context
 * contain eml:TotalVotes and eml:ReportingUnitVotes child elements holding vote count data.
 *
 * @author Chris de Vreeze
 */
final case class ContestVoteCount private (
    contestId: ContestId,
    totalVotes: TotalVotesSection,
    reportingUnitVotesMap: SeqMap[ReportingUnitId, ReportingUnitVotesSection]) {

  assert(reportingUnitVotesMap.forall(kv => kv._2.reportingUnitId == kv._1))

  def votesMap: SeqMap[Option[ReportingUnitId], VotesSection] = {
    val optReportingUnitVotesMap: Map[Option[ReportingUnitId], VotesSection] =
      reportingUnitVotesMap.toSeq.map(kv => Option(kv._1) -> kv._2).to(SeqMap)

    SeqMap(Option.empty[ReportingUnitId] -> totalVotes).concat(optReportingUnitVotesMap)
  }

  def votesSeq: Seq[VotesSection] = reportingUnitVotesMap.values.toSeq.prepended(totalVotes)

  def isConsistentRegardingValidVotesAcrossUnits(affiliationId: AffiliationId): Boolean = {
    val totalVotesForAffiliation: Long = totalVotes.filterNonCandidateSelectionsByAffiliationId(affiliationId).map(_.validVotes).sum

    val reportingUnitVotesForAffiliation: Long =
      reportingUnitVotesMap.values.flatMap(_.filterNonCandidateSelectionsByAffiliationId(affiliationId)).map(_.validVotes).sum

    totalVotesForAffiliation == reportingUnitVotesForAffiliation
  }

  def isConsistentRegardingValidVotesAcrossUnits: Boolean = {
    val affiliationIds: Set[AffiliationId] = totalVotes.selectionsByAffiliationId.keySet.toSeq
      .appendedAll(reportingUnitVotesMap.values.flatMap(_.selectionsByAffiliationId.keySet.toSeq))
      .toSet

    affiliationIds.forall(affId => isConsistentRegardingValidVotesAcrossUnits(affId))
  }

  def isConsistentRegardingValidVotes: Boolean = {
    votesSeq.forall(_.isConsistentRegardingValidVotes) && isConsistentRegardingValidVotesAcrossUnits
  }
}

object ContestVoteCount {

  def from(contestId: ContestId, totalVotes: TotalVotesSection, reportingUnitVotesSeq: Seq[ReportingUnitVotesSection]): ContestVoteCount = {
    ContestVoteCount(contestId, totalVotes, reportingUnitVotesSeq.map(votes => votes.reportingUnitId -> votes).to(SeqMap))
  }
}
