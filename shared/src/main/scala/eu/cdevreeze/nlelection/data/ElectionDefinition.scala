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
 * Election definition (or part of it), containing foremost an election tree and the registered parties.
 * It corresponds to an eml:Election element as used in the context of an eml:ElectionEvent parent element.
 * It typically contains www.kiesraad.nl extensions to EML.
 *
 * @author Chris de Vreeze
 */
final case class ElectionDefinition(
    electionIdentifier: ElectionId,
    contest: String,
    electionTree: ElectionTree,
    registeredParties: Seq[String])
