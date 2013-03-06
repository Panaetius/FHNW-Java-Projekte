package ch.fhnw.ds.internet.webserver;

//
// The root directory of the server is the directory in which the server
// was started.
//
// start with options 8080 false or 443 true
//
////////////////////////////////////////////////////////////////////////

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.net.ssl.SSLServerSocketFactory;

/**
 * Ein ganz einfacher Web-Server auf TCP und einem beliebigen Port. 
 * Der Server ist in der Lage, Seitenanforderungen lokal zu dem 
 * Verzeichnis, aus dem er gestartet wurde, zu bearbeiten. Wurde 
 * der Server z.B. im Verzeichnis c:\tmp gestartet, so w�rde eine 
 * Seitenanforderung http://localhost:80/test/index.html die Datei 
 * c:\tmp\test\index.html laden. 
 * CGIs, Servlets oder �hnliches wird nicht unterst�tzt.
 * <p>
 * Die Dateitypen .htm, .html, .gif, .jpg und .jpeg werden erkannt
 * und mit korrekten MIME-Headern �bertragen, alle anderen Dateien
 * werden als "application/octet-stream" �bertragen was z.B. gerade
 * das richtige Format f�r Java Applets ist. 
 * Jeder Request wird durch einen eigenen Client-Thread bearbeitet, 
 * nach �bertragung der Antwort schlie�t der Server den Socket. 
 * Antworten werden mit HTTP/1.0-Header gesendet.
 *
 * Quelle: Go To Java2
 */
public class Server {

	public static void main(String args[]) throws Exception {
		int port;
		try {
			port = Integer.parseInt(args[0]);
		} catch (Exception e) {
			port = 8080;
		}
		
		boolean secure;
		try {
			secure = Boolean.valueOf(args[args.length - 1]).booleanValue();
		} catch (Exception e) {
			secure = false;
		}

		System.out.print("WebServer listening to port " + port);
		if(secure) { System.out.print(" [https]"); }
		System.out.println();
		
		ServerSocket ss;
		if(secure){
			SSLServerSocketFactory ssf =
			  (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
			ss = ssf.createServerSocket(port);
		}
		else {
			ss = new ServerSocket(port);
		}

		int calls = 0;
		while (true) {
			Socket s = ss.accept();
			new BrowserClientThread(++calls, s).start();
		}
	}
}

class BrowserClientThread extends Thread {
	static Map<String, String> mimetypes = new HashMap<String, String>();
	static {
		final String marr[][] = {
			{"acc",		"text/x-account"},
			
			{"xml",		"text/xml"},
			{"abs",		"audio/x-mpeg"},
			{"ai",		"application/postscript"},
			{"aif",		"audio/x-aiff"},
			{"aifc",	"audio/x-aiff"},
			{"aiff",	"audio/x-aiff"},
			{"aim",		"application/x-aim"},
			{"art",		"image/x-jg"},
			{"asf",		"video/x-ms-asf"},
			{"asx",		"video/x-ms-asf"},
			{"au",		"audio/basic"},
			{"avi",		"video/x-msvideo"},
			{"avx",		"video/x-rad-screenplay"},
			{"bcpio",	"application/x-bcpio"},
			{"bin",		"application/octet-stream"},
			{"bmp",		"image/bmp"},
			{"body",	"text/html"},
			{"cdf",		"application/x-cdf"},
			{"cer",		"application/x-x509-ca-cert"},
			{"class",	"application/java"},
			{"cpio",	"application/x-cpio"},
			{"csh",		"application/x-csh"},
			{"css",		"text/css"},
			{"dib",		"image/bmp"},
			{"doc",		"application/msword"},
			{"dtd",		"text/plain"},
			{"dv",		"video/x-dv"},
			{"dvi",		"application/x-dvi"},
			{"eps",		"application/postscript"},
			{"etx",		"text/x-setext"},
			{"exe",		"application/octet-stream"},
			{"gif",		"image/gif"},
			{"gtar",	"application/x-gtar"},
			{"gz",		"application/x-gzip"},
			{"hdf",		"application/x-hdf"},
			{"hqx",		"application/mac-binhex40"},
			{"htm",		"text/html"},
			{"html",	"text/html"},
			{"hqx",		"application/mac-binhex40"},
			{"ief",		"image/ief"},
			{"jad",		"text/vnd.sun.j2me.app-descriptor"},
			{"jar",		"application/java-archive"},
			{"java",	"text/plain"},
			{"jnlp",	"application/x-java-jnlp-file"},
			{"jpe",		"image/jpeg"},
			{"jpeg",	"image/jpeg"},
			{"jpg",		"image/jpeg"},
			{"js",		"text/javascript"},
			{"kar",		"audio/x-midi"},
			{"latex",	"application/x-latex"},
			{"m3u",		"audio/x-mpegurl"},
			{"mac",		"image/x-macpaint"},
			{"man",		"application/x-troff-man"},
			{"me",		"application/x-troff-me"},
			{"mid",		"audio/x-midi"},
			{"midi",	"audio/x-midi"},
			{"mif",		"application/x-mif"},
			{"mov",		"video/quicktime"},
			{"movie",	"video/x-sgi-movie"},
			{"mp1",		"audio/x-mpeg"},
			{"mp2",		"audio/x-mpeg"},
			{"mp3",		"audio/x-mpeg"},
			{"mpa",		"audio/x-mpeg"},
			{"mpe",		"video/mpeg"},
			{"mpeg",	"video/mpeg"},
			{"mpega",	"audio/x-mpeg"},
			{"mpg",		"video/mpeg"},
			{"mpv2",	"video/mpeg2"},
			{"ms",		"application/x-wais-source"},
			{"nc",		"application/x-netcdf"},
			{"oda",		"application/oda"},
			{"pbm",		"image/x-portable-bitmap"},
			{"pct",		"image/pict"},
			{"pdf",		"application/pdf"},
			{"pgm",		"image/x-portable-graymap"},
			{"pic",		"image/pict"},
			{"pict",	"image/pict"},
			{"pls",		"audio/x-scpls"},
			{"png",		"image/png"},
			{"pnm",		"image/x-portable-anymap"},
			{"pnt",		"image/x-macpaint"},
			{"ppm",		"image/x-portable-pixmap"},
			{"ps",		"application/postscript"},
			{"psd",		"image/x-photoshop"},
			{"qt",		"video/quicktime"},
			{"qti",		"image/x-quicktime"},
			{"qtif",	"image/x-quicktime"},
			{"ras",		"image/x-cmu-raster"},
			{"rgb",		"image/x-rgb"},
			{"rm",		"application/vnd.rn-realmedia"},
			{"roff",	"application/x-troff"},
			{"rtf",		"application/rtf"},
			{"rtx",		"text/richtext"},
			{"sh",		"application/x-sh"},
			{"shar",	"application/x-shar"},
			{"smf",		"audio/x-midi"},
			{"snd",		"audio/basic"},
			{"src",		"application/x-wais-source"},
			{"sv4cpio",	"application/x-sv4cpio"},
			{"sv4crc",	"application/x-sv4crc"},
			{"swf",		"application/x-shockwave-flash"},
			{"t",		"application/x-troff"},
			{"tar",		"application/x-tar"},
			{"tcl",		"application/x-tcl"},
			{"tex",		"application/x-tex"},
			{"texi",	"application/x-texinfo"},
			{"texinfo",	"application/x-texinfo"},
			{"tif",		"image/tiff"},
			{"tiff",	"image/tiff"},
			{"tr",		"application/x-troff"},
			{"tsv",		"text/tab-separated-values"},
			{"txt",		"text/plain"},
			{"ulw",		"audio/basic"},
			{"ustar",	"application/x-ustar"},
			{"xbm",		"image/x-xbitmap"},
			{"xpm",		"image/x-xpixmap"},
			{"xwd",		"image/x-xwindowdump"},
			{"wav",		"audio/x-wav"},
			{"wbmp",	"image/vnd.wap.wbmp"},
			{"wml",		"text/vnd.wap.wml"},
			{"wmlc",	"application/vnd.wap.wmlc"},
			{"wmls",	"text/vnd.wap.wmls"},
			{"wmlscriptc",	"application/vnd.wap.wmlscriptc"},
			{"wrl",		"x-world/x-vrml"},
			{"Z",		"application/x-compress"},
			{"z",		"application/x-compress"},
			{"zip",		"application/zip"}
		};
		for(int i=0; i<marr.length; i++){
			mimetypes.put(marr[i][0], marr[i][1]);
		}
	}
	
	private Socket socket;
	private int	id;

	// set by method readRequest
	private String cmd = "";
	private String url = "";
	
	@SuppressWarnings("unused")
	private String httpversion = "";
	
	private int contentLength = 0;

	public BrowserClientThread(int id, Socket socket) {
		this.id = id;
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			System.out.println(id + ": Incoming call...");
			BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			readRequest(r);
			createResponse(r, new PrintStream(socket.getOutputStream()));
		} 
		catch (IOException e) {
			System.out.println(id + ": " + e.toString());
			System.out.println(id + ": Aborted.");
		}
		catch (Exception e){
			System.out.println(id + ": " + e.toString());
		}
		finally{
			try {socket.close();} catch(Exception e){}
			System.out.println(id + ": Closed.");
		}
	}

	private void readRequest(BufferedReader rd) throws IOException {
		List<String> headers = new LinkedList<String>();
		String line = rd.readLine();
		while(line != null && line.length() > 0){
			headers.add(line);
			if(line.toUpperCase().startsWith("CONTENT-LENGTH: ")){
				contentLength = Integer.parseInt(line.substring(15).trim());
			}
			line = rd.readLine();
		}
		if(headers.size()==0) throw new RuntimeException("invalid request");
		
		//Kommando, URL und HTTP-Version aus erster Zeile extrahieren
		StringTokenizer cmdline = new StringTokenizer(headers.get(0));

		if(cmdline.hasMoreTokens()) cmd = cmdline.nextToken().toUpperCase();
		if(cmdline.hasMoreTokens()) url = cmdline.nextToken();
		if(cmdline.hasMoreTokens()) httpversion = cmdline.nextToken();
		
		// debugging output
		Iterator<String> e = headers.iterator();
		while (e.hasNext()) {
			System.out.println("> " + e.next());
		}		
	}

	private void createResponse(BufferedReader in, PrintStream out) {
		if (cmd.equals("GET")) {
			if (!url.startsWith("/")) {
				httpError(out, 400, "Bad Request");
			} 
			else {
				//MIME-Typ aus Dateierweiterung bestimmen
				int pos = url.lastIndexOf(".");
				String ext = pos >= 0 ? url.substring(pos+1) : "";
				String mimestring = mimetypes.get(ext);
				if(mimestring==null)mimestring = "application/octet-stream";

				//convert URL to filename
				String fsep = System.getProperty("file.separator", "/");
				StringBuffer sb = new StringBuffer(url.length());
				for (int i = 1; i < url.length(); i++) {
					char c = url.charAt(i);
					if (c == '/') {
						sb.append(fsep);
					} 
					else {
						sb.append(c);
					}
				}

				File f = null;
				String filename = sb.toString();
				try {
					f = new File(filename);
					if(!f.exists()) {
						String filename2 = "bin"+fsep+filename;
						f = new File(filename2);
					}
					if(!f.exists()) {
						String filename2 = "web"+fsep+filename;
						f = new File(filename2);
					}
				}
				catch(Exception e){}

				try {
					FileInputStream is = new FileInputStream(f);

					//HTTP-Header senden
					out.print("HTTP/1.0 200 OK\r\n");
					out.print("Server: WebServer 0.5\r\n");
					out.print("Content-type: " + mimestring + "\r\n\r\n");
					System.out.println("< HTTP/1.0 200 OK");
					System.out.println("< Server: WebServer 0.5");
					System.out.println("< Content-type: " + mimestring);

					//Dateiinhalt senden
					byte buf[] = new byte[256];
					int len;
					while ((len = is.read(buf)) != -1) {
						out.write(buf, 0, len);
					}
					is.close();
				} 
				catch (FileNotFoundException e) {
					e.printStackTrace();
					httpError(out, 404, "Error Reading File");
				} 
				catch (IOException e) {
					httpError(out, 404, "Not Found");
				} 
				catch (Exception e) {
					e.printStackTrace();
					httpError(out, 404, "Unknown exception");
				}
			}
		}
		else if(cmd.equals("POST")){
			System.out.println("POST BODY ("+contentLength+")");
			try{
				int c = in.read();
				int pos = 0;
				while (c != -1 && pos < contentLength && pos < 1000){
					System.out.print((char)c);
					c = in.read(); pos++;
				}
				if(pos < contentLength) System.out.println("...");
			}
			catch(IOException e){}
			System.out.println();
			
			httpError(out, 501, "POST not implemented");
		}
		else {
			httpError(out, 501, "Not implemented");
		}
	}
		
	/**
	 * Eine Fehlerseite an den Browser senden.
	 */
	private void httpError(PrintStream out, int code, String description) {
		System.out.println(description);
		out.print("HTTP/1.0 " + code + " " + description + "\r\n");
		out.print("Content-type: text/html\r\n\r\n");
		out.println("<html>");
		out.println("<head>");
		out.println("<title>WebServer-Error</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("<h1>HTTP/1.0 " + code + "</h1>");
		out.println("<h3>" + description + "</h3>");
		out.println("</body>");
		out.println("</html>");
	}
}