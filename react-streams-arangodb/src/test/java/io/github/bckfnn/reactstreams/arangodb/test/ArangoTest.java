/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.bckfnn.reactstreams.arangodb.test;

import io.github.bckfnn.reactstreams.Stream;
import io.github.bckfnn.reactstreams.arangodb.Client;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

public class ArangoTest extends TestVerticle {

    @Test
    public void dummy() {
        VertxAssert.testComplete();
    }
    
    private Stream<?> init(Client client) {
        return client
                .init("127.0.0.1", 8529)
                .printStream("after connect", System.out)
                .whenDone(client.getDatabase("test").databaseCreate());
    }

    //@Test
    public void initTest() {
        final Client client = new Client(null);
        init(client)
        .printStream("after open", System.out)
        .whenDone(client.close())
        .printStream("after close", System.out)
        .whenDone(() -> "abc")
        .printStream("after lambda", System.out)
        .whenDone(() -> VertxAssert.testComplete())
        .start(1);
    }

    //@Test
    public void initListdatabases() {
        final Client client = new Client(null);
        init(client)
        .printStream("after init", System.out)
        .whenDone(client.databasesList())
        .printStream("after list ", System.out)
        .printStream("after list process", System.out)
        .whenDone(() -> VertxAssert.testComplete())
        .start(1);
    }

    //@Test
    public void save() {
        final Client client = new Client(null);
        init(client)
        .whenDone(() -> {
            JsonObject v = new JsonObject();
            v.putString("name", "the name");
            v.putNumber("value", 1233);
            return client.getDatabase("test").documentCreate("test", true, true, v);            
        })
        .printStream("after save", System.out)
        .whenDone(() -> VertxAssert.testComplete())
        .start(1);
    }

    //@Test
    public void saveMultiple() {
        final Client client = new Client(null);

        List<Integer> lst = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            lst.add(i);
        }

        init(client)
        .whenDone(Stream.from(lst))
        .map((i) -> {
            JsonObject v = new JsonObject();
            v.putString("name", "the name");
            v.putNumber("value", i);
            return client.getDatabase("test").documentCreate("test", true, true, v);
        })
        .printStream("after save", System.out)
        .whenDone(() -> VertxAssert.testComplete())
        .start(1);
    }

    //@Test
    public void load() {
        final Client client = new Client(null);
        init(client)
        .whenDoneFunc(() -> {
            JsonObject v = new JsonObject();
            v.putString("name", "the name");
            v.putNumber("value", 1233);
            return client.getDatabase("test").documentCreate("test", true, true, v);            
        })
        .map(r -> r._key)
        .mapMany(key -> {
            return client.getDatabase("test").documentLoad(key);
        })
        .map(r -> r.doc)
        .printStream("after load", System.out)
        .whenDone(() -> VertxAssert.testComplete())
        .start(1);
    }


    //@Test
    public void update() {
        final Client client = new Client(null);
        init(client)
        .whenDoneFunc(() -> {
            JsonObject v = new JsonObject();
            v.putString("name", "the name");
            v.putNumber("value", 1233);
            return client.getDatabase("test").documentCreate("test", true, true, v);            
        })
        .map(r -> r._key)
        .mapMany(key -> {
            return client.getDatabase("test").documentLoad(key);
        })
        .printStream("after load", System.out)
        .mapMany(r -> {
            int n = (Integer) r.doc.get("value");
            r.doc.put("value",  n + 1);
            return client.getDatabase("test").documentUpdate(r._key, r._rev, true, true, "pol", r.doc);
        })
        .printStream("after save", System.out)
        .whenDone(() -> VertxAssert.testComplete())
        .start(1);
    }
}
