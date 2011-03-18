package org.maski.pogic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class ModManifest {
	private List<Entry> entries = new ArrayList<Entry>();
	private URL urlBase;
	public class Entry {
		public URL url;
		public String hash;
		public long length;
		public Entry(URL url, String hash, long length) {
			this.url = url;
			this.hash = hash;
			this.length = length;
		}
	}
	
	public ModManifest() {
		
	}

	public ModManifest(URL urlBase) {
		this.urlBase = urlBase;
	}

	public int size() {
		return this.entries.size();
	}
	
	public List<Entry> getEntries() {
		return this.entries;
	}
	
	public void add(URL url, String hash, long length) {
		this.entries.add(new Entry(url, hash, length));
	}
	
	public void add(File file) {
        String shaHash = this.getHash(file);
        
        try {
			this.add(new URL(this.urlBase.toString() + file.getName()), shaHash, file.length());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	String getHash(File file) {
        MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) {
			throw new RuntimeException(e1);
		}
        FileInputStream fis;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
 
        byte[] dataBytes = new byte[1024];
 
        int nread = 0; 
        try {
			while ((nread = fis.read(dataBytes)) != -1) {
			  md.update(dataBytes, 0, nread);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		};
        byte[] mdbytes = md.digest();
 
        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
          sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        
        return sb.toString();
    }
}
