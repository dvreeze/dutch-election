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

package eu.cdevreeze.nlelection.data

import scala.collection.immutable.SeqMap

/**
 * Election tree, as a tree of regions.
 *
 * @author Chris de Vreeze
 */
final case class ElectionTree private (regionMap: SeqMap[RegionKey, Region]) {
  assert(regionMap.forall { case (key, region) => region.key == key })

  def allRegions: Seq[Region] = regionMap.values.toSeq

  def findRegion(key: RegionKey): Option[Region] = regionMap.get(key)

  def getRegion(key: RegionKey): Region = findRegion(key).getOrElse(sys.error(s"Missing region with key '$key'"))

  def findParentRegion(key: RegionKey): Option[Region] = {
    findRegion(key).flatMap(_.parentRegionKeyOption.flatMap(findRegion))
  }

  def findAllChildRegions(key: RegionKey): Seq[Region] = {
    require(findRegion(key).nonEmpty, s"Missing region with key '$key'")

    regionMap.values.filter(_.parentRegionKeyOption.contains(key)).toSeq
  }

  def findAllDescendantRegions(key: RegionKey): Seq[Region] = {
    findAllChildRegions(key).flatMap(ch => findAllDescendantRegionsOrSelf(ch.key))
  }

  def findAllDescendantRegionsOrSelf(key: RegionKey): Seq[Region] = {
    findAllChildRegions(key)
      .flatMap(ch => findAllDescendantRegionsOrSelf(ch.key))
      .prepended(getRegion(key))
  }
}

object ElectionTree {

  def from(regions: Seq[Region]): ElectionTree = {
    ElectionTree(regions.map(region => region.key -> region).to(SeqMap))
  }
}
