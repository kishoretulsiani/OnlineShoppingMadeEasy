# Online Shopping Made Easy

The application helps to quickly build up the online presence for a Fruit shop / Any business. Current focus of the implementation is on below areas. but it will be enchanced in future to accomodate other needs based on business requirements.

1. Orders Service
2. Simple offers
3. Store and retrieve orders

# Swagger for the project is available here

[OnlineShoppingMadeEasy](https://kishoretulsiani.github.io/OnlineShoppingMadeEasy/swagger/index.html)

## Building the project

## Running the project

## TODO List

* Couchbase can be setup after the docker is up for the very first time.
* [Referance 1](https://github.com/madhur/couchbase-docker)
* [Referance 2](https://github.com/arun-gupta/docker-images/blob/master/couchbase/configure-node.sh)
*
* Mongo DB setup on startup

```
db.createUser({
    user: "admin",
    pwd: "admin",
    roles: [
        {role: "readWrite", db: "fruits_shop"}
    ]
})

```

