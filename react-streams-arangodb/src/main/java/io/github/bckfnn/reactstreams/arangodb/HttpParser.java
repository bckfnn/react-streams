package io.github.bckfnn.reactstreams.arangodb;

import org.vertx.java.core.buffer.Buffer;

public class HttpParser {
	final static int HEADER = 1;
	final static int CHUNK_HEADER = 2;
	final static int CHUNK_DATA = 3;
	final static int RAW_DATA = 4;
    
	private int state = HEADER;

	private boolean chunked;
	private int contentlength;
	private int remaining;
	
	HttpHeaderParser headerParser = new HttpHeaderParser() {
		String key;
		
		
		@Override
		public void onVersion(String val) {
			HttpParser.this.onVersion(val);
		}

		@Override
		public void onStatus(int status, String val) {
			HttpParser.this.onStatus(status, val);
		}

		@Override
		public void onKey(String val) {
			key = val;
		}

		@Override
		public void onValue(String val) {
			if ("transfer-encoding".equalsIgnoreCase(key) && "chunked".equalsIgnoreCase(val)) {
				chunked = true;
			} else if ("content-length".equalsIgnoreCase(key)) {
				remaining = contentlength = Integer.parseInt(val);
			}
			onKeyValue(key, val);
		}

		@Override
		public void onDone() {
			if (chunked) {
				contentlength = 0;
				state = CHUNK_HEADER;
			} else if (contentlength == 0) {
				HttpParser.this.onDone();
				state = HEADER;
			} else if (contentlength > 0) {
				state = RAW_DATA;
			} else {
				HttpParser.this.onError();
				state = HEADER;
			}
		}

		@Override
		public void onError() {
			HttpParser.this.onError();
			state = HEADER;
		}
	};

	ChunkParser chunkParser = new ChunkParser() {
		public void onLength(int len) {
			remaining = contentlength = len;
			state = CHUNK_DATA;
		}

		public void onDone() {
			if (contentlength == 0) {
				HttpParser.this.onDone();
				state = HEADER;
			}
		}

		public void onError() {
			HttpParser.this.onError();
			state = HEADER;
		}
	};
	
	int size;
	int pos; 
	public void parse(Buffer buffer) {
		byte[] input = buffer.getBytes();
	    int size = input.length;
	    int pos = 0;
	    while (pos < size) {
	        switch (state) {
	        case HEADER:
	            pos = headerParser.parse(input, pos);
	            break;

	        case CHUNK_HEADER:
	        	pos = chunkParser.parse(input, pos);
	        	break;

	        case CHUNK_DATA:
	            int chunksize = Math.min(size - pos, contentlength);
	            if (chunksize > 0) {
	            	onBody(input, pos, chunksize);
	            	remaining -= chunksize;
	            	pos += chunksize;
	            }

	            if (remaining == 0) {
	                state = CHUNK_HEADER;
	            }
	            break;

	        case RAW_DATA: 
	            chunksize = Math.min(size - pos, contentlength);
	            onBody(input, pos, chunksize);
	            remaining -= chunksize;
	            pos += chunksize;

	            if (remaining == 0) {
	            	onDone();
	                state = HEADER;
	            }
	            break;
	        }
	    }
	}
	
	public void onVersion(String val) {
		
	}

	public void onStatus(int status, String val) {
		
	}
	
	public void onKeyValue(String key, String value) {
		
	}
	
	public void onDone() {
		
	}
 
	public void onError() {
		
	}
	
	public void onBody(byte[] buf, int pos, int len) {
		//System.out.println(new String(buf, pos, len));
	}
}
