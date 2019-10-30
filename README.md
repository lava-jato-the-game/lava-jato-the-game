
# Developer guide


## Graphql

- there is a graphql schema at `resources/schema.graphql`

## Frontend develop

- Install `clj` from [clojure](https://clojure.org/)

- run `clj -A:dev -m user` and keep it running

- connect a browser at `http://localhost:8080/` and edit files at `src/lava_jato_the_game/client.cljs`

- you can play with the mock graphql server on http://localhost:8888/?query=%7Bprofile%7Bname%20party%7Bname%7D%7D%7D

### About frontend

- Use [clojurescript](https://clojurescript.org) as main language
 
- Use [fulcro](http://book.fulcrologic.com/) to control `state` and `dom`.
