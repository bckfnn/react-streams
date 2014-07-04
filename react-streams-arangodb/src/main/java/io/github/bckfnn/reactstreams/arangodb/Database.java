package io.github.bckfnn.reactstreams.arangodb;

import org.reactivestreams.Publisher;
import org.vertx.java.core.json.JsonObject;

import com.github.bckfnn.reactstreams.Processor;

public class Database {
    private Client client;
    private String databaseName;
    
    public Database(Client client, String databaseName) {
        this.client = client;
        this.databaseName = databaseName;
    }

    public String getName() {
        return databaseName;
    }

    public String getPath() {
        return "/_db/" + databaseName;
    }
    
    public Request deleteDatabase() {
        return new Request("DELETE", "/_db/_system/_api/database/" + databaseName, null);
    }
    
    public Collection getCollection(String collectionName) {
        return new Collection(this, collectionName);
    }
    
    public Request createCollection(String collectionName, JsonObject parm) {
        parm.putString("name", collectionName);
        return new Request("POST", getPath() + "/_api/collection", parm);
    }
    
    public Request createCollection(String collectionName) {
        return createCollection(collectionName, new JsonObject());
    }

    public Request listCollections() {
        return new Request("GET", getPath() + "/_api/collections", null);
    }
    
    public Request loadDocument(String id) {
        return new Request("GET", getPath() + "/_api/document/" + id, null);
    }
    
    public Processor<Request, Response> process() {
        return client.process();
    }
    
    public Publisher<Void> close() {
        return client.close();
    }
}
