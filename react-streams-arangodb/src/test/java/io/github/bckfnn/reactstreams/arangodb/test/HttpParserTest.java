package io.github.bckfnn.reactstreams.arangodb.test;

import io.github.bckfnn.reactstreams.arangodb.HttpHeaderParser;
import io.github.bckfnn.reactstreams.arangodb.HttpParser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

public class HttpParserTest {

	@Test
	public void dummy() throws Exception {
	    
	}
	
	@Test
	public void testArrangoResponse() throws Exception {
		byte[] input = read("arango-resp.txt");
		HttpParser p = new HttpParser();
		p.parse(new Buffer(input));
	}
	
	@Test
	public void testGoogle() throws Exception {
		byte[] input = read("google.dk-resp.txt");
		HttpParser p = new HttpParser() {
			int size = 0;
			
			@Override
			public void onVersion(String val) {
				System.out.println(val);
			}

			@Override
			public void onStatus(int status, String val) {
				System.out.println(status + " " + val);
			}

			@Override
			public void onKeyValue(String key, String value) {
				System.out.println(key  +" " +value);
			}

			@Override
			public void onDone() {
				System.out.println("done " + size);
			}

			@Override
			public void onError() {
				System.out.println("error");
			}

			@Override
			public void onBody(byte[] buf, int pos, int len) {
				System.out.println("body:" + pos + " " + len);
				size += len;
			}
			
		};
		p.parse(new Buffer(input));
		
	}
	
	@Test
	public void testGooglePerformance() throws Exception {
		final byte[] input = read("google.dk-resp.txt");
		final HttpParser p = new HttpParser();
		Runnable r = new Runnable() {
			public void run() {
				p.parse(new Buffer(input));
			}
		};
		
		new HttpHeaderParser().speedTest(10, 200000, r);
	}
	
	private byte[] read(String filename) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream fis = getClass().getResourceAsStream("/arango/" + filename);
		byte[] buf = new byte[4096];
		for (int l; (l = fis.read(buf)) > 0; ) {
			baos.write(buf, 0, l);
		}
		fis.close();
		baos.close();
		return baos.toByteArray();
	}
}
