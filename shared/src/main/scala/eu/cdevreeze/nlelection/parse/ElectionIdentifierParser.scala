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

import java.time.LocalDate

import eu.cdevreeze.nlelection.common.ENames
import eu.cdevreeze.nlelection.data.ElectionId
import eu.cdevreeze.yaidom2.queryapi.BackingNodes

/**
 * Parser of election identifiers (eml:ElectionIdentifier elements) in EML XML.
 *
 * @author Chris de Vreeze
 */
object ElectionIdentifierParser {

  import ENames._

  /**
   * Parses an eml:ElectionIdentifier XML element into an ElectionId.
   */
  def parseElectionIdentifier(elem: BackingNodes.Elem): ElectionId = {
    require(elem.name == EmlElectionIdentifierEName, s"Expected $EmlElectionIdentifierEName but got ${elem.name}")

    ElectionId(
      elem.attr(IdEName),
      elem.findChildElem(_.name == EmlElectionNameEName).get.text,
      elem.findChildElem(_.name == EmlElectionCategoryEName).get.text,
      elem.findChildElem(_.name == KrElectionSubcategoryEName).get.text,
      elem.findChildElem(_.name == KrElectionDateEName).map(_.text).map(LocalDate.parse).get,
      elem.findChildElem(_.name == KrNominationDateEName).map(_.text).map(LocalDate.parse),
    )
  }
}
