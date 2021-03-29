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

package eu.cdevreeze.nlelection.tools

import java.io.File
import java.net.URI

import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.node.saxon.SaxonDocument
import eu.cdevreeze.yaidom2.node.saxon.SaxonNodes
import net.sf.saxon.s9api.Processor
import scala.collection.parallel.CollectionConverters._
import scala.collection.parallel.ForkJoinTaskSupport
import scala.util.chaining._

/**
 * Tool to show the element tree structure of one of more XML documents in terms of "paths" of (reversed) ancestor-or-self ENames.
 * Duplicate paths are removed, in order to make the output as concise as possible, with the disadvantage that element counts
 * are not shown.
 *
 * @author Chris de Vreeze
 */
object ExploreXmlStructure {

  /**
   * Path from root to "this element", containing only ENames and no "indexes".
   */
  final case class ElemPath(nesting: Seq[EName]) extends AnyVal {

    def appended(ename: EName): ElemPath = ElemPath(nesting.appended(ename))

    def prepended(ename: EName): ElemPath = ElemPath(nesting.prepended(ename))
  }

  private val saxonProcessor: Processor = new Processor(false)

  def findXmlStructure(doc: SaxonDocument): Seq[ElemPath] = {
    findXmlStructure(doc.documentElement, ElemPath(Seq(doc.documentElement.name)))
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

  private def findXmlStructure(elem: SaxonNodes.Elem, ownPath: ElemPath): Seq[ElemPath] = {
    assert(ownPath.nesting.last == elem.name)

    val childElems = elem.findAllChildElems

    // Recursive calls
    childElems
      .flatMap(che => findXmlStructure(che, ownPath.appended(che.name)))
      .prepended(ownPath)
      .distinct
  }

  def main(args: Array[String]): Unit = {
    require(args.lengthIs == 1, s"Usage: ExploreXmlStructure <file or directory>")

    val fileOrDir: File = new File(args(0))

    val files: Seq[File] = if (fileOrDir.isFile) Seq(fileOrDir) else findXmlFiles(fileOrDir, _.getName.endsWith(".xml"))
    println()
    println(s"Found ${files.size} XML documents to process.")

    val forkJoinPool = new java.util.concurrent.ForkJoinPool(2)

    println()
    val xmlStructures: Map[URI, Seq[ElemPath]] = files.par
      .tap(_.tasksupport = new ForkJoinTaskSupport(forkJoinPool))
      .map { file =>
        // The potentially large SaxonDocument can be garbage-collected almost immediately
        val doc: SaxonDocument = SaxonDocument(saxonProcessor.newDocumentBuilder().build(file))

        val elemCount = doc.documentElement.findAllDescendantElemsOrSelf.size
        println(s"Computing XML structure for document ${file.getName} (file size: ${file.length}, number of elements: $elemCount) ...")

        val xmlStructure = findXmlStructure(doc)
        doc.docUriOption.get -> xmlStructure
      }
      .seq
      .toMap

    xmlStructures.toSeq.sortBy(_._1.toString).foreach {
      case (uri, elemPaths) =>
        println()
        println(s"Document '$uri' has structure (without duplicates):")
        println()
        elemPaths.foreach { path =>
          println(path.nesting.mkString(", "))
        }
    }
  }
}
