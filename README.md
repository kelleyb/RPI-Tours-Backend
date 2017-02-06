# RPI Tours Backend

## About

This project aims to be an efficient, easy to use backend for the 
[RPI tours](https://github.com/JohnBehnke/RPI_Tours_iOS) application. 
It will provide an API to access categories, tours, landmark data,
waypoints, etc.

It is written in [Scala](https://www.scala-lang.org/) using the 
[Play Framework](https://www.playframework.com/), and uses the FRM 
(Functional Relational Mapping) library [Slick](http://slick.lightbend.com/).

Proudly an [RCOS project](https://rcos.io/).

## Goals

These are the goals and milestones set for the Spring 2017 semester in RCOS:

1. Database Schema Design and Implementation
    * Design Database Schema
    * Research ORMs/FRMs available for Scala/Play
    * Create initial models and relationships required for API
2. Back End
    * Create API endpoints to serve tour data to the applications
    * Create endpoints to add points of interest to the database
    * Create endpoints to create and edit tours
    * Add user authentication capability (for use by admissions, etc.)
3. Implement Basic Front End
    * Create views for editing tours manually
    * Add UI features such as a map for inserting waypoints/landmarks easily.
    * Prettify (semi-stretch goal, I'm a back end guy)

## Contributing

See the [CONTRIBUTING](CONTRIBUTING) file for some more information. We loosely
follow the [Official Scala Style Guide](http://docs.scala-lang.org/style/). 

## Running

If you want to try running the site on your computer, you'll need 
[SBT](http://www.scala-sbt.org/) installed. If you're not aware, SBT is the
build tool pretty much everyone uses for Scala development. You might hear
people refer to someting called "Activator", but that's pretty much just a
wrapper around SBT.

Once you have SBT installed, open a terminal and navigate to the root directory
of this project.

    cd /path/to/rpi-tours/

Then you can just run:

    sbt

And it will open up an interactive prompt. From there I suggest running:

    compile
    run

This will compile the project for you, downloading any dependencies required.

Alternatively, you can run `sbt compile run` in the root directory, and it will
accomplish the same thing as the above 3 commands.

If you do `run` before actually compiling, it will still work, but it will be
noticeably slower than trying to access the precompiled server. This is because
when you make a request to the server, it will notice it hasn't been compiled.
It then compiles for you, which can take several seconds.
