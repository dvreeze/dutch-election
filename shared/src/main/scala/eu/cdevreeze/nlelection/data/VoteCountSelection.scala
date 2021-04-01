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

/**
 * Selection of valid votes of a candidate or an affiliation. It corresponds to an eml:Selection element as it occurs
 * as child element of an eml:ReportingUnitVotes element or an eml:TotalVotes element, within an eml:Count context.
 *
 * @author Chris de Vreeze
 */
sealed trait VoteCountSelection {

  def affiliationId: AffiliationId

  def candidateKeyOption: Option[CandidateKey]

  def validVotes: Long

  def sortKey: (Long, Long) = {
    (affiliationId.id.toLongOption.getOrElse(0L), candidateKeyOption.flatMap(_.candidateId.toLongOption).getOrElse(0L))
  }
}

object VoteCountSelection {

  final case class OfCandidate(candidateKey: CandidateKey, validVotes: Long) extends VoteCountSelection {

    override def affiliationId: AffiliationId = candidateKey.affiliationId

    override def candidateKeyOption: Option[CandidateKey] = Some(candidateKey)
  }

  final case class OfAffiliation(affiliationId: AffiliationId, validVotes: Long) extends VoteCountSelection {

    override def candidateKeyOption: Option[CandidateKey] = None
  }
}
