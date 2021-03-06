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
import eu.cdevreeze.nlelection.data.ElectionVoteCount
import eu.cdevreeze.nlelection.data.VoteCountSelection
import eu.cdevreeze.nlelection.parse.ElectionVoteCountParser
import eu.cdevreeze.yaidom2.node.saxon.SaxonDocument
import eu.cdevreeze.yaidom2.node.saxon.SaxonNodes
import net.sf.saxon.s9api.Processor

/**
 * Converter of election vote count data in EML XML to CSV.
 *
 * These input XML files have an eml:Count child element of the eml:EML root element.
 *
 * In the case of Dutch "Tweede Kamer" elections there are vote count files per municipality, per "kieskring" (a region
 * combining several municipalities), and overall for the entire country. The latter file is only slightly different from
 * the other ones, in that they have an extra ElectionDomain element but miss the ContestName element.
 *
 * Currently the uncounted votes, cast votes etc. are not output (to a second CSV file), neither by this nor by any other program.
 *
 * @author Chris de Vreeze
 */
object ConvertElectionVoteCount {

  private val saxonProcessor: Processor = new Processor(false)

  def parseElectionVoteCount(elem: SaxonNodes.Elem): ElectionVoteCount = {
    ElectionVoteCountParser.parse(elem)
  }

  def convertElectionVoteCountToCsvWithHeader(election: ElectionVoteCount): Seq[Seq[String]] = {
    val header =
      Seq(
        "ElectionKey",
        "ContestId",
        "ContestName",
        "KindOfVotes (Total/ReportingUnit)",
        "ReportingUnit",
        "ReportingUnitDescription",
        "KindOfSelection (Candidate/Affiliation)",
        "AffiliationId",
        "Candidate",
        "Candidate shortcode",
        "ValidVotes"
      )

    (for {
      electionId <- Seq(election.electionId)
      contest <- election.contests
      votes <- contest.votesSeq
      (selection, idx) <- votes.selections.zipWithIndex
    } yield {
      val kindOfSelection: String = selection match {
        case VoteCountSelection.OfCandidate(_, _) => "Cnd"
        case _                                    => "Aff"
      }

      val reportingUnitDescriptionOption: Option[String] =
        if (idx == 0) {
          votes.reportingUnitIdOption.map(_.description)
        } else {
          None
        }

      Seq[String](
        electionId.key,
        contest.contestId.id,
        contest.contestId.contestNameOption.getOrElse(""),
        if (votes.reportingUnitIdOption.isEmpty) "Tot" else "Rep",
        votes.reportingUnitIdOption.map(_.id).getOrElse(""),
        reportingUnitDescriptionOption.getOrElse(""),
        kindOfSelection,
        selection.affiliationId.id,
        selection.candidateKeyOption.map(_.candidateId).getOrElse(""),
        selection.candidateKeyOption.flatMap(_.shortCodeOption).getOrElse(""),
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

      val election: ElectionVoteCount = parseElectionVoteCount(doc.documentElement)

      require(election.contests.forall(_.isConsistentRegardingValidVotes), s"Inconsistencies found in totals of valid vote counts")

      val csvRows: Seq[Seq[String]] = convertElectionVoteCountToCsvWithHeader(election)

      csvWriter.writeAll(csvRows)
    }.get
  }
}
