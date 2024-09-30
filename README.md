# project_simple_redis_server
Implementation of simple redis server, database storing key-value pairs in memory.

### currently implemented functionality
- tcp server accepting multiple concurrent client connections 
- client's input commands parsing according to redis protocol (https://redis.io/docs/latest/develop/reference/protocol-spec/#resp-protocol-description)
- currently handled commands(https://redis.io/docs/latest/commands/): 
  - ping
  - echo
  - get
  - set (This command is able to handle parameter px which sets expire time for the pair. There are two ways keys can expire: passive and active. Passive expiration means that pair is still kept in memory but it won't be returned in get command, active means that an expired pair is deleted from memory. Currently only passive expiration is implemented)