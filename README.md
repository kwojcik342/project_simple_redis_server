# project_simple_redis_server
Implementation of simple redis server.

### currently implemented functionality
- creates server continiously accepting tcp connections from multiple clients
- server parses input commands according to redis protocol (https://redis.io/docs/latest/develop/reference/protocol-spec/#resp-protocol-description)
- currently handled commands: ping, echo
