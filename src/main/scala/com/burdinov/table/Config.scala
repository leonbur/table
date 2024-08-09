//> using dep com.github.scopt::scopt::4.1.0

package com.burdinov.table

import scopt.OParser

import java.io.File

enum Select:
  case Regular(selectedColumns: Vector[Int | String])
  case Excluded(excludedColumns: Vector[Int | String])
  case Project(placeholder: String)

case class Config(select: Option[Select] = None,
                  readHeader: Boolean = false,
                  filterColumns: Map[Int | String, String] = Map(),
                  file: Option[File] = None,
                  delimiter: String = " ",
                  delimiterRepeatsAtLeast: Int = 2)

object Config:
  def parseArgsOrExit(args: Seq[String]): Config =
    OParser.parse(parser, args, Config()) match
      case Some(config) => config
      case None => sys.exit(1)

  def parseColumnStr(colStr: String): Int | String =
    colStr.toIntOption.getOrElse(colStr).asInstanceOf[Int | String]

  private val parser =
    val builder = OParser.builder[Config]
    import builder.*

    var multipleSelects = false

    OParser.sequence(
      programName("table"),
      head("table", "1.1"),
      opt[Seq[String]]('s', "select")
        .valueName("0,1,2,...")
        .action((x, c) =>
          if (c.select.isDefined) multipleSelects = true
          c.copy(select = Some(Select.Regular(x.map(parseColumnStr).toVector)))
        )
        .text("select the column indexes (or names if --header) to be outputted"),
      opt[Seq[String]]('S', "exclude-select")
        .valueName("0,1,2,...")
        .action((x, c) =>
          if (c.select.isDefined) multipleSelects = true
          c.copy(select = Some(Select.Excluded(x.map(parseColumnStr).toVector)))
        )
        .text("select the column indexes (or names if --header) to be will be EXCLUDED from the output"),
      opt[String]('p', "project")
        .valueName("\"this column {1} is injected and this column {0} too\"")
        .action((x, c) =>
          if (c.select.isDefined) multipleSelects = true
          c.copy(select = Some(Select.Project(x)))
        )
        .text("replaces the default printing of every selected column in each row with a string interpolated from the selected columns of each row"),
      opt[String]('d', "delimiter")
        .valueName("e.g. \"-\" or \"_\"")
        .action((d, c) => c.copy(delimiter = d))
        .validate(delim =>
          if (delim.nonEmpty) success
          else failure("delimiter cannot be an empty string")
        )
        .text("changes the default delimiter to a different set of characters"),
      opt[Int]('r', "delimiter-repeats")
        .valueName("2 is default. could be a value between 1 and higher")
        .action((d, c) => c.copy(delimiterRepeatsAtLeast = d))
        .validate(repeats =>
          if (repeats > 0) success
          else failure("delimiter repeats must be at least 1")
        )
        .text("sets at least how many times the delimiter needs to appear in a row to separate between columns"),
      opt[Unit]('h', "header")
        .action((_, c) => c.copy(readHeader = true))
        .text("treat first line as header. allows selecting columns by column names extracted from the header instead of by index"),
      opt[Map[String, String]]('f', "filter")
        .valueName("col1=predicate1,col2=predicate2...")
        .action((x, c) => c.copy(filterColumns = x.map((colStr, pred) => parseColumnStr(colStr) -> pred)))
        .text("filter rows that the filtering columns contain the predicate"),
      arg[File]("<file>...")
        .optional()
        .action((x, c) => c.copy(file = Some(x)))
        .text("read from file instead of stdin"),
      checkConfig(c =>
        if (c.select.isEmpty)
          failure("missing selection operation [--select, --exclude-select, --project]")
        else if (multipleSelects)
          failure("only one of --select, --exclude-select, --project options can be chosen")
        else
          success
      )
    )
