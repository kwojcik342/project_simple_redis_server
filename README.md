# project_simple_redis_server
Implementation of simple redis server, database storing key-value pairs in memory.

## currently implemented functionalities
### basic redis features
- tcp server accepting multiple concurrent client connections. Although multiple connections are accepted only single thread is sequentially processing actual commands accessing data (https://redis.io/docs/latest/operate/oss_and_stack/management/optimization/latency/#single-threaded-nature-of-redis)
- client's input commands parsing according to redis protocol (https://redis.io/docs/latest/develop/reference/protocol-spec/#resp-protocol-description)
- currently handled commands(https://redis.io/docs/latest/commands/): 
  - ping (ex: *1\r\n$4\r\nPING\r\n)
  - echo (ex: *2\r\n$4\r\nECHO\r\n$3\r\nhey\r\n)
  - get (ex: *2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n)
  - set (This command is able to handle parameter px which sets expire time for the pair. There are two ways keys can expire: passive and active. Passive expiration means that pair is still kept in memory but it won't be returned in get command, active means that an expired pair is deleted from memory. Currently only passive expiration is implemented) (ex: *5\r\n$3\r\nSET\r\n$3\r\nfoo\r\n$3\r\nbar\r\n$2\r\npx\r\n$5\r\n20000\r\n)
  - keys (command returns unexpired keys that match a pattern, it handles only patterns containing * and ?) (ex: *2\r\n$4\r\nKEYS\r\n$2\r\nf*\r\n)
- ability to read config file on startup, config file shoud be in path "redis_data/redis.conf". Folder "redis_data" contains sample config.
- limited ability to read redis dump files in rdb format (based on https://rdb.fnordig.de/file_format.html). It's possible to read key value pairs with value encoded using "String encoding". On startup program will try to read dump file available in path defined in config(default path is "\redis_data\dump.rdb"). There are 2 files included both of them contain 2 pairs (hey:bye, foo:bar), pairs in default file "dump.rdb" don't have any expiration time, in second file "dump_expire.rdb" key "foo" has expiration date of "2024-09-30T11:50:13.261" so it won't be loaded from file because it has already expired.

### replication
- possibility to run the program with input parameter --replicaof "localhost 6379" which will automaticly establish connection with master instance running on specified port (running program without this parameter will launch it as master)
- connection to master is established with handshake process consisting of 3 automatic steps: sending a PING, REPLCONF twice, PSYNC commands
- set command sent to replica is automaticly resent to master