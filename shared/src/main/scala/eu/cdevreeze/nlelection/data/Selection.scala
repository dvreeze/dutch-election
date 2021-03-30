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
 * Selection of valid votes of a candidate or an affiliation.
 *
 * @author Chris de Vreeze
 */
sealed trait Selection {

  def affiliationId: AffiliationId

  def candidateKeyOption: Option[CandidateKey]

  def validVotes: Long

  def sortKey: (Long, Long) = {
    (affiliationId.id.toLongOption.getOrElse(0L), candidateKeyOption.flatMap(_.candidateId.toLongOption).getOrElse(0L))
  }
}

object Selection {

  final case class OfCandidate(candidateKey: CandidateKey, validVotes: Long) extends Selection {

    override def affiliationId: AffiliationId = candidateKey.affiliationId

    override def candidateKeyOption: Option[CandidateKey] = Some(candidateKey)
  }

  final case class OfAffiliation(affiliationId: AffiliationId, validVotes: Long) extends Selection {

    override def candidateKeyOption: Option[CandidateKey] = None
  }
}
