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

import scala.annotation.tailrec

import eu.cdevreeze.nlelection.common.ENames
import eu.cdevreeze.nlelection.data.ElectionResult
import eu.cdevreeze.yaidom2.queryapi.BackingNodes
import scala.util.chaining._

import eu.cdevreeze.nlelection.data.AffiliationId
import eu.cdevreeze.nlelection.data.AffiliationResult
import eu.cdevreeze.nlelection.data.CandidateResult
import eu.cdevreeze.nlelection.data.ContestResult

/**
 * Parser of election results and their parts in EML XML. Results are recognized in EML XML by an
 * eml:Result child element of the eml:EML root element.
 *
 * @author Chris de Vreeze
 */
object ElectionResultParser {

  import ENames._

  /**
   * Parses an XML element into an ElectionResult. The XML tree must contain an eml:Result element, and that's where
   * parsing starts.
   */
  def parse(elem: BackingNodes.Elem): ElectionResult = {
    require(elem.findDescendantElemOrSelf(_.name == EmlResultEName).nonEmpty, s"Expected $EmlResultEName 'somewhere'")
    val resultElem: BackingNodes.Elem = elem.findDescendantElemOrSelf(_.name == EmlResultEName).get

    parseElectionResult(resultElem.findChildElem(_.name == EmlElectionEName).get)
  }

  def parseElectionResult(elem: BackingNodes.Elem): ElectionResult = {
    require(elem.name == EmlElectionEName, s"Expected $EmlElectionEName but got ${elem.name}")

    ElectionResult(
      elem.findChildElem(_.name == EmlElectionIdentifierEName).get.pipe(ElectionIdentifierParser.parseElectionIdentifier),
      elem.filterChildElems(_.name == EmlContestEName).map(parseContestResult)
    )
  }

  def parseContestResult(elem: BackingNodes.Elem): ContestResult = {
    require(elem.name == EmlContestEName, s"Expected $EmlContestEName but got ${elem.name}")

    val affiliationResults: Seq[AffiliationResult] = collectCandidateGroups(elem.filterChildElems(_.name == EmlSelectionEName))
      .map(_.parseSelections)

    ContestResult(CandidateListParser.parseContestId(elem.findChildElem(_.name == EmlContestIdentifierEName).get), affiliationResults)
  }

  private def collectCandidateGroups(selectionElems: Seq[BackingNodes.Elem]): Seq[CandidateGroup] = {
    require(selectionElems.forall(_.name == EmlSelectionEName))

    collectCandidateGroups(selectionElems, Seq.empty)
  }

  @tailrec
  private def collectCandidateGroups(selectionElems: Seq[BackingNodes.Elem], acc: Seq[CandidateGroup]): Seq[CandidateGroup] = {
    if (selectionElems.isEmpty) {
      acc
    } else {
      require(selectionElems.head.findChildElem(_.name == EmlAffiliationIdentifierEName).nonEmpty)

      val (candidateElemsInGroup, remainder) = selectionElems.drop(1).span(_.findChildElem(_.name == EmlCandidateEName).nonEmpty)

      // Recursive call
      collectCandidateGroups(remainder, acc.appended(new CandidateGroup(selectionElems.head, candidateElemsInGroup)))
    }
  }

  private final class CandidateGroup(val affiliationSelectionElem: BackingNodes.Elem, val candidateSelectionElems: Seq[BackingNodes.Elem]) {
    require(affiliationSelectionElem.name == EmlSelectionEName)
    require(candidateSelectionElems.forall(_.name == EmlSelectionEName))

    def parseSelections: AffiliationResult = {
      val affiliationId =
        CandidateListParser.parseAffiliationId(affiliationSelectionElem.findChildElem(_.name == EmlAffiliationIdentifierEName).get)

      val electedElemOption: Option[BackingNodes.Elem] = affiliationSelectionElem.findChildElem(_.name == EmlElectedEName)
      val elected: Boolean = electedElemOption.exists(_.text == "yes")

      val candidateResults: Seq[CandidateResult] = candidateSelectionElems.map(e => parseCandidateResult(e, affiliationId))

      AffiliationResult(affiliationId, elected, candidateResults)
    }
  }

  private def parseCandidateResult(elem: BackingNodes.Elem, affiliationId: AffiliationId): CandidateResult = {
    require(elem.name == EmlSelectionEName, s"Expected $EmlSelectionEName but got ${elem.name}")

    CandidateResult(
      CandidateListParser.parseCandidate(elem.findChildElem(_.name == EmlCandidateEName).get, affiliationId),
      elem.findChildElem(_.name == EmlRankingEName).map(_.text.toLong),
      elem.findChildElem(_.name == EmlElectedEName).map(_.text == "yes").getOrElse(false)
    )
  }
}
