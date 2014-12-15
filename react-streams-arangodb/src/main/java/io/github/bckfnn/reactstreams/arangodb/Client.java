package io.github.bckfnn.reactstreams.arangodb;

import io.github.bckfnn.reactstreams.BaseProcessor;
import io.github.bckfnn.reactstreams.Stream;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.core.net.NetSocket;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Client {
    private static Logger LOG = LoggerFactory.getLogger(Client.class);

    private Vertx vertx;
    private NetClient client;
    private NetSocket socket;

    Queue<Response> queue = new LinkedList<>();

    Response active;

    public Client(Vertx vertx) {
        this.vertx = vertx;
    }

    /**
     * Connect the server using default host and port.
     * @return a publisher with the result of the connect.
     */
    public Stream<JsonObject> init() {
        return init("localhost", 8529);
    }

    /**
     * Connect the server using default host and port.
     * @param host the host string.
     * @param port the port number.
     * @return a publisher of the connection result.
     */
    public Stream<JsonObject> init(final String host, final int port) {
        return Stream.asOne(subscription -> {
            client = vertx.createNetClient();

            client.connect(port, host, new Handler<AsyncResult<NetSocket>>() {
                public void handle(AsyncResult<NetSocket> asyncResult) {
                    System.out.println("connected " + asyncResult + " " + asyncResult.succeeded() + " " + asyncResult.result());
                    if (asyncResult.succeeded()) {
                        socket = asyncResult.result();
                        socket.dataHandler(new DataHandler());
                        socket.closeHandler(new Handler<Void>() {
                            @Override
                            public void handle(Void event) {
                                System.out.println("socket close");
                                socket = null;
                            }
                        });
                        subscription.sendComplete();
                    } else {
                        asyncResult.cause().printStackTrace();
                        subscription.sendError(asyncResult.cause());
                    }            
                }
            });
        });
    }


    class DataHandler implements Handler<Buffer> {
        ObjectMapper mapper = new ObjectMapper();

        HttpParser httpParser = new HttpParser() {
            //JsonParser jsonParser = new JsonParser();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            public void onVersion(String val) {
            }

            public void onStatus(int status, String val) {
                active.setStatus(status);
                active.setStatusText(val);
                baos = new ByteArrayOutputStream();
                LOG.debug("recieved status {} {}", status, val);
            }

            public void onKeyValue(String key, String value) {
                active.setHeader(key, value);
                LOG.debug("recieved header {} {}", key, value);
            }

            public void onDone() {
                try {
                    byte[] body = baos.toByteArray();

                    @SuppressWarnings("unchecked")
                    Map<String, Object> m = mapper.readValue(body, Map.class);
                    active.setBody(new JsonObject(m));
                    //LOG.debug("onDone body len {}, {}", body.length, new String(body));
                    //active.setBody(new JsonObject(new String(baos.toByteArray(), "UTF-8")));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                active.done();
                active = queue.poll();
                if (active != null) {
                    active.begin();
                    active.getRequest().send(socket);
                }
                LOG.debug("next pending {}", active);
            }

            public void onBody(byte[] buf, int pos, int len) {
                LOG.debug("onBody chunk len {}", len);
                //jsonParser.parse(buf, pos, len);
                baos.write(buf, pos, len);
            }
        };

        @Override
        public void handle(Buffer event) {
            LOG.debug("input {} bytes", event.length());
            httpParser.parse(event);
        }
    }

    public Database getDatabase(String name) {
        return new Database(this, name);
    }

    public Request listDatabases() {
        return new Request("GET", "/_api/database", null);
    }

    public Request createDatabase(String name) {
        return createDatabase(name, new JsonObject());
    }

    public Request createDatabase(String name, JsonObject details) {
        details.putString("name", name);
        return new Request("POST", "/_api/database", details);
    }

    public Publisher<Void> close() {
        return Stream.asOne(subscription -> {
            client.close();
            subscription.sendComplete();
        });
    }

    public Request getVersion(boolean details) {
        return new Request("GET", "/_api/version?details=" + details, null);
    }

    public BaseProcessor<Request, Response> process() { 
        return new BaseProcessor<Request, Response>() {
            @Override
            public void doNext(final Request req) {
                Response resp = new Response(req) {
                    @Override
                    public void done() {
                        super.done();
                        sendNext(this);
                    }
                };
                //System.out.println("active " + active + " " + req);
                if (active == null) {
                    active = resp;
                    active.begin();
                    req.send(socket);
                } else {
                    LOG.debug("queue request {} {}", req.getMethod(), req.getUri());
                    queue.add(resp);
                }
            }
        };
    }
}
