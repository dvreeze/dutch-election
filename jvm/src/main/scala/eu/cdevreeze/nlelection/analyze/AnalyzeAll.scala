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

package eu.cdevreeze.nlelection.analyze

import java.io.File
import java.util.Locale

import scala.util.chaining._

import eu.cdevreeze.nlelection.data.CandidateList
import eu.cdevreeze.nlelection.data.Contest
import eu.cdevreeze.nlelection.data.ContestId
import eu.cdevreeze.nlelection.data.ContestVoteCount
import eu.cdevreeze.nlelection.data.ElectionDefinition
import eu.cdevreeze.nlelection.data.ElectionResult
import eu.cdevreeze.nlelection.data.ElectionVoteCount
import eu.cdevreeze.nlelection.data.ReportingUnitId
import eu.cdevreeze.nlelection.parse.CandidateListParser
import eu.cdevreeze.nlelection.parse.ElectionDefinitionParser
import eu.cdevreeze.nlelection.parse.ElectionResultParser
import eu.cdevreeze.nlelection.parse.ElectionVoteCountParser
import eu.cdevreeze.yaidom2.node.saxon.SaxonDocument
import net.sf.saxon.s9api.Processor

/**
 * Analyzes all EML XML files (for elections, election definitions etc.) w.r.t. consistency (vote counts, candidates etc.).
 *
 * There are 4 kinds of EML XML files to analyze: candidate list files, election definition files, vote count files and
 * election result files. Candidate list files have an eml:CandidateList child element of the eml:EML root element.
 * For election definitions this is an eml:ElectionEvent element. Vote count files have an eml:Count element under
 * the root element, and election result files have an eml:Result element under the root element.
 *
 * The 4 kinds of EML files can be recognized by file names containing strings "Kandidatenlijsten", "Verkiezingsdefinitie",
 * "Telling"/"Totaaltelling" and "Resultaat", respectively.
 *
 * @author Chris de Vreeze
 */
object AnalyzeAll {

  private val saxonProcessor: Processor = new Processor(false)

  // scalastyle:off
  def main(args: Array[String]): Unit = {
    require(
      args.lengthIs == 1 || args.lengthIs == 2,
      s"Usage: AnalyzeAll <input XML root directory> [<input file name extension (starting with dot)>]"
    )

    val inputDirectory: File = new File(args(0)).ensuring(_.isDirectory)

    val inputFileNameExtension: String = (if (args.lengthIs > 1) args(1) else ".eml.xml").ensuring(_.startsWith("."))

    val inputXmlFiles: Seq[File] = findXmlFiles(inputDirectory, _.getName.endsWith(inputFileNameExtension))

    println()
    println(s"Found ${inputXmlFiles.size} input files")

    val docBuilder = saxonProcessor.newDocumentBuilder()

    val electionDefinitions: Seq[ElectionDefinition] = inputXmlFiles
      .filter(_.getName.contains("Verkiezingsdefinitie"))
      .tap(_ => println())
      .tap(files => println(s"Parsing ${files.size} election definitions ..."))
      .map { f =>
        val doc = SaxonDocument(docBuilder.build(f))
        ElectionDefinitionParser.parse(doc.documentElement) // Document can now be garbage-collected
      }

    require(
      electionDefinitions.sizeIs == 1,
      s"Expected precisely 1 election definition file, but got ${electionDefinitions.size} ones instead")

    val electionDefinition: ElectionDefinition = electionDefinitions.head

    println()
    println(s"Registered parties (${electionDefinition.registeredParties.size}): ${electionDefinition.registeredParties.mkString(", ")}")

    val candidateLists: Seq[CandidateList] = inputXmlFiles
      .filter(_.getName.contains("Kandidatenlijsten"))
      .sortBy(_.getName)
      .tap(_ => println())
      .tap(files => println(s"Parsing ${files.size} candidate lists ..."))
      .map { f =>
        val doc = SaxonDocument(docBuilder.build(f))
        CandidateListParser.parse(doc.documentElement) // Document can now be garbage-collected
      }

    require(candidateLists.nonEmpty, s"Expected at least one candidate list, but got none")
    candidateLists.foreach { cl =>
      require(
        cl.elections.flatMap(_.contests).sizeIs == 1,
        s"Expected precisely 1 contest per candidate list, but got ${cl.elections.flatMap(_.contests).size} ones (for one candidate list)"
      )

      println()
      val contest: Contest = cl.elections.flatMap(_.contests).head
      println(s"Contest (id ${contest.id.id}): ${contest.id.contestNameOption.getOrElse("")}")
      println(s"Affiliations (${contest.affiliations.size}): ${contest.affiliations.map(_.id.registeredName).mkString(", ")}")
    }

    val candidateListMap: Map[ContestId, CandidateList] = candidateLists
      .groupBy(_.elections.flatMap(_.contests).head.id)
      .ensuring(_.forall(_._2.sizeIs == 1), s"Expected precisely one candidate list per contest ID, but this is not the case")
      .view
      .mapValues(_.head)
      .toMap

    val electionVoteCounts: Seq[ElectionVoteCount] = inputXmlFiles
      .filter(_.getName.toLowerCase(Locale.ENGLISH).contains("telling"))
      .tap(_ => println())
      .tap(files => println(s"Parsing ${files.size} election vote counts ..."))
      .map { f =>
        val doc = SaxonDocument(docBuilder.build(f))
        ElectionVoteCountParser.parse(doc.documentElement) // Document can now be garbage-collected
      }

    require(electionVoteCounts.nonEmpty, s"Expected at least one election vote count, but got none")
    electionVoteCounts.foreach { vc =>
      require(
        vc.contests.sizeIs == 1,
        s"Expected precisely 1 contest per election vote count, but got ${vc.contests.size} ones (for one election vote count)"
      )

      println()
      val contestVoteCount: ContestVoteCount = vc.contests.head
      val regions: Seq[ReportingUnitId] = contestVoteCount.reportingUnitVotesMap.keySet.toSeq.sortBy(_.description)
      println(s"Contest (id ${contestVoteCount.contestId.id}): ${contestVoteCount.contestId.contestNameOption.getOrElse("")}")
      regions.foreach(r => println(s"\tRegion (${r.id}): ${r.description}"))

      if (contestVoteCount.contestId.id != "alle") {
        val candidateList: CandidateList =
          candidateListMap.getOrElse(contestVoteCount.contestId, sys.error(s"Unknown contest ID ${contestVoteCount.contestId}"))

        // TODO Far more useful checks, like candidate counts per affiliation

        if (contestVoteCount.reportingUnitVotesMap.values
              .exists(_.selectionsByAffiliationId.keySet != candidateList.elections.flatMap(_.contests).head.affiliations.map(_.id).toSet)) {

          println(s"Potential error! Mismatch between affiliation IDs of vote counts and of candidate list.")
        }
      }
    }

    val electionResults: Seq[ElectionResult] = inputXmlFiles
      .filter(_.getName.contains("Resultaat"))
      .tap(_ => println())
      .tap(files => println(s"Parsing ${files.size} election results ..."))
      .map { f =>
        val doc = SaxonDocument(docBuilder.build(f))
        ElectionResultParser.parse(doc.documentElement) // Document can now be garbage-collected
      }

    require(electionResults.sizeIs == 1, s"Expected precisely 1 election result file, but got ${electionResults.size} ones instead")

    println()
    println(
      s"Number of parsed documents: ${electionDefinitions.size + candidateLists.size + electionVoteCounts.size + electionResults.size}")
  }

  def findXmlFiles(dir: File, isXml: File => Boolean): Seq[File] = {
    require(dir.isDirectory, s"Not a directory: '$dir'")

    dir.listFiles().toSeq.flatMap { f =>
      if (f.isFile) {
        Seq(f).filter(isXml)
      } else {
        // Recursive call
        findXmlFiles(f, isXml)
      }
    }
  }
}
