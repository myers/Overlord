package org.maski.pogic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.NetHandler;
import net.minecraft.server.Packet;

public class Packet200Pogic extends Packet {

	static {
		@SuppressWarnings("rawtypes")
		Class params[] = {Integer.TYPE, Class.class};
		Method method;
		try {
			method = Packet.class.getDeclaredMethod("a", params);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		method.setAccessible(true);
		try {
			method.invoke(Packet.class, 200, Packet200Pogic.class);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	private ModManifest packages;
	private Map<Integer, String> entityMap;
	
	public Packet200Pogic() {
		packages = new ModManifest();
		entityMap = new HashMap<Integer, String>();
	}
	
	public Packet200Pogic(ModManifest packages, Map<Integer, String> entityMap) {
		this.packages = packages;
		this.entityMap = entityMap;
	}
	
	// size of packet
	@Override
	public int a() {
		int acc = 4;
		for (ModManifest.Entry entry : packages.getEntries()) {
			acc += entry.url.toString().length();
			acc += 8;
			acc += entry.hash.length();
		}
		acc += 4;
		for (Map.Entry<Integer, String> entry: entityMap.entrySet()) {
			acc += 4;
			acc += entry.getValue().length();
		}
		return acc;
	}

	@Override
	public void a(DataInputStream in) {
		try {
			int size = in.readInt();
			for (int ii = 0; ii < size; ii++) {
				URL url = new URL(in.readUTF());
				long length = in.readLong();
				String hash = in.readUTF();
				packages.add(url, hash, length);
			}
			int mapSize = in.readInt();
			for (int ii = 0; ii < mapSize; ii++) {
				entityMap.put(in.readInt(), in.readUTF());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public void a(DataOutputStream out) {
		try {
			out.writeInt(packages.size());
			for (ModManifest.Entry entry : packages.getEntries()) {
				out.writeUTF(entry.url.toString());
				out.writeLong(entry.length);
				out.writeUTF(entry.hash);
			}
			out.writeInt(entityMap.size());
			for (Map.Entry<Integer, String> entry: entityMap.entrySet()) {
				out.writeInt(entry.getKey());
				out.writeUTF(entry.getValue());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void a(NetHandler arg0) {
		// FIXME: when we get this on the server side we should then send the client info about his surroundings
	}

}
