# Table

A command line tool to select columns from a table-shaped input, where columns are separated by multiple whitespaces.  Supports filtering and formatting.
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
#docker images:
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
### Limitations
`table` doesn't handle properly two specific cases:
1. Right-justified column headers, for example notice the TIME column:
    ```shell
    ps -ef
    #  UID   PID  PPID   C STIME   TTY           TIME CMD
    #    0     1     0   0  9Mar23 ??       336:12.65 /sbin/launchd
    #    0   323     1   0  9Mar23 ??        43:20.14 /usr/libexec/logd
    ```
    This case would not catch the width of the TIME column properly
2. Columns with a single whitespace between them - this is intentional so that `table` could capture multi-word columns. In the same example as before, notice the C and STIME columns - they will be captured as a single column:
    ```shell
    ps -ef
    #  UID   PID  PPID   C STIME   TTY           TIME CMD
    #    0     1     0   0  9Mar23 ??       336:12.65 /sbin/launchd
    #    0   323     1   0  9Mar23 ??        43:20.14 /usr/libexec/logd
   
   
   ps -ef | table -h -s C
   # C is invalid column name. possible names: [, TIME CMD, PPID, PID, C STIME, UID, TTY]
    ```

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
