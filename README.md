# Table

A command line tool to select columns from a table-shaped input, where columns are separated by multiple whitespaces (or a delimiter of your choice).  Supports filtering and formatting.
Run `table` without any arguments to see available options.

Quick example:

* `-h` will treat the first line as the header and allow selecting the columns by name
* `-s` to choose which columns to output
```shell
docker images
#REPOSITORY          TAG       IMAGE ID       CREATED        SIZE
#bitnami/kafka       3.2       b7add9628c8e   4 weeks ago    657MB
#bitnami/zookeeper   3.8       000e247c0e4c   4 weeks ago    477MB
#openjdk             11.0      23d35e2be72f   7 weeks ago    650MB

docker images | table -h -s "TAG,IMAGE ID"

#output:
#3.2
#b7add9628c8e
#3.8
#000e247c0e4c
#11.0
#23d35e2be72f
```

## Selecting
1. regular select: `-s`/`--select`. select the columns by name (if `--header` flag is on) or by the index of the column.  
2. excluding select: `-S`/`--exclude-select` selects all the columns except the requested.  
3. projection: `-p`/`--project` allows to form an output string with placeholders for the requested columns. for example:  
```shell
docker images | table -h -p "the image {REPOSITORY} is of size {SIZE}"
```   
will output  
```shell
#the image bitnami/kafka is of size 657MB
#the image bitnami/zookeeper is of size 477MB
#the image openjdk is of size 650MB"
```  

## Filtering
You can filter only the rows you care about with `-f`/`--filter`. The filter works by searching if the cell includes the searched substring.  
```shell
docker images:
#REPOSITORY          TAG       IMAGE ID       CREATED        SIZE
#bitnami/kafka       3.2       b7add9628c8e   4 weeks ago    657MB
#bitnami/zookeeper   3.8       000e247c0e4c   4 weeks ago    477MB
#openjdk             11.0      23d35e2be72f   7 weeks ago    650MB

docker images | table -h -s "TAG,IMAGE ID" -f "REPOSITORY=bitnami"

#output:
#3.2
#b7add9628c8e
#3.8
#000e247c0e4c
```

### Custom delimiter
To split the columns by a different delimiter, such as dashes (`-`), you can use `-d/--delimiter` flag to choose a different character than whitespace.
In the following example the whitespaces has been replaced by dashes we declare dash to be the delimiter:
```shell
docker images
#REPOSITORY----------TAG----IMAGE ID-------CREATED-------SIZE
#bitnami/kafka-------3.2----b7add9628c8e---4 weeks ago---657MB
#bitnami/zookeeper---3.8----000e247c0e4c---4 weeks ago---477MB
#openjdk-------------11.0---23d35e2be72f---7 weeks ago---650MB

docker images | table -h -s "TAG,IMAGE ID" -d -

#output:
#3.2
#b7add9628c8e
#3.8
#000e247c0e4c
```
### custom repetitions of the delimiter
To change the amount of times the delimiter has to show up consecutively to split the columns, you can use the `-r/--delimiter-repeats` flag.
The following example shows a table input that is separated by a single tab (`\t`), so we will declare the delimiter to be the tab symbol, and the repetitions to be 1:
```shell
cat example.tsv
#CONST  123456  12.45

cat example.tsv | table -s 1 -d $'\t' -r 1

#output:
#123456
```
__Note on special character escaping:__ to properly pass special characters like tab (`\t`) or new line (`\n`), it needs to be interpreted by the shell like so: `$'\t'`.


### Limitations
`table` doesn't handle properly a specific case:
Right-justified column headers, for example notice the TIME column:
```shell
 ps -ef
 #  UID   PID  PPID   C STIME   TTY           TIME CMD
 #    0     1     0   0  9Mar23 ??       336:12.65 /sbin/launchd
 #    0   323     1   0  9Mar23 ??        43:20.14 /usr/libexec/logd
```
 This case would not catch the width of the TIME column properly

---
## Build
### Setup
While `table` can work with a JVM because it's written in [Scala](http://www.google.com), it's intended to be a fast CLI tool, so it should be compiled to a native binary.
The build requires Clang/LLVM installed for Scala Native compilation. You can use [SDKMAN](https://sdkman.io/) to install JVM-related dependencies

#### requirements:
1. [Scala-CLI](https://scala-cli.virtuslab.org/install) or using SDKMAN: ```sdk install scalacli``` 
2. Clang/LLVM - see Scala Native's [instructions](https://scala-native.org/en/stable/user/setup.html#installing-clang-and-runtime-dependencies)

### Compile and packge an executable
From the root directory
```shell
scala-cli --power package --native .  -S 3.4.1 -o table
```
will create an executable named `table`

### Test
```shell
scala-cli test . --native
```
