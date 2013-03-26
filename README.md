# EnerNOC OpenADR 2.0 VTN

This is the section developement version of EnerNOC's open source VTN.  It covers similar
functionality to the original VTN except the underlying web framework was migrated from Play 2
to Grails.  Grails is a much more mature web framework while Play 2 had some missing features
that caused the app to require work-arounds for common features such as services and 
dependency injection.

## Development

The app uses Apache Maven for building and is the easiest way to run the server.

    export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256"

For more info, see: http://grails.org/doc/latest/guide/commandLine.html#4.5%20Ant%20and%20Maven 

Install the oadr2-ven code found here: http://github.com/enernoc/oadr2-ven

    ~/oadr2-ven $ mvn install -Dmaven.test.skip=true

Then 
