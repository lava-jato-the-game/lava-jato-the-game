
# Developer guide

## Dev setup

### For clojure users

- Check if `clojure` and `npm` are installed

```bash
lava-jato-the-game$ npm --version
6.12.0
lava-jato-the-game$ clj -Sdescribe
{:version "1.10.1.447"
 :config-files ["/usr/share/clojure/deps.edn" "/home/me/.clojure/deps.edn" "deps.edn" ]
 :install-dir "/usr/share/clojure"
 :config-dir "/home/me/.clojure"
 :cache-dir ".cpcache"
 :force false
 :repro false
 :resolve-aliases ""
 :classpath-aliases ""
 :jvm-aliases ""
 :main-aliases ""
 :all-aliases ""}
```

- Install `npm` deps

```bash
lava-jato-the-game$ npm install
npm WARN lava-jato-the-game No repository field.
npm WARN lava-jato-the-game No license field.

audited 688 packages in 1.768s
found 0 vulnerabilities 
```

- Start a REPL with the `dev` profile (you can do this inside your editor/IDE or in any terminal)

```bash
lava-jato-the-game$ clj -A:dev
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Clojure 1.10.1
user=> 
```

- Inside the REPL, call `(user/-main)`. It may take some minutes

```
user=> (user/-main)
Oct 30, 2019 11:08:46 AM org.xnio.Xnio <clinit>
INFO: XNIO version 3.7.3.Final
Oct 30, 2019 11:08:46 AM org.xnio.nio.NioXnio <clinit>
INFO: XNIO NIO Implementation Version 3.7.3.Final
Oct 30, 2019 11:08:46 AM org.jboss.threads.Version <clinit>
INFO: JBoss Threads version 2.3.2.Final
shadow-cljs - server version: 2.8.68 running at http://localhost:9630
shadow-cljs - nREPL server started on port 43179
[:client] Configuring build.
[:client] Compiling ...
[:client] Build completed. (268 files, 256 compiled, 0 warnings, 14.01s)
[:workspaces] Configuring build.
[:workspaces] Compiling ...
[:workspaces] Build completed. (569 files, 350 compiled, 0 warnings, 14.17s)
{:io.pedestal.http/port 8080 ...}
user=> 
```

### For npm users

- Check if `npm` and `java` are installed

```bash
lava-jato-the-game$ npm --version
6.12.0
lava-jato-the-game$ java -version
openjdk version "1.8.0_232"
OpenJDK Runtime Environment (build 1.8.0_232-b09)
OpenJDK 64-Bit Server VM (build 25.232-b09, mixed mode)
```

- Install `npm` deps

```bash
lava-jato-the-game$ npm install
npm WARN lava-jato-the-game No repository field.
npm WARN lava-jato-the-game No license field.

audited 688 packages in 1.768s
found 0 vulnerabilities 
```

- Start it

```bash
lava-jato-the-game$ npm start
> @ start /home/me/src/lava-jato-the-game
> shadow-cljs run user/-main

shadow-cljs - config: /home/me/src/lava-jato-the-game/shadow-cljs.edn  cli version: 2.8.67  node: v12.13.0
shadow-cljs - starting via "clojure"
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
NPM dependency "react-grid-layout" has installed version "0.17.1"
"^0.16.6" was required by jar:file:/home/me/.m2/repository/nubank/workspaces/1.0.13/workspaces-1.0.13.jar!/deps.cljs
shadow-cljs - server version: 2.8.68 running at http://localhost:9630
shadow-cljs - nREPL server started on port 42183
[:client] Configuring build.
[:client] Compiling ...
[:client] Build completed. (268 files, 1 compiled, 0 warnings, 5.49s)
[:workspaces] Configuring build.
[:workspaces] Compiling ...
[:workspaces] Build completed. (569 files, 1 compiled, 0 warnings, 3.92s)
```

## Frontend Dev workflow

- You can connect in the "main" app at [8080](http://localhost:8080/#/home)

- There is a devcards/storybook-like tool at [8080/workspaces](http://localhost:8080/workspaces)

- There is a "control pannel" at [9630](http://localhost:9630) where you can find build status, REPL, debugging info and more.


## References

### Docs about frontend:
- [fulcro](http://book.fulcrologic.com/fulcro3)
- [react-fulcro-interop](https://medium.com/@wilkerlucio/using-any-react-ui-kit-with-fulcro-82cce271b9cc)
- [fulcro+css](https://medium.com/@wilkerlucio/a-guide-to-organizing-styles-on-fulcro-apps-b280d2dfee6b) WARN: It's about fulcro2. We are using fulcro3.

### Docs about backend:
- [pathom](https://wilkerlucio.github.io/pathom/v2)
- [pathom-connect](https://medium.com/@wilkerlucio/pathom-connect-higher-level-abstraction-for-writing-parsers-video-44c009e5d531)

### EDN Query language: [EQL](https://edn-query-language.org/)

### Community:

- Checkout [clojurians](http://clojurians.net) community.

## Backend Dev workflow

- You can run the frontend while running/testing the backend

- You may setup some integration from your editor/repl with `clojure.test/deftest`

- Midje is used just for checking, not for test-runner

### Example

After start the repl, as in `Dev setup -> For clojure users`, run:

```clojure
=> (require '[lava-jato-the-game.integration-test])
=> (lava-jato-the-game.integration-test/index-explorer)
```

It will run one test.
To run all tests:

```clojure
=> (clojure.test/run-all-tests) 
```
