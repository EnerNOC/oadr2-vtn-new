# EnerNOC OpenADR 2.0 VTN

EnerNOC's open source VTN (server) for [OpenADR 2.0](http://openadr.org).  You can find more 
documentation on the [project wiki](http://github.com/EnerNOC/oadr2-vtn-new/wiki/).

This app supercedes the [first version of oadr2-vtn](http://github.com/EnerNOC/oadr2-vtn), 
the major difference being the underlying web framework 
was migrated from [Play 2](http://www.playframework.com/) to [Grails](http://grails.org/).
Grails is a much more mature web framework while Play 2 had some missing features.
The original Play app required work-arounds for common features such as services and 
dependency injection, which Grails provides out of the box.

## Configuration

Most application settings are found in `grails-app/conf/Config.groovy`.  Set the `xmppSvc`
settings in order to enable XMPP functionality.  Note that if using OpenFire as the 
XMPP server, `jid` should be just the 'username,' not `username@host.com`

AMQP configuration can be found at the bottom of `Config.groovy` in the `rabbitmq` section.
See the [plugin documentation](http://grails-plugins.github.io/grails-rabbitmq/docs/manual/)
for more details.


## Development

### Prerequisites

The VTN depends on the oadr2-ven code found here: https://github.com/enernoc/oadr2-ven

Clone and install *oadr2-ven* by running 

```bash
git clone https://github.com/EnerNOC/oadr2-ven.git
cd oadr2-ven
mvn install -Dmaven.test.skip=true
```

You also need an AMQP server to handle push operations.  The easiest solution is 
[RabbitMQ](http://www.rabbitmq.com/download.html).  If you're on a Mac with Homeberw or
most Linux distros, you can easily install Rabbit via your package manager.


### Running Locally

The app can be run from the command line with Apache Maven or the Grails command line tools.

If you have Grails installed: `grails run-app`

If you use Maven:

    export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256"
    maven grails:run-app

For more info, see: http://grails.org/doc/latest/guide/commandLine.html#4.5%20Ant%20and%20Maven 


### Running an external server with HTTPS

You can use the embedded server and make it accessible externally like so
 
    grails run-app -https -grails.server.host 192.168.56.102


### Database

Edit `grails-app/conf/DataSrouce.groovy`.  By default when running with `grails run-app`, 
it runs with an in-memory database that is wiped when the app shuts down.  If you want to 
use a simple, file-based database that persists betwen shutdowns, you can remove the
`mem:` from the `url` parameter and change `dbCreate='update'`.


## Testing

### Unit Testing

You can unit test classes by using Grails' built-in testing features:

    grails test-app -echoOut # runs all tests, with console output

    grails test-app -unit Ven # runs only test/unit/VenTests.groovy


### Functional Testing

You can use `curl` to execute OpenADR requests on the server like so:

    curl -vd @xmpp-http-tests/httpRequest1.xml -H "Content-Type: application/xml" \
       http://localhost:8080/oadr2-vtn-groovy/OpenADR2/Simple/EiEvent

Or use the test console located at http://localhost:8080/oadr2-vtn-groovy/OADRTest/index


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

* Filter on OpenADR services to handle certificate auth
* Web app auth

## Credits 

Much of the original code is based on a Play 2.0 app written by our 2012 summer intern 
[Jeff Lajoie](http://www.linkedin.com/pub/jeff-lajoie/5b/424/109).  It was then converted
to Grails by one of our 2013 interns, [Yang Xiang](www.linkedin.com/pub/yang-xiang/6b/36/300), 
with help from [Thom Nichols](http://open.enernoc.com/profiles/thom.html).


This project also relies on the following open source frameworks and libraries to help make it awesome:

* [Twitter Bootstrap](http://twitter.github.io/bootstrap/index.html)
* [JQuery](http://jquery.com/) 
* [Font Awesome](http://fortawesome.github.com/Font-Awesome/)

