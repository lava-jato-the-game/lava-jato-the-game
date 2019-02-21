
# Developer guide


## Graphql

- there is a graphql schema at `resources/schema.graphql`

## Frontend develop

- Install `clj` foi [clojure](https://clojure.org/)

- run `clj -A:dev -m user` and keep it running

- connect a browser at `http://localhost:8080/` and edit files at `src/lava_jato_the_game/client.cljs`

### About frontend

- Use [clojurescript](https://clojurescript.org) as main language
 
- Use [fulcro](http://book.fulcrologic.com/) to control `state` and `dom`.

- Use [pathom](https://wilkerlucio.github.io/pathom) as a "network driver" to turn
 [EQL](https://github.com/edn-query-language/eql)'s from fulcro into graphql queries/mutations.
 
- There is in `ùser.clj` a [lacinia](http://lacinia.readthedocs.io/) server that runs on
port `8888` and provide a fake-data-mock GraphQL API auto-generated from schema.

- Is use [shadow-cljs](http://shadow-cljs.github.io/) build and serve JS at port `8080`