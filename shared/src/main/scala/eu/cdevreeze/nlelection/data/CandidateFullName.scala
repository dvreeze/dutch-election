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
 * Candidate full name, occurring in a candidate list. This corresponds to an eml:CandidateFullName element within an eml:Candidate
 * element that in turn has an eml:CandidateList as context.
 *
 * The candidate full name is filled with data coming from "xnl" extensions to EML.
 *
 * @author Chris de Vreeze
 */
final case class CandidateFullName(
    initialsOption: Option[String],
    firstNameOption: Option[String],
    namePrefixOption: Option[String],
    lastName: String)
