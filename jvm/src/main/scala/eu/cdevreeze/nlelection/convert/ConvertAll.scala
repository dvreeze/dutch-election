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
import java.nio.file.Path

import scala.util.chaining._
import scala.util.Using

import com.github.tototoshi.csv.CSVWriter
import eu.cdevreeze.nlelection.common.ENames
import eu.cdevreeze.nlelection.data.CandidateList
import eu.cdevreeze.nlelection.data.ElectionDefinition
import eu.cdevreeze.nlelection.data.ElectionVoteCount
import eu.cdevreeze.yaidom2.node.saxon.SaxonDocument
import net.sf.saxon.s9api.Processor

/**
 * Converts all EML XML files (for elections, election definitions etc.) to CSV files.
 *
 * @author Chris de Vreeze
 */
object ConvertAll {

  // TODO 'Result' files

  private val saxonProcessor: Processor = new Processor(false)

  // scalastyle:off
  def main(args: Array[String]): Unit = {
    require(
      args.lengthIs == 2 || args.lengthIs == 3,
      s"Usage: ConvertElection <input XML root directory> <output directory> [<input file name extension (starting with dot)>]"
    )

    val inputDirectory: File = new File(args(0)).ensuring(_.isDirectory)
    val outputDirectory: File = new File(args(1))
    outputDirectory.mkdirs()

    val inputFileNameExtension: String = (if (args.lengthIs > 2) args(2) else ".eml.xml").ensuring(_.startsWith("."))

    val inputXmlFiles: Seq[File] = findXmlFiles(inputDirectory, _.getName.endsWith(inputFileNameExtension))

    println()
    println(s"Found ${inputXmlFiles.size} input files")

    println()

    inputXmlFiles.foreach(f => convertFile(f, inputDirectory, outputDirectory, inputFileNameExtension))
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

  def convertFile(inputFile: File, rootInputDirectory: File, rootOutputDirectory: File, inputFileNameExtension: String): Unit = {
    require(inputFile.isFile, s"Not a normal file: '$inputFile'")
    require(rootInputDirectory.isDirectory, s"Not a directory: '$rootInputDirectory'")
    require(rootOutputDirectory.isDirectory, s"Not a directory: '$rootOutputDirectory'")

    val relativePath: Path = rootInputDirectory.toPath.relativize(inputFile.toPath).pipe(p => adaptFileName(p, inputFileNameExtension))
    require(!relativePath.isAbsolute, s"Not a relative path ('$relativePath') w.r.t. '$rootInputDirectory'")

    val outputFile: File = rootOutputDirectory.toPath.resolve(relativePath).toFile

    val saxonDoc: SaxonDocument = SaxonDocument(saxonProcessor.newDocumentBuilder().build(inputFile))
    val elementCount = saxonDoc.documentElement.findAllDescendantElemsOrSelf.size

    println(s"Analyzing file '$inputFile' (size: ${inputFile.length}; element count: $elementCount)")

    if (saxonDoc.documentElement.findDescendantElemOrSelf(_.name == ENames.EmlCountEName).nonEmpty) {
      // scalastyle:off
      println(s"Converting election vote count file '$inputFile' to CSV file '$outputFile' ...")

      convertElectionVoteCountFile(saxonDoc, outputFile)
    } else if (saxonDoc.documentElement.findDescendantElemOrSelf(_.name == ENames.EmlElectionEventEName).nonEmpty) {
      // scalastyle:off
      println(s"Converting election definition file '$inputFile' to CSV file '$outputFile' ...")

      convertElectionDefinitionFile(saxonDoc, outputFile)
    } else if (saxonDoc.documentElement.findDescendantElemOrSelf(_.name == ENames.EmlCandidateListEName).nonEmpty) {
      // scalastyle:off
      println(s"Converting candidate list file '$inputFile' to CSV file '$outputFile' ...")

      convertCandidateListFile(saxonDoc, outputFile)
    } else {
      // scalastyle:off
      println(s"Currently unsupported file '$inputFile'")
    }
  }

  def convertElectionVoteCountFile(inputDoc: SaxonDocument, outputFile: File): Unit = {
    val election: ElectionVoteCount = ConvertElectionVoteCount.parseNestedElectionVoteCount(inputDoc.documentElement)

    val csvRows: Seq[Seq[String]] = ConvertElectionVoteCount.convertElectionVoteCountToCsvWithHeader(election)

    require(
      election.contests.forall(_.isConsistentRegardingValidVotes),
      s"Inconsistencies found in totals of valid vote counts (file: ${inputDoc.docUriOption.get})"
    )

    outputFile.getParentFile.mkdirs()

    Using.Manager { use =>
      val csvWriter: CSVWriter = use(CSVWriter.open(outputFile))

      csvWriter.writeAll(csvRows)
    }.get
  }

  def convertElectionDefinitionFile(inputDoc: SaxonDocument, outputFile: File): Unit = {
    val electionDef: ElectionDefinition = ConvertElectionDefinition.parseElectionDefinition(inputDoc.documentElement)

    val csvRows: Seq[Seq[String]] = ConvertElectionDefinition.convertElectionTreeToCsvWithHeader(electionDef.electionTree)

    Using.Manager { use =>
      val csvWriter: CSVWriter = use(CSVWriter.open(outputFile))

      csvWriter.writeAll(csvRows)
    }.get
  }

  def convertCandidateListFile(inputDoc: SaxonDocument, outputFile: File): Unit = {
    val candidateList: CandidateList = ConvertCandidateList.parseNestedCandidateList(inputDoc.documentElement)

    val csvRows: Seq[Seq[String]] = ConvertCandidateList.convertCandidateListToCsvWithHeader(candidateList)

    Using.Manager { use =>
      val csvWriter: CSVWriter = use(CSVWriter.open(outputFile))

      csvWriter.writeAll(csvRows)
    }.get
  }

  private def adaptFileName(path: Path, inputFileNameExtension: String): Path = {
    val originalFileName: String = path.toFile.getName

    if (originalFileName.endsWith(inputFileNameExtension)) {
      val newFileName: String = originalFileName.substring(0, originalFileName.length - inputFileNameExtension.length) + csvFileExtension

      path.subpath(0, path.getNameCount - 1).resolve(newFileName)
    } else {
      path
    }
  }

  private val csvFileExtension = ".csv"
}
