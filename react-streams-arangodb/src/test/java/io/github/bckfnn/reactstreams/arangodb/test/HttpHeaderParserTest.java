package io.github.bckfnn.reactstreams.arangodb.test;

import io.github.bckfnn.reactstreams.arangodb.HttpHeaderParser;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class HttpHeaderParserTest {

	@Test
	public void testNoHeaders() {
        final String t = 
                "HTTP/1.1 202 OK\r\n"
                        + "\r\n";
        test(t, version("HTTP/1.1"), status(202, "OK"), 
        		done());
	}

	@Test
	public void testArrangoHeaders() {
        final String t = 
                "HTTP/1.1 202 OK\r\n"
                        + "Server:ArangoDB\r\n"
                        + "Connection: Keep-Alive\r\n"
                        + "Content-Type: application/json; charset=utf-8\r\n"
                        + "Content-Length: 81\r\n"
                        + "\r\n";
        test(t, version("HTTP/1.1"), status(202, "OK"), 
        		key("Server"), value("ArangoDB"),
        		key("Connection"), value("Keep-Alive"),
        		key("Content-Type"), value("application/json; charset=utf-8"),
        		key("Content-Length"), value("81"),
        		done());
        
	}
	
	public void test(String input, String... expected) {
		final List<String> actual = new ArrayList<>();
		
		HttpHeaderParser p = new HttpHeaderParser() {

			@Override
			public void onVersion(String val) {
				actual.add(version(val));
			}

			@Override
			public void onStatus(int status, String val) {
				actual.add(status(status, val));
			}

			@Override
			public void onKey(String val) {
				actual.add(key(val));
			}

			@Override
			public void onValue(String val) {
				actual.add(value(val));
			}

			@Override
			public void onDone() {
				actual.add(done());
			}

			@Override
			public void onError() {
				actual.add(error());
			}
			
		};
		p.parse(input.getBytes(), 0);
		//System.out.println(input + " " + actual);
		Assert.assertArrayEquals(expected, actual.toArray());
	}
	
	public String version(String version) {
		return "version:" + version;
	}
	
	public String status(int status, String statusText) {
		return "status:" + status + " " + statusText;
	}

	public String key(String key) {
		return "key:" + key;
	}

	public String value(String value) {
		return "value:" + value;
	}

	public String done() {
		return "done";
	}
	
	public String error() {
		return "error";
	}
}
