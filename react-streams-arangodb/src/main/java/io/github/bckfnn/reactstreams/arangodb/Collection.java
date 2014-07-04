package io.github.bckfnn.reactstreams.arangodb;

import org.vertx.java.core.json.JsonObject;

public class Collection {
    Database database;
    String collectionName;

    public Collection(Database database, String collectionName) {
        this.database =  database;
        this.collectionName = collectionName;
    }
    
    public String getCollectionPath() {
        return database.getPath() + "/_api/collection/" + collectionName;
    }

    public String getCollectionPath(String suffix) {
        return database.getPath() + "/_api/collection/" + collectionName + suffix;
    }

    public String getDocumentPath(String suffix) {
        return database.getPath() + "/_api/document" + suffix;
    }

    public Request deleteCollection() {
        return new Request("DELETE", getCollectionPath(), null);
    }

    public Request createDocument(final JsonObject body, boolean create) {
        return new Request("POST", getDocumentPath("?collection=" + collectionName + "&createCollection=" + create), body);
    }

    public Request updateDocument(final JsonObject body) {
        return new Request("PATCH", getDocumentPath("/" + body.getString("_id")), body);
    }

    public Request replaceDocument(final JsonObject body) {
        return new Request("PUT", getDocumentPath("/" + body.getString("_id")), body);
    }

    public Request loadDocument(String key) {
        return new Request("GET", getDocumentPath("/" + collectionName + "/" + key), null);
    }

    public Request deleteDocument(JsonObject body) {
        return deleteDocument(body.getString("_key"));
    }
    
    public Request deleteDocument(String key) {
        return new Request("DELETE", getDocumentPath("/" + collectionName + "/" + key), null);
    }
    
    public Request count() {
        return new Request("GET", getCollectionPath("/count"), null);
    }

    public Request load() {
        return new Request("PUT", getCollectionPath("/load"), null);
    }

    public Request unload() {
        return new Request("PUT", getCollectionPath("/unload"), null);
    }
    
    public Request truncate() {
        return new Request("PUT", getCollectionPath("/truncate"), null);
    }

    public Request figures() {
        return new Request("GET", getCollectionPath("/figures"), null);
    }

    public Request listIndexes() {
        return new Request("GET", database.getPath() + "/_api/index?collection=" + collectionName, null);
    }

    public Request getIndex(String id) {
        return new Request("GET", database.getPath() + "/_api/index/" + id, null);
    }

    public Request addIndex(String type, JsonObject parms) {
        parms.putString("type", type);
        return new Request("POST", database.getPath() + "/_api/index/", parms);
    }

    public Request deleteIndex(String id) {
        return new Request("DELETE", database.getPath() + "/_api/index/" + id, null);
    }

}
