# lox

Contains code for both **jlox** and **clox** implementations of the Lox language.
Code is from [Crafting Interpreters](http://craftinginterpreters.com) book with completed
challenges and my updates.

## jlox

Java implementation of Lox (tree-walk interpreter). The code is in [src/main/java/com/github/sadikovi](./src/main/java/com/github/sadikovi).

Run `sbt run` to launch the `jlox` console, pass a path to clox grammar to the command to execute
the program.

```shell
# run the console
sbt run

# run the program
sbt 'run grammar.lox'
```

## clox

C implementation of Lox (bytecode virtual machine). All of the code is in [clox](./clox) folder.

Run `clox/bin/compile` to build the code, this creates a binary in `clox/target` that you can then
run with `clox/target/clox`.

The binary takes one argument which is the path to a file that contains lox code/program.

```shell
# build the code
clox/bin/compile

# run the console
clox/target/clox

# execute the program
clox/target/clox clox.lox
```

All of the flags to toggle GC logging or debug messages are in [clox/common.h](./clox/common.h).
