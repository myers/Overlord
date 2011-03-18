package org.maski.pogic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import javax.activation.MimetypesFileTypeMap;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class PogicHttpServer {
	// FIXME: replace this with http://elonen.iki.fi/code/nanohttpd/
	
	private static Logger l = Logger.getLogger("Minecraft.PogicPlugin");
	private HttpServer server;
	public PogicHttpServer(File root, int port) {
		try {
			server = HttpServer.create(new InetSocketAddress(port), 100);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		server.createContext("/", new FileHandler(root));
		server.start();
	}
	public void stop() {
		server.stop(0);
	}


	private class FileHandler implements HttpHandler {
		private File root;
		public FileHandler(File root) {
			super();
			this.root = root;
		}
		public void handle(HttpExchange xchg) throws IOException {
			String path = xchg.getRequestURI().normalize().toString();
			File file = new File(this.root, path.substring(1));
			if (!file.exists()) {
				l.info("HTTP 404 " + path);
				xchg.sendResponseHeaders(404, 0);
				return;
			}
			l.info("HTTP 200 " + path);
			xchg.sendResponseHeaders(200, file.length());
			
			FileInputStream input = new FileInputStream(file);
			
			Headers headers = xchg.getResponseHeaders();
			headers.add("Content-Type", new MimetypesFileTypeMap().getContentType(file.getName()));
			OutputStream output = xchg.getResponseBody();
		    byte[] buf = new byte[16384];
		    while (true) {
		      int length = input.read(buf);
		      if (length < 0)
		        break;
		      output.write(buf, 0, length);
		    }
		    input.close();
		    output.close();
		}
	}

}

