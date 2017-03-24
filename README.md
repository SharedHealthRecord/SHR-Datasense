# SHR Datasense
---------------
# Setting up the environment locally

Requisites
------------
* [MySQL](https://www.mysql.com/downloads/)

To package the application
---------------------------
In command line, Navigate into the project directory and run the following command

On Linux/Mac OS :
```
./gradlew clean dist
```
On Windows :
```
gradlew.bat clean dist
```

Prerequisites to run/debug datasense locally
--------------------------------------------
Create a database called 'datasense' in MySQL
```
create database datasense;
```

Edit the local.properties file with appropriate values. They contain default values now. 
Change the URLs and credentials present appropriately.
Change the DHIS2 instance URL, username and password.
Change the IDP URL and IDP Auth Credentails appropriately.
The DATASENSE_CATCHMENT_LIST must be configured with the geo location code from which data is to be read.
The DHIS_AQS_CONFIG_PATH must contain the absolute path to the local file location where the DHIS2 reports are present.

To run the application locally
------------------------------
On Linux/Mac OS :
```
./gradlew runDatasense
```
On Windows :
```
gradlew.bat runDatasense
```

To debug the application locally
------------------------------
On Linux/Mac OS :
```
./gradlew debugDatasense
```
On Windows :
```
gradlew.bat debugDatasense
```
