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
import eu.cdevreeze.nlelection.data.ElectionDefinition
import eu.cdevreeze.nlelection.data.ElectionTree
import eu.cdevreeze.nlelection.parse.ElectionDefinitionParser
import eu.cdevreeze.yaidom2.node.saxon.SaxonDocument
import eu.cdevreeze.yaidom2.node.saxon.SaxonNodes
import net.sf.saxon.s9api.Processor

/**
 * Converter of election definitions in EML XML to CSV.
 *
 * These input XML files have an eml:ElectionEvent child element of the eml:EML root element.
 *
 * @author Chris de Vreeze
 */
object ConvertElectionDefinition {

  private val saxonProcessor: Processor = new Processor(false)

  def parseElectionDefinition(elem: SaxonNodes.Elem): ElectionDefinition = {
    ElectionDefinitionParser.parse(elem)
  }

  // TODO Other parts of election definition

  def convertElectionTreeToCsvWithHeader(electionTree: ElectionTree): Seq[Seq[String]] = {
    val header = Seq(
      "RegionCategory",
      "RegionNumber",
      "RegionName",
      "CommitteeCategory",
      "CommitteeName",
      "SuperiorRegionCategory",
      "SuperiorRegionNumber")

    electionTree.allRegions
      .map { region =>
        Seq[String](
          region.key.regionCategory,
          region.key.regionNumber.toString,
          region.regionName,
          region.committee.category,
          region.committee.nameOption.getOrElse(""),
          region.parentRegionKeyOption.map(_.regionCategory).getOrElse(""),
          region.parentRegionKeyOption.map(_.regionNumber.toString).getOrElse("")
        )
      }
      .prepended(header)
  }

  def main(args: Array[String]): Unit = {
    require(args.lengthIs == 2, s"Usage: ConvertElectionDefinition <input XML file> <output CSV file>")

    val inputFile: File = new File(args(0)).ensuring(_.isFile)
    val outputFile: File = new File(args(1))

    Using.Manager { use =>
      val csvWriter: CSVWriter = use(CSVWriter.open(outputFile))

      val doc: SaxonDocument = SaxonDocument(saxonProcessor.newDocumentBuilder().build(inputFile))

      val electionDefinition: ElectionDefinition = parseElectionDefinition(doc.documentElement)

      val csvRows: Seq[Seq[String]] = convertElectionTreeToCsvWithHeader(electionDefinition.electionTree)

      csvWriter.writeAll(csvRows)
    }.get
  }
}
