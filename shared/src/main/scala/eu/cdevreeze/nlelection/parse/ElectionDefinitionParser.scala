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
import eu.cdevreeze.nlelection.data.Committee
import eu.cdevreeze.nlelection.data.ElectionDefinition
import eu.cdevreeze.nlelection.data.ElectionId
import eu.cdevreeze.nlelection.data.ElectionTree
import eu.cdevreeze.nlelection.data.Region
import eu.cdevreeze.nlelection.data.RegionKey
import eu.cdevreeze.yaidom2.queryapi.BackingNodes

import scala.util.chaining._

/**
 * Parser of election definitions and their parts in EML XML. Election definitions are recognized in EML XML by an
 * eml:ElectionEvent child element of the eml:EML root element.
 *
 * @author Chris de Vreeze
 */
object ElectionDefinitionParser {

  import ENames._

  def parse(elem: BackingNodes.Elem): ElectionDefinition = {
    require(elem.findDescendantElemOrSelf(_.name == EmlElectionEventEName).nonEmpty, s"Expected $EmlElectionEventEName 'somewhere'")

    ElectionDefinition(
      elem.findDescendantElem(_.name == EmlElectionIdentifierEName).get.pipe(parseElectionIdentifier),
      elem.findDescendantElem(_.name == EmlContestIdentifierEName).get.attr(IdEName),
      elem.findDescendantElem(_.name == KrElectionTreeEName).get.pipe(parseElectionTree),
      elem.filterDescendantElems(_.name == KrRegisteredAppellationEName).map(_.text),
    )
  }

  def parseElectionIdentifier(elem: BackingNodes.Elem): ElectionId = {
    ElectionIdentifierParser.parseElectionIdentifier(elem)
  }

  def parseElectionTree(elem: BackingNodes.Elem): ElectionTree = {
    require(elem.name == KrElectionTreeEName, s"Expected $KrElectionTreeEName but got ${elem.name}")

    val regionElems: Seq[BackingNodes.Elem] = elem.filterChildElems(_.name == KrRegionEName)

    ElectionTree.from(regionElems.map(parseRegion))
  }

  def parseRegion(elem: BackingNodes.Elem): Region = {
    require(elem.name == KrRegionEName, s"Expected $KrRegionEName but got ${elem.name}")

    val committeeElem: BackingNodes.Elem = elem.findChildElem(_.name == KrCommitteeEName).get

    Region(
      RegionKey(elem.attr(RegionCategoryEName), elem.attr(RegionNumberEName).toLong),
      elem.findChildElem(_.name == KrRegionNameEName).get.text,
      Committee(committeeElem.attr(CommitteeCategoryEName), committeeElem.attrOption(CommitteeNameEName)),
      elem.attrOption(SuperiorRegionNumberEName).map(nr => RegionKey(elem.attr(SuperiorRegionCategoryEName), nr.toLong)),
    )
  }
}
