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

package eu.cdevreeze.nlelection.parse

import eu.cdevreeze.nlelection.common.ENames
import eu.cdevreeze.nlelection.data.Affiliation
import eu.cdevreeze.nlelection.data.Candidate
import eu.cdevreeze.nlelection.data.CandidateList
import eu.cdevreeze.nlelection.data.Contest
import eu.cdevreeze.nlelection.data.Election
import eu.cdevreeze.nlelection.data.ElectionId
import eu.cdevreeze.yaidom2.queryapi.BackingNodes
import scala.util.chaining._

import eu.cdevreeze.nlelection.data.AffiliationId
import eu.cdevreeze.nlelection.data.CandidateFullName
import eu.cdevreeze.nlelection.data.CandidateKey
import eu.cdevreeze.nlelection.data.ContestId
import eu.cdevreeze.nlelection.data.Gender
import eu.cdevreeze.nlelection.data.QualifyingAddress

/**
 * Parser of candidate lists and their parts in EML XML. Candidate lists are recognized in EML XML by an
 * eml:CandidateList child element of the eml:EML root element.
 *
 * @author Chris de Vreeze
 */
object CandidateListParser {

  import ENames._

  /**
   * Parses an XML element into a CandidateList. The XML tree must contain an eml:CandidateList element, and that's where
   * parsing starts.
   */
  def parse(elem: BackingNodes.Elem): CandidateList = {
    require(elem.findDescendantElemOrSelf(_.name == EmlCandidateListEName).nonEmpty, s"Expected $EmlCandidateListEName 'somewhere'")
    val candidateListElem: BackingNodes.Elem = elem.findDescendantElemOrSelf(_.name == EmlCandidateListEName).get

    CandidateList(candidateListElem.filterChildElems(_.name == EmlElectionEName).map(parseElection))
  }

  def parseElection(elem: BackingNodes.Elem): Election = {
    require(elem.name == EmlElectionEName, s"Expected $EmlElectionEName but got ${elem.name}")

    Election(
      elem.findChildElem(_.name == EmlElectionIdentifierEName).get.pipe(parseElectionIdentifier),
      elem.filterChildElems(_.name == EmlContestEName).map(parseContest)
    )
  }

  def parseContest(elem: BackingNodes.Elem): Contest = {
    require(elem.name == EmlContestEName, s"Expected $EmlContestEName but got ${elem.name}")

    Contest(
      parseContestId(elem.findChildElem(_.name == EmlContestIdentifierEName).get),
      elem.filterChildElems(_.name == EmlAffiliationEName).map(parseAffiliation))
  }

  def parseAffiliation(elem: BackingNodes.Elem): Affiliation = {
    require(elem.name == EmlAffiliationEName, s"Expected $EmlAffiliationEName but got ${elem.name}")

    val affiliationId: AffiliationId = elem.findChildElem(_.name == EmlAffiliationIdentifierEName).get.pipe(parseAffiliationId)

    Affiliation(
      affiliationId,
      elem.filterChildElems(_.name == EmlCandidateEName).map(e => parseCandidate(e, affiliationId))
    )
  }

  def parseCandidate(elem: BackingNodes.Elem, affiliationId: AffiliationId): Candidate = {
    require(elem.name == EmlCandidateEName, s"Expected $EmlCandidateEName but got ${elem.name}")

    Candidate(
      parseCandidateKey(elem.findChildElem(_.name == EmlCandidateIdentifierEName).get, affiliationId),
      elem.findChildElem(_.name == EmlCandidateFullNameEName).get.pipe(parseCandidateFullName),
      elem.findChildElem(_.name == EmlGenderEName).map(_.text).flatMap {
        case "male" | "Male"     => Some(Gender.Male)
        case "female" | "Female" => Some(Gender.Female)
        case _                   => None
      },
      elem.findChildElem(_.name == EmlQualifyingAddressEName).map(parseQualifyingAddress)
    )
  }

  def parseCandidateFullName(elem: BackingNodes.Elem): CandidateFullName = {
    require(elem.name == EmlCandidateFullNameEName, s"Expected $EmlCandidateFullNameEName but got ${elem.name}")

    CandidateFullName(
      elem.findDescendantElem(e => e.name == XnlNameLineEName && e.attrOption(NameTypeEName).contains("Initials")).map(_.text),
      elem.findDescendantElem(_.name == XnlFirstNameEName).map(_.text),
      elem.findDescendantElem(_.name == XnlLastNameEName).get.text
    )
  }

  def parseQualifyingAddress(elem: BackingNodes.Elem): QualifyingAddress = {
    require(elem.name == EmlQualifyingAddressEName, s"Expected $EmlQualifyingAddressEName but got ${elem.name}")

    QualifyingAddress(elem.findDescendantElem(_.name == XalLocalityNameEName).map(_.text).getOrElse(""))
  }

  def parseElectionIdentifier(elem: BackingNodes.Elem): ElectionId = {
    ElectionIdentifierParser.parseElectionIdentifier(elem)
  }

  def parseContestId(elem: BackingNodes.Elem): ContestId = {
    require(elem.name == EmlContestIdentifierEName, s"Expected $EmlContestIdentifierEName but got ${elem.name}")

    ContestId(elem.attr(IdEName), elem.findChildElem(_.name == EmlContestNameEName).map(_.text))
  }

  def parseAffiliationId(elem: BackingNodes.Elem): AffiliationId = {
    require(elem.name == EmlAffiliationIdentifierEName, s"Expected $EmlAffiliationIdentifierEName but got ${elem.name}")

    AffiliationId(elem.attr(IdEName), elem.findChildElem(_.name == EmlRegisteredNameEName).get.text)
  }

  def parseCandidateKey(elem: BackingNodes.Elem, affiliationId: AffiliationId): CandidateKey = {
    require(elem.name == EmlCandidateIdentifierEName, s"Expected $EmlCandidateIdentifierEName but got ${elem.name}")

    CandidateKey(
      affiliationId,
      elem.attrOption(IdEName).getOrElse(""),
      elem.attrOption(ShortCodeEName)
    )
  }
}
