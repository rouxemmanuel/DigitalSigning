# DigitalSigning 

This is a mirror of the project [(DigitalSigning )](https://github.com/rouxemmanuel/DigitalSigning/) , i just add the
same projects to work with the Alfresco Maven SDK and make compatibile with the most recent standard structure of Alfresco 5.

##### NOTE: Is not ready yet

#### TODO:
 - Resolve some problem on the aspect , the apsect key can't be add to a content
 - [SOLVED]Resolve some standard structure problem
 - Try to understand how use the replace parameter on the config tag for implement with other amp
 
 - Make it work like the olde version
 
###### [Reminder compatibility the version of sdk with alfresco version](http://docs.alfresco.com/5.1/concepts/alfresco-sdk-compatibility.html)
###### [New compatibility with the SDK 3.0.0](http://ecmarchitect.com/archives/2017/04/24/4235)

### Installation

```sh
mvn clean install <MAVEN_PROJECT>
```

for generate the amp packages, after that use the alfresco offical tool for the installation on the alfresco.war and share.war

### License 

All right of these code belong to rouxemmanuel.


## The structure of the project based on the official documentation [SDK Project Structure](http://docs.alfresco.com/5.2/concepts/sdk-projects-aio.html) for the new SDK 3.0.0

├── my-all-in-one-project-platform-jar
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── assembly
│       │   │   ├── amp.xml
│       │   │   ├── file-mapping.properties
│       │   │   └── web
│       │   │       └── README.md
│       │   ├── java
│       │   │   └── com
│       │   │       └── example
│       │   │           └── platformsample
│       │   │               ├── DemoComponent.java
│       │   │               ├── Demo.java
│       │   │               └── HelloWorldWebScript.java
│       │   └── resources
│       │       ├── alfresco
│       │       │   ├── extension
│       │       │   │   └── templates
│       │       │   │       └── webscripts
│       │       │   │           └── alfresco
│       │       │   │               └── tutorials
│       │       │   │                   ├── helloworld.get.desc.xml
│       │       │   │                   ├── helloworld.get.html.ftl
│       │       │   │                   └── helloworld.get.js
│       │       │   └── module
│       │       │       └── my-all-in-one-project-platform-jar
│       │       │           ├── alfresco-global.properties
│       │       │           ├── context
│       │       │           │   ├── bootstrap-context.xml
│       │       │           │   ├── service-context.xml
│       │       │           │   └── webscript-context.xml
│       │       │           ├── messages
│       │       │           │   └── content-model.properties
│       │       │           ├── model
│       │       │           │   ├── content-model.xml
│       │       │           │   └── workflow-model.xml
│       │       │           ├── module-context.xml
│       │       │           ├── module.properties
│       │       │           └── workflow
│       │       │               └── sample-process.bpmn20.xml
│       │       └── META-INF
│       │           └── resources
│       │               └── test.html
│       └── test
│           └── java
│               └── com
│                   └── example
│                       └── platformsample
│                           └── HelloWorldWebScriptControllerTest.java
├── my-all-in-one-project-share-jar
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── assembly
│       │   │   ├── amp.xml
│       │   │   ├── file-mapping.properties
│       │   │   └── web
│       │   │       └── README.md
│       │   ├── java
│       │   │   └── com
│       │   │       └── example
│       │   └── resources
│       │       ├── alfresco
│       │       │   ├── module
│       │       │   │   └── my-all-in-one-project-share-jar
│       │       │   │       └── module.properties
│       │       │   └── web-extension
│       │       │       ├── messages
│       │       │       │   └── my-all-in-one-project-share-jar.properties
│       │       │       ├── my-all-in-one-project-share-jar-slingshot-application-context.xml
│       │       │       ├── site-data
│       │       │       │   └── extensions
│       │       │       │       └── my-all-in-one-project-share-jar-example-widgets.xml
│       │       │       └── site-webscripts
│       │       │           ├── com
│       │       │           │   └── example
│       │       │           │       └── pages
│       │       │           │           ├── simple-page.get.desc.xml
│       │       │           │           ├── simple-page.get.html.ftl
│       │       │           │           └── simple-page.get.js
│       │       │           └── org
│       │       │               └── alfresco
│       │       │                   └── README.md
│       │       └── META-INF
│       │           ├── resources
│       │           │   └── my-all-in-one-project-share-jar
│       │           │       └── js
│       │           │           └── tutorials
│       │           │               └── widgets
│       │           │                   ├── css
│       │           │                   │   └── TemplateWidget.css
│       │           │                   ├── i18n
│       │           │                   │   └── TemplateWidget.properties
│       │           │                   ├── templates
│       │           │                   │   └── TemplateWidget.html
│       │           │                   └── TemplateWidget.js
│       │           └── share-config-custom.xml
│       └── test
│           └── java
│               └── com
│                   └── example


