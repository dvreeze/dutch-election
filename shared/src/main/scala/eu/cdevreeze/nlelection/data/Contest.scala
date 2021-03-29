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
 * Contest, with all its valid votes.
 *
 * @author Chris de Vreeze
 */
final case class Contest private (
    contestId: ContestId,
    totalVotes: TotalVotes,
    reportingUnitVotesMap: SeqMap[ReportingUnitId, ReportingUnitVotes]) {

  assert(reportingUnitVotesMap.forall(kv => kv._2.reportingUnitId == kv._1))

  def votesMap: SeqMap[Option[ReportingUnitId], Votes] = {
    val optReportingUnitVotesMap: Map[Option[ReportingUnitId], Votes] =
      reportingUnitVotesMap.toSeq.map(kv => Option(kv._1) -> kv._2).to(SeqMap)

    SeqMap(Option.empty[ReportingUnitId] -> totalVotes).concat(optReportingUnitVotesMap)
  }

  def votesSeq: Seq[Votes] = reportingUnitVotesMap.values.toSeq.prepended(totalVotes)

  // Inefficient computations, but that's not a problem until it's a problem

  def isConsistentRegardingValidAffiliationVotes(affiliationId: AffiliationId): Boolean = {
    val validVotesPerOptReportingUnit: Map[Option[ReportingUnitId], Long] =
      votesMap.view
        .mapValues(_.selections.filter(sel => sel.candidateKeyOption.isEmpty && sel.affiliationId == affiliationId).map(_.validVotes).sum)
        .toMap

    validVotesPerOptReportingUnit.getOrElse(None, 0L) ==
      validVotesPerOptReportingUnit.filterNot(_._1.isEmpty).values.sum
  }

  def isConsistentRegardingValidAffiliationVotes: Boolean = {
    val affiliationIds: Set[AffiliationId] = votesSeq.flatMap(_.selections).map(_.affiliationId).toSet

    affiliationIds.forall(affId => isConsistentRegardingValidAffiliationVotes(affId))
  }

  def isConsistentRegardingValidVotes: Boolean = {
    votesSeq.forall(_.isConsistentRegardingValidVotes) && isConsistentRegardingValidAffiliationVotes
  }
}

object Contest {

  def from(contestId: ContestId, totalVotes: TotalVotes, reportingUnitVotesSeq: Seq[ReportingUnitVotes]): Contest = {
    Contest(contestId, totalVotes, reportingUnitVotesSeq.map(votes => votes.reportingUnitId -> votes).to(SeqMap))
  }
}
