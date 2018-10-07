# tempvs-message
[![Circle CI](https://circleci.com/gh/ahlinist/tempvs-message/tree/master.svg?&style=shield)](https://circleci.com/gh/ahlinist/tempvs-message/tree/master)

A message microservice for tempvs (see: https://github.com/ahlinist/tempvs) based on spring boot.
 
## Configuration

The following env variables need to be set:
 * PORT
 * TOKEN (security token that matches the one being set up in the host app)
 * JDBC_DATABASE_URL
 * JDBC_DATABASE_USERNAME
 * JDBC_DATABASE_PASSWORD

## Running installations
### Stage
http://stage.message.tempvs.club
### Prod
http://message.tempvs.club
