package io.github.bckfnn.reactstreams.arangodb;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonObject;

public class Response {
    private static Logger LOG = LoggerFactory.getLogger(Response.class);
    
    private Request request;
    private int status;
    private String statusText;
    private JsonObject body;
    private Map<String, String> headers = new HashMap<>();
    public long time;
    
    public Response(Request request) {
        this.request = request;
    }

    public void begin() {
        this.time = System.currentTimeMillis();
    }

    public Request getRequest() {
        return request;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    
    public String getStatusText() {
		return statusText;
	}

	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}

	public JsonObject getBody() {
        return body;
    }
	
	public void setBody(JsonObject body) {
		this.body = body;
	}

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }
    
    public void done() {
        long t = System.currentTimeMillis() - time;
        System.out.println("response took:" + t + " " + body);
        if (t > 100) {
            LOG.info("Response took:{} {}", t, body);
        }
    }
    
    public String toString() {
        return "Response[" + status + " " + body + "]";
    }
}
