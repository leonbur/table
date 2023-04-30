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
                  file: Option[File] = None)

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
      head("table", "1.0"),
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
