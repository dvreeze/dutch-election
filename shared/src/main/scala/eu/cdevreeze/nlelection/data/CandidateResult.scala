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
 * Election result for a candidate. It corresponds to an eml:Selection element containing an eml:Candidate child element
 * where the eml:Selection element has an eml:Result element as ancestor element.
 *
 * @author Chris de Vreeze
 */
final case class CandidateResult(candidate: Candidate, rankingOption: Option[Long], elected: Boolean)
