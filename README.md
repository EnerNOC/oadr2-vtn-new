# EnerNOC OpenADR 2.0 VTN

This is the section developement version of EnerNOC's open source VTN (server) for 
[OpenADR 2.0](http://openadr.org).  

This app supercedes the original VTN, the major difference being the underlying web framework 
was migrated from [Play 2](http://www.playframework.com/) to [Grails](http://grails.org/).
Grails is a much more mature web framework while Play 2 had some missing features.
The original Play app required work-arounds for common features such as services and 
dependency injection, which Grails provides out of the box.

## Configuration

Most application settings are found in `gails-app/conf/Config.groovy`.  Set the `xmppSvc`
settings in order to enable XMPP functionality.  Note that if using OpenFire as the 
XMPP server, `jid` should be just the 'username,' not `username@host.com`


## Development

### Prerequisites

The VTN depends on the oadr2-ven code found here: http://github.com/enernoc/oadr2-ven

Install locally by running 

    ~/oadr2-ven $ mvn install -Dmaven.test.skip=true

### Running Locally

The app can be run from the command line with Apache Maven or the Grails command line tools.

If you have Grails installed: `grails run-app`

If you use Maven:

    export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256"
    maven grails:run-app

For more info, see: http://grails.org/doc/latest/guide/commandLine.html#4.5%20Ant%20and%20Maven 


### Database

Edit `grails-app/conf/DataSrouce.groovy`.  By default when running with `grails run-app`, 
it runs with an in-memory database that is wiped when the app shuts down.  If you want to 
use a simple, file-based database that persists betwen shutdowns, you can remove the
`mem:` from the `url` parameter and change `dbCreate='update'`.


### Testing Locally

You can use `curl` to execute OpenADR requests on the server like so:

    curl -vd @xmpp-http-tests/httpRequest1.xml -H "Content-Type: application/xml" \
       http://localhost:8080/oadr2-vtn-groovy/OpenADR2/Simple/EiEvent
    


### Packaging

Using grails command line: `grails war`

or with Maven: `mvn package`


### Developer References

If you want to extend this project, here are several resources that will be helpful:

* [Grails Reference](http://grails.org/doc/2.2.1/guide/index.html)
* [Groovy API](http://groovy.codehaus.org/api/)
* [Groovy JDK](http://groovy.codehaus.org/groovy-jdk.html) (extensions to Java classes)
* [Spring Framework Reference](http://static.springsource.org/spring/docs/3.1.x/javadoc-api/)
* [Java EE API](http://docs.oracle.com/javaee/6/api/)
* [Java SE API](http://docs.oracle.com/javase/7/docs/api/)


## TODO

* OpenADR Debug page to post example XML from a web form and display the result
* Filter on OpenADR services to handle certificate auth
* Web app auth

## Credits 

Much of the original code is based on a Play 2.0 app written by our 2012 summer intern 
[Jeff Lajoie](http://www.linkedin.com/pub/jeff-lajoie/5b/424/109).  It was then converted
to Grails by one of our 2013 interns, Yang Xiang, with help from 
[Thom Nichols](http://open.enernoc.com/profiles/thom.html).


This project also relies on the following open source frameworks and libraries to help make it awesome:

* [Twitter Bootstrap](http://twitter.github.io/bootstrap/index.html)
* [JQuery](http://jquery.com/) 
* [Font Awesome](http://fortawesome.github.com/Font-Awesome/)

