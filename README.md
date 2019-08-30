# DigitalSigning 

Updated to work with Alfresco SDK 4 and Alfresco community 6.1.2
 
###### [Reminder compatibility the version of sdk with alfresco version](http://docs.alfresco.com/5.1/concepts/alfresco-sdk-compatibility.html)
###### [New compatibility with the SDK 3.0.0](http://ecmarchitect.com/archives/2017/04/24/4235)

### Installation

```sh
mvn clean install
```

for generate the amp packages, after that use the alfresco offical tool for the installation on the alfresco.war and share.war
or the following command with the assembly plugin

```sh
mvn clean source:jar install package assembly:single 
```

## Realese  ###

Modified by Dercioink


### License 

All right of these code belong to rouxemmanuel.

#### The structure of the project based on the official documentation [SDK Project Structure](http://docs.alfresco.com/5.2/concepts/sdk-projects-aio.html) for the new SDK 3.0.0

