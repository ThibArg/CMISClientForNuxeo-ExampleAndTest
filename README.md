CMISClientForNuxeo-ExampleAndTest
=================================

Java implementation of a CMIS client for Nuxeo, using Apache Chemistry

The goal is to check the speed of some CMIS requests to Nuxeo, because we had a question (on answers.nuxeo.com) on this topic, with somebody having performance troubles for simple requests.

For testing, I built a localhost:8080 nuxeo application with 31,000 "File" document which is not a huge number, but the question was about having "some thousands of documents". I used nuxeo-bulk-importer and its randomImporter to create random documents. Note that for the "getObjectByPath()" query, I had to move some of the existing documents so they were all in the "other" folder

=> You sure need to adapt the hard-coded names/paths if you want to use this app for your own testing

This is a quick and dirty sample java application. Just open it from Eclipse and:

* Update the hard-coded doc names and paths if needed
* Setup the test parameters (kSAME_DOC_FOR_ALL, ...)
* Run
* Check the output to get the results

###Create the executable jar from eclipse
* You can create sopme jars with different settings for the testing.
* "File" -> "Export" -> "Runnable jar File"
* (select the confguration? Easiest way: When you first ran your app, for test, your selected "Java Application" and eclipse create a default configuration. Use it)
* Select "Package required libraries into generated jar"
* Export
* To execute it:

cd the/firectory/of/the/jar
java -jar yourjar.jar

## About Nuxeo

Nuxeo provides a modular, extensible Java-based [open source software platform for enterprise content management](http://www.nuxeo.com/en/products/ep) and packaged applications for [document management](http://www.nuxeo.com/en/products/document-management), [digital asset management](http://www.nuxeo.com/en/products/dam) and [case management](http://www.nuxeo.com/en/products/case-management). Designed by developers for developers, the Nuxeo platform offers a modern architecture, a powerful plug-in model and extensive packaging capabilities for building content applications.

More information on: <http://www.nuxeo.com/>
