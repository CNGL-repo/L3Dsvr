# L3Dsvr Instructions and Methods

## Description
Core of L3Data server in the FALCON project (http://falcon-project.eu/). 

## Methods

### addCSVFile
Receives the URL of a CSV file and copies it into the relevant folder in the L3Dsvr server
Method: POST addCSVFile
Input: sourceURL, custId, projId, sourceLang, targetLang
Response: outputURL


## How to run locally
1. Import the project in Eclipse JEE Mars from https://github.com/CNGL-repo/L3Dsvr.git
2. On L3DsvrServices.java, comment the [PRODUCTION] lines and discomment the [DEVELOPMENT] ones
3. Update the username, replacing "calvodea" by the correct one
4. Run As -> Run on Server. Choose server Tomcat v8.0
5. From a terminal session, type the following example
   curl -X POST --data "sourceURL=https://docs.shopify.com/manual/your-store/products/product_template.csv&custId=1234&projId=5678&sourceLang=es&targetLang=en" http://localhost:8080/L3Dsvr/api/addCSVFile
6. It will return something like {"outputURL": "/home/calvodea/workspace/L3Dsvr/files/1234/5678/es/en/4.csv"}
7. If you open /home/calvodea/workspace/L3Dsvr/files/1234/5678/es/en/4.csv you will see the csv file

## How to run in production
1. From a terminal session, type the following example
   curl -X POST --data "sourceURL=https://docs.shopify.com/manual/your-store/products/product_template.csv&custId=1234&projId=5678&sourceLang=es&targetLang=en" http://l3dsvr.peep.ie/api/addCSVFile
2. It will return something like {"outputURL": "http://l3dsvr.peep.ie/files/1234/5678/es/en/4.csv"}
3. If you open http://l3dsvr.peep.ie/files/1234/5678/es/en/4.csv you will see the optimal path file

## How to deploy to a production server
1. Install Tomcat 8 in the production server (path /opt/tomcat8)
2. On L3DsvrServices.java, comment the [PRODUCTION] lines and discomment the [DEVELOPMENT] ones
3. Update the productions URLS if this is not L3Dsvr.peep.ie server. Update the basePath if necessary.
4. From Eclipse, select the project and right-click Export -> Web -> WAR file
5. Upload the new L3Dsvr.war file to the server /opt/tomcat8/webapps. It will automatically extract its content into a L3Dsvr folder
6. Restart the Tomcat server: sudo sh /opt/tomcat8/bin/start.sh
7. Follow the instructions in "How to run in production" to test if it is working properly