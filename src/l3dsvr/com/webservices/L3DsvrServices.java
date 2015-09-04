package l3dsvr.com.webservices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

@Path("/")
public class L3DsvrServices {
	
/* Comment or discomment the lines below depending on the environment, and update the username ("calvodea") */
	String basePath = "/var/www/webservices/l3dsvr/"; // [PRODUCTION] Path to server 
	String resultsBaseURL = "http://l3dsvr.peep.ie/data/"; // [PRODUCTION] URL to files
//	String basePath = "/home/calvodea/workspace/L3Dsvr/"; // [DEVELOPMENT] Path to server 
//	String resultsBaseURL = basePath + "data/"; // [DEVELOPMENT] URL to files
	
	@POST
	@Path("/addFile")
	@Produces(MediaType.APPLICATION_JSON)
	/* Receives the URL of a file, the customerId, projectId, source language and target language
	 * and copies it into the folder data/customerId/projectId/ in the L3Dsvr server */
	public Response addFileService(
			@FormParam("sourceURL") String sourceFileURL, // e.g. "https://docs.shopify.com/manual/your-store/products/product_template.csv"
			@FormParam("custId") String customerId, // e.g. "1234"
			@FormParam("projId") String projectId) // e.g. "5678"
					throws IOException, InterruptedException, DOMException, SAXException { 
		
		// If URL is not accessible, return error
		if (isURLAccessible(sourceFileURL) == false) {
			String unavailableFileResponse = "{\"error\": \"Input file not available\"}";			
			return Response.ok(unavailableFileResponse, MediaType.APPLICATION_JSON).build();
		}
		
		// If URL is not in the correct format, return error
		if (isFileInCorrectFormat(sourceFileURL) == false) {
			String wrongFormatFileResponse = "{\"error\": \"Wrong format in input file. Expecting .csv or .csvm extension.\"}";			
			return Response.ok(wrongFormatFileResponse, MediaType.APPLICATION_JSON).build();
		}
		
		String localFilePath = copyFileToLocalhost(sourceFileURL, customerId, projectId);
				
		String jsonResponse = "{\"outputURL\": \"" + localFilePath + "\"}";
		
		return Response.ok(jsonResponse, MediaType.APPLICATION_JSON).build();
	}


	/* Checks that the provided URL is accessible */
	private boolean isURLAccessible(String xliffFileURL)
			throws MalformedURLException, IOException, ProtocolException {

		URL xliffURL = new URL(xliffFileURL);
		HttpURLConnection testConnection = (HttpURLConnection)xliffURL.openConnection();
		testConnection.setRequestMethod("HEAD"); 
		int code = testConnection.getResponseCode();
		
		if (code == HttpURLConnection.HTTP_OK) // OK, accessible
			return true;
		else // Not accessible
			return false;
	}
	
	
	/* Checks that if the file is in the correct CSV or CSVM format */
	private boolean isFileInCorrectFormat(String sourceFileURL)
			throws MalformedURLException, IOException, ProtocolException {

		// Get the file extension
		String fileExtension = FilenameUtils.getExtension(sourceFileURL);
		
		if (fileExtension.equalsIgnoreCase("csv") || fileExtension.equalsIgnoreCase("csvm")) // OK, correct format
			return true;
		else // Wrong format
			return false;
	}
	
	
	/* Make an exact copy of [remoteFile] in data/customerId/projectId/sourceLanguage/targetLanguage/[remoteFile]
	 * in the L3Dsvr server, returning the path */
	private String copyFileToLocalhost(String remoteFileURL, String customerId, String projectId) 
			throws IOException {

		URL inputURL = new URL(remoteFileURL);
		String fileName = FilenameUtils.getName(remoteFileURL);

		String localFilePath = basePath + "data/" + customerId + "/" + projectId;
		
		String localFileURL = resultsBaseURL + customerId + "/" + projectId + "/" + fileName;
		
		InputStream inputFile = inputURL.openStream();
		
	    File localFileDirectory = new File(localFilePath);
	    // Check if folders do not exist, and create them if so
	    if(localFileDirectory.exists() == false) {
	    	localFileDirectory.mkdirs();
	    }
	    
	    localFileDirectory.setWritable(true); // Give permissions to write in this folder
	    localFileDirectory.setReadable(true); // Give permissions to read in this folder
	    localFileDirectory.setExecutable(true); // Give permissions to execute in this folder
	    		
		FileOutputStream localFile = new FileOutputStream(localFilePath + "/" + fileName);
		final int BUF_SIZE = 1 << 8;
		byte[] buffer = new byte[BUF_SIZE];
		int bytesRead = -1;
		while((bytesRead = inputFile.read(buffer)) > -1) {
			localFile.write(buffer, 0, bytesRead);
		}
		
		inputFile.close();
		localFile.close();
		
		return localFileURL;
	}
}