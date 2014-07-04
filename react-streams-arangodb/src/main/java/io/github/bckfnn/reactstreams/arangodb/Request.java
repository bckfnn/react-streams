package io.github.bckfnn.reactstreams.arangodb;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetSocket;

public class Request {
    private static Logger LOG = LoggerFactory.getLogger(Request.class);

    private final String method;
    private String uri;
    private JsonObject body;
    
    public Request(String method, String uri, JsonObject body) {
        this.method = method;
        this.uri = uri;
        this.body = body;
    }

    public static Request load(String id) {
        return new Request("GET", id, null);
    }
    
    
    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public JsonObject getBody() {
        return body;
    }
    
    public void setBody(JsonObject body) {
        this.body = body;
    }

    public void process(JsonObject response) {
    
    }
    public void send(NetSocket socket) {
        Buffer b = new Buffer();
        b.appendString(method);
        b.appendString(" ");
        b.appendString(uri);
        b.appendString(" ");
        b.appendString("HTTP/1.1\r\n");
        b.appendString("connection: Keep-Alive\r\n");
        byte[] c = null;
        if (body != null) {
            try {
                c = body.toString().getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
        }
        int clen = 0;
        if (c != null) {
            clen = Integer.valueOf(c.length);
        }
        b.appendString("Content-Length:" + clen + "\r\n");
        b.appendString("\r\n");
        if (c != null) {
            b.appendBytes(c);
        }
        LOG.debug("Sending to socket {}", b.toString());
        socket.write(b);
    }
}
