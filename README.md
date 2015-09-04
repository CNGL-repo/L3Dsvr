# L3Dsvr Instructions and Methods

## Description
Core of L3Data server in the FALCON project (http://falcon-project.eu/). 

## Methods

### addFile
Receives the URL of a CSV or CSVM file and copies it into the relevant folder in the L3Dsvr server
Method: POST addFile
Input: sourceURL, custId, projId
Response: outputURL


## How to run locally
1. Import the project in Eclipse JEE Mars from https://github.com/CNGL-repo/L3Dsvr.git
2. On L3DsvrServices.java, comment the [PRODUCTION] lines and discomment the [DEVELOPMENT] ones
3. Update the username, replacing "calvodea" by the correct one
4. Run As -> Run on Server. Choose server Tomcat v8.0
5. From a terminal session, type the following example
   curl -X POST --data "sourceURL=https://docs.shopify.com/manual/your-store/products/product_template.csv&custId=1234&projId=5678" http://localhost:8080/L3Dsvr/api/addFile
6. It will return something like {"outputURL": "/home/calvodea/workspace/L3Dsvr/data/1234/5678/product_template.csv"}
7. If you open /home/calvodea/workspace/L3Dsvr/data/1234/5678/product_template.csv you will see the file

## How to run in production
1. From a terminal session, type the following example
   curl -X POST --data "sourceURL=https://docs.shopify.com/manual/your-store/products/product_template.csv&custId=1234&projId=5678" http://l3dsvr.peep.ie/api/addFile
2. It will return something like {"outputURL": "http://l3dsvr.peep.ie/data/1234/5678/product_template.csv"}
3. If you open http://l3dsvr.peep.ie/data/1234/5678/product_template.csv you will see the file

## How to deploy to a production server
1. Install Tomcat 8 in the production server (path /opt/tomcat8)
2. On L3DsvrServices.java, comment the [PRODUCTION] lines and discomment the [DEVELOPMENT] ones
3. Update the productions URLS if this is not L3Dsvr.peep.ie server. Update the basePath if necessary.
4. From Eclipse, select the project and right-click Export -> Web -> WAR file
5. Upload the new L3Dsvr.war file to the server /opt/tomcat8/webapps. It will automatically extract its content into a L3Dsvr folder
6. Restart the Tomcat server: sudo sh /opt/tomcat8/bin/start.sh
7. Follow the instructions in "How to run in production" to test if it is working properly