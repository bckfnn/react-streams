package io.github.bckfnn.reactstreams.arangodb.test;

import io.github.bckfnn.reactstreams.arangodb.Client;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

import com.github.bckfnn.reactstreams.Builder;
import com.github.bckfnn.reactstreams.Operations;



public class ArangoTest extends TestVerticle {

    private Operations<?> init(Client client) {
        return client
                .init("127.0.0.1", 8529)
                .printStream("after connect", System.out)
                .whenDoneValue(client.createDatabase("test"))
                .then(client.process());
    }

    @Test
    public void initTest() {
        final Client client = new Client(getVertx());
        init(client)
        .printStream("after open", System.out)
        .whenDone(client.close())
        .printStream("after close", System.out)
        .whenDone(() -> "abc")
        .printStream("after lambda", System.out)
        .whenDone(() -> VertxAssert.testComplete())
        .start(1);
    }

    @Test
    public void initListdatabases() {
        final Client client = new Client(getVertx());
        init(client)
        .printStream("after init", System.out)
        .whenDoneValue(client.listDatabases())
        .printStream("after list ", System.out)
        .then(client.process())
        .printStream("after list process", System.out)
        .whenDone(() -> VertxAssert.testComplete())
        .start(1);
    }

    @Test
    public void save() {
        final Client client = new Client(getVertx());
        init(client)
        .whenDone(() -> {
            JsonObject v = new JsonObject();
            v.putString("name", "the name");
            v.putNumber("value", 1233);
            return client.getDatabase("test").getCollection("test").createDocument(v, true);            
        })
        .then(client.process())
        .printStream("after save", System.out)
        .whenDone(() -> VertxAssert.testComplete())
        .start(1);
    }

    @Test
    public void saveMultiple() {
        final Client client = new Client(getVertx());

        List<Integer> lst = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            lst.add(i);
        }

        init(client)
        .whenDone(Builder.from(lst))
        .map((i) -> {
            JsonObject v = new JsonObject();
            v.putString("name", "the name");
            v.putNumber("value", i);
            return client.getDatabase("test").getCollection("test").createDocument(v, true);
        })
        .then(client.process())
        .printStream("after save", System.out)
        .whenDone(() -> VertxAssert.testComplete())
        .start(1);
    }

    @Test
    public void load() {
        final Client client = new Client(getVertx());
        init(client)
        .whenDone(() -> {
            JsonObject v = new JsonObject();
            v.putString("name", "the name");
            v.putNumber("value", 1233);
            return client.getDatabase("test").getCollection("test").createDocument(v, true);            
        })
        .then(client.process())
        .map((r) -> r.getBody().getString("_key"))
        .map((key) -> {
            return client.getDatabase("test").getCollection("test").loadDocument(key);
        })
        .then(client.process())
        .map((r) -> r.getBody())
        .printStream("after load", System.out)
        .whenDone(() -> VertxAssert.testComplete())
        .start(1);
    }


    @Test
    public void update() {
        final Client client = new Client(getVertx());
        init(client)
        .whenDone(() -> {
            JsonObject v = new JsonObject();
            v.putString("name", "the name");
            v.putNumber("value", 1233);
            return client.getDatabase("test").getCollection("test").createDocument(v, true);            
        })
        .then(client.process())
        .map((r) -> r.getBody().getString("_key"))
        .map((key) -> {
            return client.getDatabase("test").getCollection("test").loadDocument(key);
        })
        .then(client.process())
        .map((r) -> r.getBody())
        .printStream("after load", System.out)
        .map((doc) -> {
            doc.putNumber("value",  doc.getNumber("value").intValue() + 1);
            return client.getDatabase("test").getCollection("test").updateDocument(doc);
        })
        .then(client.process())
        .printStream("after save", System.out)
        .whenDone(() -> VertxAssert.testComplete())
        .start(1);
    }
}
