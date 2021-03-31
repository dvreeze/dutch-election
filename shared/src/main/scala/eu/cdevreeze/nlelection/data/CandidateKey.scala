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
 * Candidate key, consisting of an affiliation ID plus candidate ID which is assumed to be unique within the affiliation.
 * It can occur in just about any kind of EML XML document.
 *
 * The ID is assumed to be unique given an affiliation ID.
 *
 * It corresponds to an eml:CandidateIdentifier element in EML XML, within or preceded (as sibling) by an eml:AffiliationIdentifier
 * element as context.
 *
 * @author Chris de Vreeze
 */
final case class CandidateKey(affiliationId: AffiliationId, candidateId: String, shortCodeOption: Option[String])
