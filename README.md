# Table

A command line tool to select columns from a table-shaped input.  Supports filtering and formatting.
Run `table` without arguments to see available options.

Quick example:
```shell
#docker images:
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
* `-h` will parse the first line as the header and allow selecting the columns by name
* `-s` will choose which columns to output

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
You can filter only the rows you are care about with `-f`/`--filter`  
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