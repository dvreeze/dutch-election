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
import java.util.Locale

import scala.util.Using

import com.github.tototoshi.csv.CSVWriter
import eu.cdevreeze.nlelection.data.ElectionResult
import eu.cdevreeze.nlelection.parse.ElectionResultParser
import eu.cdevreeze.yaidom2.node.saxon.SaxonDocument
import eu.cdevreeze.yaidom2.node.saxon.SaxonNodes
import net.sf.saxon.s9api.Processor

/**
 * Converter of election results in EML XML to CSV.
 *
 * @author Chris de Vreeze
 */
object ConvertElectionResult {

  private val saxonProcessor: Processor = new Processor(false)

  def parseElectionResult(elem: SaxonNodes.Elem): ElectionResult = {
    ElectionResultParser.parse(elem)
  }

  def convertElectionResultToCsvWithHeader(electionResult: ElectionResult): Seq[Seq[String]] = {
    val header =
      Seq(
        "ElectionKey",
        "ContestId",
        "ContestName",
        "AffiliationId",
        "AffiliationName",
        "Candidate",
        "Candidate shortcode",
        "Ranking",
        "Elected",
        "Initials",
        "FirstName",
        "LastName",
        "Gender",
        "Address",
      )

    // TODO Show affiliation results without candidates as well

    (for {
      contestResult <- electionResult.contestResults
      affiliationResult <- contestResult.affiliationResults
      candidateResult <- affiliationResult.candidateResults
    } yield {
      Seq[String](
        electionResult.id.key,
        contestResult.id.id,
        contestResult.id.contestNameOption.getOrElse(""),
        affiliationResult.id.id,
        affiliationResult.id.registeredName,
        candidateResult.candidate.key.candidateId,
        candidateResult.candidate.key.shortCodeOption.getOrElse(""),
        candidateResult.rankingOption.map(_.toString).getOrElse(""),
        candidateResult.elected.toString,
        candidateResult.candidate.fullName.initialsOption.getOrElse(""),
        candidateResult.candidate.fullName.firstNameOption.getOrElse(""),
        candidateResult.candidate.fullName.lastName,
        candidateResult.candidate.genderOption.map(_.toString.toLowerCase(Locale.ENGLISH)).getOrElse(""),
        candidateResult.candidate.qualifyingAddressOption.map(_.localityName).getOrElse(""),
      )
    }).prepended(header)
  }

  def main(args: Array[String]): Unit = {
    require(args.lengthIs == 2, s"Usage: ConvertElectionResult <input XML file> <output CSV file>")

    val inputFile: File = new File(args(0)).ensuring(_.isFile)
    val outputFile: File = new File(args(1))

    Using.Manager { use =>
      val csvWriter: CSVWriter = use(CSVWriter.open(outputFile))

      val doc: SaxonDocument = SaxonDocument(saxonProcessor.newDocumentBuilder().build(inputFile))

      val electionResult: ElectionResult = parseElectionResult(doc.documentElement)

      val csvRows: Seq[Seq[String]] = convertElectionResultToCsvWithHeader(electionResult)

      csvWriter.writeAll(csvRows)
    }.get
  }
}
