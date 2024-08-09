package com.burdinov.table

import com.burdinov.table.Config.{parseArgsOrExit, parseColumnStr}

import scala.io.Source
import scala.io.StdIn.readLine
import scala.collection.immutable.VectorBuilder

type Offset = Int

extension (s: String)
  def trimChars(c: Char): String = 
    var startCursor = 0
    while (startCursor < s.length && s.charAt(startCursor) == c) startCursor += 1
    var endCursor = s.length - 1
    while (endCursor > startCursor && s.charAt(endCursor) == c) endCursor -= 1
    s.substring(startCursor, endCursor + 1)

case class Column(name: String, offset: Offset)

class Columns(cols: Vector[Column], delimiter: Char):
  private val byName: Map[String, Int] = cols.map(_.name).zipWithIndex.toMap

  private def indexOf(indexOrName: Int | String): Int = indexOrName match
    case idx: Int => idx
    case name: String => byName(name)

  def get(inputRow: String, indexOrName: Int | String): String =
    val columnIndex = indexOf(indexOrName)

    (if (columnIndex == cols.length - 1)
      inputRow.substring(cols(columnIndex).offset)
    else
      inputRow.substring(cols(columnIndex).offset, cols(columnIndex + 1).offset)
      ).trimChars(delimiter)

  def allExcept(inputRow: String, excluded: Set[Int | String]): Vector[String] =
    val excludedIndexes = excluded.map(indexOf)
    for {
      i <- cols.indices.toVector
      if !excludedIndexes.contains(i)
    } yield get(inputRow, i)

  def requireContainsNecessaryCols(isContained: Vector[Int | String]): Unit =
    isContained.foreach {
      case c @ (i: Int) => if (0 > i || i >= cols.length) {
        println(s"invalid column index $c. must be in the range [0 - ${cols.length}]")
        sys.exit(1)
      }
      case c @ (s: String) => if (!byName.contains(s)) {
        println(s"$c is invalid column name. possible names: ${byName.keys.mkString("[", ", ", "]")}")
        sys.exit(1)
      }
}

object Columns:
  def apply(input: String, delimiter: Char = ' ', delimiterRepeats: Int = 2): Columns =
    val result = VectorBuilder[Column]()
    var cursor = 0
    var columnStartOffset = 0
    var delimiterEncountered = 0
    while (cursor < input.length) 
      val ch = input.charAt(cursor)
      if ch == delimiter then 
        delimiterEncountered += 1
      else if delimiterEncountered >= delimiterRepeats then
        val column = Column(input.substring(columnStartOffset, cursor).trimChars(delimiter), columnStartOffset)
        result.addOne(column)
        delimiterEncountered = 0
        columnStartOffset = cursor
        
      cursor += 1
    if cursor > columnStartOffset then
      val column = Column(input.substring(columnStartOffset, input.length).trimChars(delimiter), columnStartOffset)
        result.addOne(column)
    
    new Columns(result.result(), delimiter)


object Table:

  def traverseLines(input: Iterator[String], columns: Columns, config: Config): Iterator[String] =
    val lineParser = createLineParser(config, columns)
    for {
      line <- input
      if config.filterColumns.forall((col, predicate) => columns.get(line, col).contains(predicate))
      output <- lineParser(line)
    } yield output

  def createLineParser(config: Config, columns: Columns): String => Vector[String] =
    config.select.get match
      case Select.Regular(selectedColumns) =>
        columns.requireContainsNecessaryCols(selectedColumns)
        line => selectedColumns.map(col => columns.get(line, col))
      case Select.Excluded(excludedColumns) =>
        columns.requireContainsNecessaryCols(excludedColumns)
        line => columns.allExcept(line, excludedColumns.toSet)
      case Select.Project(projectionStr) =>
        val regex = "(\\{\\S+\\})".r
        val selectedCols = regex.findAllIn(projectionStr).toVector

        columns.requireContainsNecessaryCols(selectedCols.map(col => parseColumnStr(col.substring(1, col.length - 1))))

        line => Vector((selectedCols zip selectedCols.map(col => columns.get(line, parseColumnStr(col.substring(1, col.length - 1)))))
        .foldLeft(projectionStr){case (currProjection, (placeholder, value)) => currProjection.replace(placeholder, value)})

  @main def main(args: String*): Unit =
    val config = parseArgsOrExit(args)

    val fileSource = config.file.map(Source.fromFile)

    val input = fileSource
      .map(_.getLines)
      .getOrElse(Iterator.continually(readLine).takeWhile(s => s != null && s.nonEmpty))

    if (!input.hasNext)
      println("ERROR: input empty")
      sys.exit(1)

    val firstLine = input.next()
    val columns = Columns(firstLine)

    val rest = if (config.readHeader) input else Iterator(firstLine) ++ input

    val result = traverseLines(rest, columns, config)
    result.foreach(println)

    fileSource.foreach(_.close)
