M-Jvm
=====

The jvm backend for [the M programming language](https://m-language.github.io/).

Note that m-jvm compiles only a subset of M, as it is only meant to 
bootstrap [the main M compiler](https://github.com/m-language/m-compiler).

Usage
-----

M-Jvm uses Gradle as its build system, and is built with the `fatJar`
task.

    git clone https://github.com/m-language/m-jvm.git
    cd m-jvm
    gradle fatJar

This will create a jar file in `./build/libs` with all dependencies
included.

The M-Jvm CLI takes an input file and output directory as arguments.
