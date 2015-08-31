package l3dsvr.com.webservices;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joox.Match;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import static org.joox.JOOX.$;

@Path("/")
public class L3DsvrServices {
	
/* Comment or discomment the lines below depending on the environment, and update the username ("calvodea") */
	String basePath = "/var/www/webservices/L3Dsvr/"; // [PRODUCTION] Path to server 
	String resultsBaseURL = "http://l3dsvr.peep.ie/files/"; // [PRODUCTION] URL to CSV files
//	String basePath = "/home/calvodea/workspace/L3Dsvr/"; // [DEVELOPMENT] Path to server 
//	String resultsBaseURL = basePath + "files/"; // [DEVELOPMENT] URL to CSV files
	
	@POST
	@Path("/addCSVFile")
	@Produces(MediaType.APPLICATION_JSON)
	/* Receives the URL of a CSV file, the customerId, projectId, source language and target language
	 * and copies it into the folder files/customerId/projectId/sourceLanguage/targetLanguage/ in the L3Dsvr server */
	public Response addCSVFileService(
			@FormParam("sourceURL") String sourceFileURL, // e.g. "https://docs.shopify.com/manual/your-store/products/product_template.csv"
			@FormParam("custId") String customerId, // e.g. "1234"
			@FormParam("projId") String projectId, // e.g. "5678"
			@FormParam("sourceLang") String sourceLanguage, // e.g. "pl"
			@FormParam("targetLang") String targetLanguage) // e.g. "en"
					throws IOException, InterruptedException, DOMException, SAXException { 
		
		// If URL is not accessible, return error
		if (isURLAccessible(sourceFileURL) == false) {
			String unavailableFileResponse = "{\"error\": \"Input file not available\"}";			
			return Response.ok(unavailableFileResponse, MediaType.APPLICATION_JSON).build();
		}
		
		// If URL is not in the correct format, return error
		if (isFileInCorrectFormat(sourceFileURL) == false) {
			String wrongFormatFileResponse = "{\"error\": \"Wrong format in input file. Expecting .csv extension.\"}";			
			return Response.ok(wrongFormatFileResponse, MediaType.APPLICATION_JSON).build();
		}
		
		// Generate a new processingId for the optimal path generation
		String processingId = getNextProcessingId();
		
		String localFilePath = copyFileToLocalhost(sourceFileURL, customerId, projectId, sourceLanguage, targetLanguage, processingId);
				
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
	
	
	/* Checks that if the file is in the correct CSV format */
	private boolean isFileInCorrectFormat(String sourceFileURL)
			throws MalformedURLException, IOException, ProtocolException {

		// Get the file extension
		String fileExtension = sourceFileURL.substring(sourceFileURL.length() - 3, sourceFileURL.length());
		
		if (fileExtension.equalsIgnoreCase("csv")) // OK, correct format
			return true;
		else // Wrong format
			return false;
	}
	
	
	// Read from sequence.txt the number, increment it + 1, overwrite the file and return it
	private String getNextProcessingId() {
		
		// Read original number
		File file = new File(basePath + "sequence.txt");
		BufferedReader reader = null;
		String currentProcessingId = null;
		
		try {
		    reader = new BufferedReader(new FileReader(file));
		    currentProcessingId = reader.readLine(); // Read number from file
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    try {
		        if (reader != null) {
		        	int nextProcessingId = Integer.parseInt(currentProcessingId) + 1;
		            reader.close(); // Close read file
		        	BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		        	writer.write(Integer.toString(nextProcessingId));
		        	writer.close(); // Close write file
		        }
		    } catch (IOException e) {
		    }
		}
		
		return currentProcessingId;
	}
	
	
	/* Make an exact copy of the remote CSV file in files/customerId/projectId/sourceLanguage/targetLanguage/processingId.csv
	 * in the L3Dsvr server, returning the path */
	private String copyFileToLocalhost(String remoteFileURL, String customerId, String projectId,
			String sourceLanguage, String targetLanguage, String processingId) throws IOException {
		
		String localFilePath = basePath + "files/" + customerId + "/" + projectId + "/" +
				sourceLanguage + "/" + targetLanguage;
		
		String localFileURL = resultsBaseURL + customerId + "/" + projectId + "/" +
				sourceLanguage + "/" + targetLanguage + "/" + processingId + ".csv";
		
		URL inputURL = new URL(remoteFileURL);
		InputStream inputCSVFile = inputURL.openStream();
		
	    File localFileDirectory = new File(localFilePath);
	    // Check if folders do not exist, and create them if so
	    if(localFileDirectory.exists() == false) {
	    	localFileDirectory.mkdirs();
	    }
	    
	    localFileDirectory.setWritable(true); // Give permissions to write in this folder
	    localFileDirectory.setReadable(true); // Give permissions to read in this folder
	    localFileDirectory.setExecutable(true); // Give permissions to execute in this folder
	    		
		FileOutputStream localCSVFile = new FileOutputStream(localFilePath + "/" + processingId + ".csv");
		final int BUF_SIZE = 1 << 8;
		byte[] buffer = new byte[BUF_SIZE];
		int bytesRead = -1;
		while((bytesRead = inputCSVFile.read(buffer)) > -1) {
			localCSVFile.write(buffer, 0, bytesRead);
		}
		
		inputCSVFile.close();
		localCSVFile.close();
		
		return localFileURL;
	}
}