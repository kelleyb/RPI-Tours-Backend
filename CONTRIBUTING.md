# Contributing to the RPI Tours Backend

## Submitting an Issue

If what you want to submit is a bug, test multiple times under different
conditions if at all possible. Your issue should contain clear steps to
reproduce, as well as screenshots if applicable.

If it's a feature, give an in-depth explanation of what you want the server to
do, as well as why you think this should be something implemented in the server,
rather than the client.

Each issue should contain what is called a "Definition of Done", referred to as
"DoD". This is a measureable deliverable which essentially defines what it means
to complete an issue. If you're not sure about what the DoD would be, then you
can label it with "dod-unknown". This should be used sparingly, if at all.

## Contributing Code

Try to follow the conventions defined in the style guide, as well as those
displayed throughout the codebase.

We use a functional programming pattern. This means that, for one,  our code 
never actually modifies any state. How is this possible if we're going to be
creating and updating rows in a database? This is actually handled by Slick.
All we do is say what we want to happen, then Slick handles the rest. Whether
it's making a query, inserting/deleting a value, etc.

There is a file in the root of the project named `pre-commit`. This file
essentially tells git to run our tests before it will allow you to actually
commit your changes. This takes a few seconds, but in my opinion it is well
worth it to make sure we never commit broken code.

To enable the pre-commit hook, my recommendation is to run the command 
`ln -s ../../pre-commit ./pre-commit` from the directory `.git/hooks/`. If 
you're not aware, this makes a link to the pre-commit file in the root directory
from a new file in the `.git/hooks` directory. So if the pre-commit file changes
in the future, it should automatically be updated in your hooks directory. Make
sure the pre-commit file is executable.

## Style Guide

We somewhat loosely follow the 
[official Scala Style Guide](http://docs.scala-lang.org/style/), once ScalaStyle
gets integrated into this project you can use that as a reference.

Basic rules:

* 2 spaces for indentation
* If you need to go to another line, indent another 2 spaces more than the first
line
* If you need to call/declare a method over multiple lines, put each arg on its
own line
* PascalCase for class/object names, camelCase for variable names (incl. method
args)
* 80 characters per line wherever possible. If necessary, it can be expanded to
a maximum of 100 characters.
* No semicolons

If ScalaStyle is implemented, then if you get a warning on compilation, fix it.
We will not accept any pull requests which cause a warning/error.
