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
import scala.collection.immutable.SeqMap

import eu.cdevreeze.nlelection.common.ENames
import eu.cdevreeze.nlelection.data.AffiliationId
import eu.cdevreeze.nlelection.data.CandidateKey
import eu.cdevreeze.nlelection.data.ContestVoteCount
import eu.cdevreeze.nlelection.data.ContestId
import eu.cdevreeze.nlelection.data.ElectionVoteCount
import eu.cdevreeze.nlelection.data.ElectionId
import eu.cdevreeze.nlelection.data.ReportingUnitId
import eu.cdevreeze.nlelection.data.ReportingUnitVotesSection
import eu.cdevreeze.nlelection.data.TotalVotesSection
import eu.cdevreeze.nlelection.data.VoteCountSection
import eu.cdevreeze.nlelection.data.VoteCountSection.ReasonCode
import eu.cdevreeze.nlelection.data.VoteCountSelection
import eu.cdevreeze.yaidom2.queryapi.BackingNodes

/**
 * Parser of election vote counts with its contest(s) in EML XML.
 *
 * @author Chris de Vreeze
 */
object ElectionVoteCountParser {

  import ENames._

  def parse(elem: BackingNodes.Elem): ElectionVoteCount = {
    require(elem.name == EmlElectionEName, s"Expected $EmlElectionEName but got ${elem.name}")

    val contests: Seq[ContestVoteCount] =
      elem.filterChildElems(_.name == EmlContestsEName).flatMap(_.filterChildElems(_.name == EmlContestEName)).map(parseContestVoteCount)

    ElectionVoteCount(parseElectionIdentifier(elem.findChildElem(_.name == EmlElectionIdentifierEName).get), contests)
  }

  def parseElectionIdentifier(elem: BackingNodes.Elem): ElectionId = {
    ElectionIdentifierParser.parseElectionIdentifier(elem)
  }

  def parseContestVoteCount(elem: BackingNodes.Elem): ContestVoteCount = {
    require(elem.name == EmlContestEName, s"Expected $EmlContestEName but got ${elem.name}")

    ContestVoteCount.from(
      parseContestId(elem.findChildElem(_.name == EmlContestIdentifierEName).get),
      parseTotalVotesSection(elem.findChildElem(_.name == EmlTotalVotesEName).get),
      elem.filterChildElems(_.name == EmlReportingUnitVotesEName).map(parseReportingUnitVotesSection)
    )
  }

  def parseContestId(elem: BackingNodes.Elem): ContestId = {
    require(elem.name == EmlContestIdentifierEName, s"Expected $EmlContestIdentifierEName but got ${elem.name}")

    ContestId(elem.attr(IdEName), elem.findChildElem(_.name == EmlContestNameEName).map(_.text))
  }

  def parseVoteCountSection(elem: BackingNodes.Elem): VoteCountSection = {
    if (elem.name == EmlTotalVotesEName) {
      parseTotalVotesSection(elem)
    } else {
      parseReportingUnitVotesSection(elem)
    }
  }

  def parseTotalVotesSection(elem: BackingNodes.Elem): TotalVotesSection = {
    require(elem.name == EmlTotalVotesEName, s"Expected $EmlTotalVotesEName but got ${elem.name}")

    doParseTotalVotesSection(elem)
  }

  def parseReportingUnitVotesSection(elem: BackingNodes.Elem): ReportingUnitVotesSection = {
    require(elem.name == EmlReportingUnitVotesEName, s"Expected $EmlReportingUnitVotesEName but got ${elem.name}")

    val resultAsTotalVotes = doParseTotalVotesSection(elem)

    val reportingUnitIdElem: BackingNodes.Elem = elem.findChildElem(_.name == EmlReportingUnitIdentifierEName).get
    val reportingUnitId: ReportingUnitId = ReportingUnitId(reportingUnitIdElem.attr(IdEName), reportingUnitIdElem.text)

    ReportingUnitVotesSection(
      reportingUnitId,
      resultAsTotalVotes.selections,
      resultAsTotalVotes.votesCast,
      resultAsTotalVotes.totalCounted,
      resultAsTotalVotes.rejectedVotes,
      resultAsTotalVotes.uncountedVotes
    )
  }

  def parseVoteCountSelection(elem: BackingNodes.Elem, contextAffiliationId: AffiliationId): VoteCountSelection = {
    require(elem.name == EmlSelectionEName, s"Expected $EmlSelectionEName but got ${elem.name}")

    val validVotes: Long = elem.findChildElem(_.name == EmlValidVotesEName).get.text.toLong

    if (elem.findChildElem(_.name == EmlAffiliationIdentifierEName).isEmpty) {
      VoteCountSelection.OfCandidate(
        parseCandidateKey(elem.findChildElem(_.name == EmlCandidateEName).get, contextAffiliationId),
        validVotes)
    } else {
      VoteCountSelection.OfAffiliation(parseAffiliationId(elem.findChildElem(_.name == EmlAffiliationIdentifierEName).get), validVotes)
    }
  }

  def parseCandidateKey(elem: BackingNodes.Elem, contextAffiliationId: AffiliationId): CandidateKey = {
    require(elem.name == EmlCandidateEName, s"Expected $EmlCandidateEName but got ${elem.name}")

    CandidateKey(
      contextAffiliationId,
      elem.findChildElem(_.name == EmlCandidateIdentifierEName).flatMap(_.attrOption(IdEName)).getOrElse(""),
      elem.findChildElem(_.name == EmlCandidateIdentifierEName).flatMap(_.attrOption(ShortCodeEName))
    )
  }

  def parseAffiliationId(elem: BackingNodes.Elem): AffiliationId = {
    require(elem.name == EmlAffiliationIdentifierEName, s"Expected $EmlAffiliationIdentifierEName but got ${elem.name}")

    AffiliationId(elem.attr(IdEName), elem.findChildElem(_.name == EmlRegisteredNameEName).get.text)
  }

  private def collectCandidateGroups(elems: Seq[BackingNodes.Elem]): Seq[CandidateGroup] = {
    collectCandidateGroups(elems, Seq.empty)
  }

  @tailrec
  private def collectCandidateGroups(elems: Seq[BackingNodes.Elem], acc: Seq[CandidateGroup]): Seq[CandidateGroup] = {
    if (elems.isEmpty) {
      acc
    } else {
      require(elems.head.findChildElem(_.name == EmlAffiliationIdentifierEName).nonEmpty)

      val (candidateElemsInGroup, remainder) = elems.drop(1).span(_.findChildElem(_.name == EmlCandidateEName).nonEmpty)

      // Recursive call
      collectCandidateGroups(remainder, acc.appended(new CandidateGroup(elems.head, candidateElemsInGroup)))
    }
  }

  private def doParseTotalVotesSection(elem: BackingNodes.Elem): TotalVotesSection = {
    val candidateGroups: Seq[CandidateGroup] = collectCandidateGroups(elem.filterChildElems(_.name == EmlSelectionEName))

    val selections: Seq[VoteCountSelection] = candidateGroups.flatMap(_.parseSelections())

    TotalVotesSection(
      selections,
      elem.findChildElem(_.name == EmlCastEName).get.text.toLong,
      elem.findChildElem(_.name == EmlTotalCountedEName).get.text.toLong,
      elem.filterChildElems(_.name == EmlRejectedVotesEName).map(e => ReasonCode(e.attr(ReasonCodeEName)) -> e.text.toLong).to(SeqMap),
      elem.filterChildElems(_.name == EmlUncountedVotesEName).map(e => ReasonCode(e.attr(ReasonCodeEName)) -> e.text.toLong).to(SeqMap),
    )
  }

  private final class CandidateGroup(val affiliationElem: BackingNodes.Elem, val candidateElems: Seq[BackingNodes.Elem]) {

    def parseSelections(): Seq[VoteCountSelection] = {
      val affiliationId = parseAffiliationId(affiliationElem.findChildElem(_.name == EmlAffiliationIdentifierEName).get)

      val affiliationSelection: VoteCountSelection = parseVoteCountSelection(affiliationElem, affiliationId)

      val candidateSelections: Seq[VoteCountSelection] = candidateElems.map(e => parseVoteCountSelection(e, affiliationId))

      candidateSelections.prepended(affiliationSelection)
    }
  }
}
