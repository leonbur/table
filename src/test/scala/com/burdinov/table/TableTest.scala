//> using test.dep org.scalameta::munit::1.0.0

package com.burdinov.table

import munit.FunSuite

class TableTest extends FunSuite :
  test("parsing first line columns: extracting names and offsets of sentences") {
    val input = "REPOSITORY          TAG       IMAGE ID      CREATED        SIZE"

    val expected = Vector("REPOSITORY", "TAG", "IMAGE ID", "CREATED", "SIZE")
    val actual = Columns(input)

    val resultByColumnName = expected.map(col => actual.get(input, col))
    val resultByColumnIndex = expected.zipWithIndex.map((_, colIdx) => actual.get(input, colIdx))
    assertEquals(resultByColumnName, expected)
    assertEquals(resultByColumnIndex, expected)
  }

  test("selecting columns: extract a two columns from multiple lines") {
    val input =
      """REPOSITORY          TAG       IMAGE ID       CREATED        SIZE
        |bitnami/kafka       3.2       b7add9628c8e   4 weeks ago    657MB
        |bitnami/zookeeper   3.8       000e247c0e4c   4 weeks ago    477MB
        |openjdk             11.0      23d35e2be72f   7 weeks ago    650MB
        |""".stripMargin

    val lines = input.split('\n')
    val columns = Columns(lines.head)
    val config = Config(select = Some(Select.Regular(Vector(1, 2))))

    val expected = List("3.2", "b7add9628c8e", "3.8", "000e247c0e4c", "11.0", "23d35e2be72f")

    val actual = Table.traverseLines(lines.tail.iterator, columns, config).toList
    assertEquals(actual, expected)
  }

  test("selecting columns: header-less with columns with empty strings") {
    val input =
      """          202         203                             blabla@gmail.com
        |          208         209                             blabla@gmail.com
        |*         42          43                              blabla@gmail.com                some-namespace
        |          84          85                              blabla@gmail.com
        |          96          97                              blabla@gmail.com
        |          ae1         gke_bla-system_asia-east1_ae1   gke_bla-system_asia-east1_ae1   some-namespace
        |          uw2-edt-1   uw2-edt-2                       blabla@gmail.com
        |          uw2-pub-1   uw2-pub-2                       blabla@gmail.com""".stripMargin

    val lines = input.split('\n')
    val columns = Columns(lines.head)
    val config = Config(select = Some(Select.Regular(Vector(1))))

    val expected = List("202",
    "208",
    "42",
    "84",
    "96",
    "ae1",
    "uw2-edt-1",
    "uw2-pub-1")

    val actual = Table.traverseLines(Iterator.from(lines), columns, config).toList
    assertEquals(actual, expected)
  }

  test("selecting all but excluded columns: exclude three columns and remain with two") {
    val input =
      """REPOSITORY          TAG       IMAGE ID       CREATED        SIZE
        |bitnami/kafka       3.2       b7add9628c8e   4 weeks ago    657MB
        |bitnami/zookeeper   3.8       000e247c0e4c   4 weeks ago    477MB
        |openjdk             11.0      23d35e2be72f   7 weeks ago    650MB
        |""".stripMargin

    val lines = input.split('\n')
    val columns = Columns(lines.head)
    val config = Config(select = Some(Select.Excluded(Vector("REPOSITORY", "SIZE", "IMAGE ID"))), readHeader = true)

    val expected = List("3.2", "4 weeks ago", "3.8", "4 weeks ago", "11.0", "7 weeks ago")

    val actual = Table.traverseLines(lines.tail.iterator, columns, config).toList
    assertEquals(actual, expected)
  }

  test("project output: header") {
    val input =
      """REPOSITORY          TAG       IMAGE ID       CREATED        SIZE
        |bitnami/kafka       3.2       b7add9628c8e   4 weeks ago    657MB
        |bitnami/zookeeper   3.8       000e247c0e4c   4 weeks ago    477MB
        |openjdk             11.0      23d35e2be72f   7 weeks ago    650MB
        |""".stripMargin

    val lines = input.split('\n')
    val columns = Columns(lines.head)
    val config = Config(select = Some(Select.Project("the image {REPOSITORY} is of size {SIZE}")), readHeader = true)

    val expected = List(
      "the image bitnami/kafka is of size 657MB",
      "the image bitnami/zookeeper is of size 477MB",
      "the image openjdk is of size 650MB"
    )

    val actual = Table.traverseLines(lines.tail.iterator, columns, config).toList
    assertEquals(actual, expected)
  }

  test("project output: no header") {
    val input =
      """REPOSITORY          TAG       IMAGE ID       CREATED        SIZE
        |bitnami/kafka       3.2       b7add9628c8e   4 weeks ago    657MB
        |bitnami/zookeeper   3.8       000e247c0e4c   4 weeks ago    477MB
        |openjdk             11.0      23d35e2be72f   7 weeks ago    650MB
        |""".stripMargin

    val lines = input.split('\n')
    val columns = Columns(lines.head)
    val config = Config(select = Some(Select.Project("the image {0} is of size {4}")))

    val expected = List(
      "the image bitnami/kafka is of size 657MB",
      "the image bitnami/zookeeper is of size 477MB",
      "the image openjdk is of size 650MB"
    )

    val actual = Table.traverseLines(lines.tail.iterator, columns, config).toList
    assertEquals(actual, expected)
  }

  test("filter columns: filter by one column and select from another column") {
    val input =
      """REPOSITORY          TAG       IMAGE ID       CREATED        SIZE
        |bitnami/kafka       3.2       b7add9628c8e   4 weeks ago    657MB
        |bitnami/zookeeper   3.8       000e247c0e4c   4 weeks ago    477MB
        |openjdk             11.0      23d35e2be72f   7 weeks ago    650MB
        |""".stripMargin

    val lines = input.split('\n')
    val columns = Columns(lines.head)
    val config = Config(filterColumns = Map("REPOSITORY" -> "bitnami"), select = Some(Select.Regular(Vector(2))))

    val expected = List("b7add9628c8e", "000e247c0e4c")

    val actual = Table.traverseLines(lines.tail.iterator, columns, config).toList
    assertEquals(actual, expected)
  }
