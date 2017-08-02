# DigitalSigning 

This is a mirror of the project [(DigitalSigning )](https://github.com/rouxemmanuel/DigitalSigning/) , i just add the
same projects to work with the Alfresco Maven SDK and make compatibile with the most recent standard structure of Alfresco 5.

##### NOTE: Is not ready yet

#### TODO:
 - Resolve some problem on the aspect , the apsect key can't be add to a content
 - [SOLVED]Resolve some standard structure problem
 - Try to understand how use the replace parameter on the config tag for implement with other amp
 - Implement a more simple sign wihtout all the parmaeter on the position of the stamp.
 - Strange problem with signed the document , the addon say the document is signed with success but i can't see the generate signed content on the destination folder i choose.
 
 - Make it work like the older version (more or less the final objective)
 
###### [Reminder compatibility the version of sdk with alfresco version](http://docs.alfresco.com/5.1/concepts/alfresco-sdk-compatibility.html)
###### [New compatibility with the SDK 3.0.0](http://ecmarchitect.com/archives/2017/04/24/4235)

### Installation

```sh
mvn clean install <MAVEN_PROJECT>
```

for generate the amp packages, after that use the alfresco offical tool for the installation on the alfresco.war and share.war

### License 

All right of these code belong to rouxemmanuel.

#### The structure of the project based on the official documentation [SDK Project Structure](http://docs.alfresco.com/5.1/tasks/alfresco-sdk-tutorials-share-amp-archetype.html) for the new SDK 2.2.0

#### The structure of the project based on the official documentation [SDK Project Structure](http://docs.alfresco.com/5.2/concepts/sdk-projects-aio.html) for the new SDK 3.0.0

