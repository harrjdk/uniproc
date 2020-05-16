# uniproc
Universal Procedure Language

## Usage Goals

Rather than compiling to byte code, uniproc will expand into reasonably readable macros for the target language

## Roadmap

Basic functions:

Implemented:

* Print
* Readline/Input
* Assign
* File IO
* Control Statements

To Do:
* Arithmetic
* Casting
* String formatting

Compile to Target:

To Do:

* Java

## Example

```
PRINT Welcome!
ASSIGN @name INPUT What's your name?
PRINT Hello, @name
```

More examples are in examples/

## Syntax

UniProc is a Basic-like language meant to avoid common roadblocks to 
new developers such as quoting strings and an "open-garden."

### Opinionated Syntax

UniProc forces developers to move logic branches larger than a single line 
into procedures

This is okay:
```
ASSIGN @a 0
ASSIGN @b 1
ASSIGN @c 0
ASSIGN @i 30
PROCEDURE fib
ASSIGN @c @b + @a
PRINT @c
ASSIGN @a @b
ASSIGN @b @c
ASSIGN @i @i - 1
IF @i EXECUTE fib
END
PRINT @a
PRINT @b
EXECUTE fib
```

This is not okay:
```
IF INPUT What's up? PRINT Foo!
```

