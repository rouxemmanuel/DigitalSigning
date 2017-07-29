# DigitalSigning 

This is a mirror of the project [(DigitalSigning )](https://github.com/rouxemmanuel/DigitalSigning/) , i just add the
same projects to work with the Alfresco Maven SDK and make compatibile with the most recent standard structure of Alfresco 5.

##### NOTE: Is not ready yet

#### TODO:
 - Resolve some problem on the aspect , the apsect key cn't be add to a content
 - Resolve some standard structure problem
 - Make it work

### Installation

```sh
mvn clean install
```

for generate the amp packages, after that use the alfresco offical tool for the installation on the alfresco.war and share.war

### License 

All right of these code belong to rouxemmanuel.


## The structure of the project based on the official documentation [SDK Project Structure](http://docs.alfresco.com/5.2/concepts/sdk-projects-aio.html)

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


