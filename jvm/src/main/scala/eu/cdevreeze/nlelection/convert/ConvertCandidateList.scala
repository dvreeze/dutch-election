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
import eu.cdevreeze.nlelection.data.CandidateList
import eu.cdevreeze.nlelection.parse.CandidateListParser
import eu.cdevreeze.yaidom2.node.saxon.SaxonDocument
import eu.cdevreeze.yaidom2.node.saxon.SaxonNodes
import net.sf.saxon.s9api.Processor

/**
 * Converter of candidate lists in EML XML to CSV.
 *
 * These input XML files have an eml:CandidateList child element of the eml:EML root element.
 *
 * @author Chris de Vreeze
 */
object ConvertCandidateList {

  private val saxonProcessor: Processor = new Processor(false)

  def parseCandidateList(elem: SaxonNodes.Elem): CandidateList = {
    CandidateListParser.parse(elem)
  }

  def convertCandidateListToCsvWithHeader(candidateList: CandidateList): Seq[Seq[String]] = {
    val header =
      Seq(
        "ElectionKey",
        "ContestId",
        "ContestName",
        "AffiliationId",
        "AffiliationName",
        "Candidate",
        "Candidate shortcode",
        "Initials",
        "FirstName",
        "NamePrefix",
        "LastName",
        "Gender",
        "Address",
      )

    (for {
      election <- candidateList.elections
      contest <- election.contests
      affiliation <- contest.affiliations
      candidate <- affiliation.candidates
    } yield {
      Seq[String](
        election.id.key,
        contest.id.id,
        contest.id.contestNameOption.getOrElse(""),
        affiliation.id.id,
        affiliation.id.registeredName,
        candidate.key.candidateId,
        candidate.key.shortCodeOption.getOrElse(""),
        candidate.fullName.initialsOption.getOrElse(""),
        candidate.fullName.firstNameOption.getOrElse(""),
        candidate.fullName.namePrefixOption.getOrElse(""),
        candidate.fullName.lastName,
        candidate.genderOption.map(_.toString.toLowerCase(Locale.ENGLISH)).getOrElse(""),
        candidate.qualifyingAddressOption.map(_.localityName).getOrElse(""),
      )
    }).prepended(header)
  }

  def main(args: Array[String]): Unit = {
    require(args.lengthIs == 2, s"Usage: ConvertCandidateList <input XML file> <output CSV file>")

    val inputFile: File = new File(args(0)).ensuring(_.isFile)
    val outputFile: File = new File(args(1))

    Using.Manager { use =>
      val csvWriter: CSVWriter = use(CSVWriter.open(outputFile))

      val doc: SaxonDocument = SaxonDocument(saxonProcessor.newDocumentBuilder().build(inputFile))

      val candidateList: CandidateList = parseCandidateList(doc.documentElement)

      val csvRows: Seq[Seq[String]] = convertCandidateListToCsvWithHeader(candidateList)

      csvWriter.writeAll(csvRows)
    }.get
  }
}
