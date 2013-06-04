import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.MessageDigest;

import sun.security.provider.MD5;

/**
 * Curl replacement for Windows based machines...
 * 
 * @author Leonard Wolters
 */
public class curl {

	public static void main(String[] args) {
		
		// url
		String url = getArgument(args, "-h", false);
		if(url == null || url.trim().length() == 0) {
			System.err.println(String.format("No host. Please set one by using " +
					"'-h <<host>>'. Exiting", url));
			System.exit(0);
		}
		
		// input file..
		File inputFile = null;
		String val = getArgument(args, "-f", true);
		if(val != null && val.trim().length() > 0) {
			inputFile = new File(val);
			if(!inputFile.exists()) {
				System.err.println(String.format("Inputfile[%s] doesn't denote an existing file. Exiting", val));
				System.exit(0);
			}
		} 

		// md5 password
		val = getArgument(args, "-p", true);
		if(val != null && val.trim().length() > 0) {
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				System.out.println(String.format("MD5(%s) -> %s", val, 
						md.digest(val.getBytes(Charset.forName("UTF-8")))));
			} catch (Exception e) {
				System.err.println(e.getMessage());
				System.exit(0);
			}
		}
		
		try {
			// open connection
			URL u = new URL(url);
			URLConnection con = u.openConnection();
			
			if(inputFile != null) {
				con.setDoOutput(true);
				con.addRequestProperty("Content-Type", "application/json");
				
				// upload file
				byte[] buf = new byte[(int) inputFile.length()];
				FileInputStream fis = new FileInputStream(inputFile);
				fis.read(buf);
				con.getOutputStream().write(buf);
				fis.close();
			}
			
			// get response...
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line = null;
			while((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
		
	}
	
	private static String getArgument(String[] args, String argument, boolean suppressWarning) {		
		for(int i = 0 ; i < args.length; i++) {
			String s = args[i];
			
			if(s.equals(argument)) {
				if(i == (args.length-1)) {
					if(!suppressWarning) 
						System.err.println(String.format("Warning: argument[%s] is last element, "
							+ "no value found", argument));
					return null;
				} 
				return args[i + 1];
			}
		}
		if(!suppressWarning) 
			System.err.println(String.format("Warning: no value found for argument[%s]", argument));
		return null;
	}
}
