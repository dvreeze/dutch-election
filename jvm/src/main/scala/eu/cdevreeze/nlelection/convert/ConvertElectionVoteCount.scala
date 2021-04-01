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

package eu.cdevreeze.nlelection.convert

import java.io.File

import scala.util.Using

import com.github.tototoshi.csv.CSVWriter
import eu.cdevreeze.nlelection.common.ENames
import eu.cdevreeze.nlelection.data.ElectionVoteCount
import eu.cdevreeze.nlelection.data.VoteCountSelection
import eu.cdevreeze.nlelection.parse.ElectionVoteCountParser
import eu.cdevreeze.yaidom2.node.saxon.SaxonDocument
import eu.cdevreeze.yaidom2.node.saxon.SaxonNodes
import net.sf.saxon.s9api.Processor

/**
 * Converter of election vote count data in EML XML to CSV.
 *
 * @author Chris de Vreeze
 */
object ConvertElectionVoteCount {

  private val saxonProcessor: Processor = new Processor(false)

  def parseNestedElectionVoteCount(elem: SaxonNodes.Elem): ElectionVoteCount = {
    parseElectionVoteCount(elem.findDescendantElemOrSelf(_.name == ENames.EmlElectionEName).get)
  }

  def parseElectionVoteCount(elem: SaxonNodes.Elem): ElectionVoteCount = {
    ElectionVoteCountParser.parseElectionVoteCount(elem)
  }

  def convertElectionVoteCountToCsvWithHeader(election: ElectionVoteCount): Seq[Seq[String]] = {
    val header =
      Seq(
        "ElectionKey",
        "ContestId",
        "ContestName",
        "KindOfVotes (Total/ReportingUnit)",
        "ReportingUnit",
        "KindOfSelection (Candidate/Affiliation)",
        "AffiliationId",
        "Candidate",
        "ValidVotes"
      )

    (for {
      electionId <- Seq(election.electionId)
      contest <- election.contests
      votes <- contest.votesSeq
      selection <- votes.selections
    } yield {
      val kindOfSelection: String = selection match {
        case VoteCountSelection.OfCandidate(_, _) => "Cnd"
        case _                                    => "Aff"
      }

      Seq[String](
        electionId.key,
        contest.contestId.id,
        contest.contestId.contestNameOption.getOrElse(""),
        if (votes.reportingUnitIdOption.isEmpty) "Tot" else "Rep",
        votes.reportingUnitIdOption.map(_.id).getOrElse(""),
        kindOfSelection,
        selection.affiliationId.id,
        selection.candidateKeyOption.map(_.candidateId).getOrElse(""),
        selection.validVotes.toString
      )
    }).prepended(header)
  }

  def main(args: Array[String]): Unit = {
    require(args.lengthIs == 2, s"Usage: ConvertElection <input XML file> <output CSV file>")

    val inputFile: File = new File(args(0)).ensuring(_.isFile)
    val outputFile: File = new File(args(1))

    Using.Manager { use =>
      val csvWriter: CSVWriter = use(CSVWriter.open(outputFile))

      val doc: SaxonDocument = SaxonDocument(saxonProcessor.newDocumentBuilder().build(inputFile))

      val election: ElectionVoteCount = parseNestedElectionVoteCount(doc.documentElement)

      require(election.contests.forall(_.isConsistentRegardingValidVotes), s"Inconsistencies found in totals of valid vote counts")

      val csvRows: Seq[Seq[String]] = convertElectionVoteCountToCsvWithHeader(election)

      csvWriter.writeAll(csvRows)
    }.get
  }
}
