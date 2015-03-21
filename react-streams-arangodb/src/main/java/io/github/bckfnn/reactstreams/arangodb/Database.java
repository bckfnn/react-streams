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
package io.github.bckfnn.reactstreams.arangodb;

import io.github.bckfnn.reactstreams.Stream;

import java.util.List;
import java.util.Map;

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

    public String dbPath() {
        return "/_db/" + databaseName;
    }
    
    public String colPath(String collectionName) {
        return dbPath() + "/_api/collection/" + collectionName;
    }
    
    public Stream<Result.DatabaseCreateResult> databaseCreate() {
        return databaseCreate(null);
    }

    public Stream<Result.DatabaseCreateResult> databaseCreate(List<Result.UserOptions> users) {
        Result.DatabaseCreateOptions options = new Result.DatabaseCreateOptions();
        options.name = databaseName;
        options.users = users;
        Url url = new Url("/_api/database");
        return process(post(url, options, Result.DatabaseCreateResult.class));
    }
    
    public Stream<Result.DatabaseDeleteResult> databaseDelete() {
        Url url = new Url("/_db/_system/_api/database/", databaseName);
        return process(del(url, Result.DatabaseDeleteResult.class));
    }

    /*
     * Index.
     */
    public Stream<Result.CollectionListResult> collectionsList(boolean excludeSystem) {
        Url url = new Url("/_api/collections");
        url.parm("exludeSystem", excludeSystem, false);
        return process(get(url, Result.CollectionListResult.class));
    }
    
    public Stream<Result.CollectionCreateResult> collectionCreate(String collectionName, Result.CollectionCreateOptions options) {
        options.name = collectionName;
        Url url = new Url(dbPath(), "/_api/collection");
        return process(post(url, options, Result.CollectionCreateResult.class));
    }
    
    public Stream<Result.CollectionCreateResult> collectionCreate(String collectionName) {
        return collectionCreate(collectionName, new Result.CollectionCreateOptions());
    }

    public Stream<Result.CollectionDeleteResult> dollectionDelete(String collectionName) {
        Url url = new Url(colPath(collectionName), "/delete/");
        return process(del(url, Result.CollectionDeleteResult.class));
    }

    public Stream<Result.CollectionCountResult> collectionCount(long id) {
        return collectionCount(String.valueOf(id));
    }

    public Stream<Result.CollectionCountResult> collectionCount(String collectionName) {
        Url url = new Url(colPath(collectionName), "/count");
        return process(get(url, Result.CollectionCountResult.class));
    }

    public Stream<Result.CollectionPropertiesResult> collectionProperties(String collectionName) {
        Url url = new Url(colPath(collectionName), "/properties");
        return process(get(url, Result.CollectionPropertiesResult.class));
    }

    public Stream<Result.CollectionPropertiesResult> collectionProperties(String collectionName, boolean waitForSync, long journalSize) {
        Url url = new Url(colPath(collectionName), "/properties");
        url.parm("waitForSync", waitForSync, false);
        url.parm("journalSize", journalSize, 0);
        return process(put(url, null, Result.CollectionPropertiesResult.class));
    }

    public Stream<Result.CollectionFiguresResult> collectionFigures(String collectionName) {
        Url url = new Url(colPath(collectionName), "/figures");
        return process(get(url, Result.CollectionFiguresResult.class));
    }

    public Stream<Result.CollectionLoadResult> collectionLoad(String collectionName, boolean count) {
        Url url = new Url(colPath(collectionName), "/load");
        url.parm("count", count, false);
        return process(put(url, null, Result.CollectionLoadResult.class));
    }

    public Stream<Result.CollectionUnloadResult> collectionUnload(String collectionName) {
        Url url = new Url(colPath(collectionName), "/unload");
        return process(put(url, null, Result.CollectionUnloadResult.class));
    }

    public Stream<Result.CollectionTruncateResult> collectionTruncate(String collectionName) {
        Url url = new Url(colPath(collectionName), "/unload");
        return process(put(url, null, Result.CollectionTruncateResult.class));
    }

    public Stream<Result.CollectionRenameResult> collectionRename(String collectionName, String newCollectionName) {
        Url url = new Url(colPath(collectionName), "/rename");
        url.parm("name", newCollectionName);
        return process(put(url, null, Result.CollectionRenameResult.class));
    }


    /*
     * Index.
     */
    public Stream<Result.IndexList> indexesList(String collectionName) {
        Url url = new Url(dbPath(), "/_api/index");
        url.parm("collection", collectionName);
        return process(get(url, Result.IndexList.class));
    }


    public Stream<Result.IndexInfo> indexInfo(String id) {
        Url url = new Url(dbPath(), "/_api/index/" + id);
        return process(get(url, Result.IndexInfo.class));
    }
/*
    public Promise<Result.IndexInfo> indexAdd(String type, JsonObject parms) {
        parms.putString("type", type);
        return new Request("POST", database.getPath() + "/_api/index/", parms);
    }
*/
    public Stream<Result.IndexDelete> deleteIndex(String id) {
        Url url = new Url(dbPath(), "/_api/index/", id);
        return process(del(url, Result.IndexDelete.class));
    }

  
    /*
     * Document
     */
  
    public Stream<Result.Document> documentLoad(String id) {
        Url url = new Url(dbPath(), "/_api/document/", id);
        return process(get(url, Result.Document.class));
    }
    
    public Stream<Result.DocumentCreate> documentCreate(String collectionName, boolean createCollection, boolean waitForSync, Object doc) {
        Url url = new Url(dbPath(), "/_api/document");
        url.parm("collection", collectionName);
        url.parm("createCollection", createCollection, false);
        url.parm("waitForSync", waitForSync, false);
        return process(post(url, doc, Result.DocumentCreate.class));
    }

    public Stream<Result.DocumentCreate> documentReplace(String id, String rev, boolean waitForSync, String policy, Object doc) {
        Url url = new Url(dbPath(), "/_api/document/", id);
        url.parm("waitForSync", waitForSync, false);
        url.parm("rev", rev);
        url.parm("policy", policy);
        return process(put(url, doc, Result.DocumentCreate.class));
    }

    public Stream<Result.DocumentCreate> documentUpdate(String id, String rev, boolean keepNull, boolean waitForSync, String policy, Object doc) {
        Url url = new Url(dbPath(), "/_api/document/", id);
        url.parm("keepNull", keepNull, false);
        url.parm("waitForSync", waitForSync, false);
        url.parm("rev", rev);
        url.parm("policy", policy);
        return process(patch(url, doc, Result.DocumentCreate.class));
    }

    public Stream<Result.DocumentCreate> documentDelete(String id, String rev, boolean waitForSync, String policy) {
        Url url = new Url(dbPath(), "/_api/document/", id);
        url.parm("waitForSync", waitForSync, false);
        url.parm("rev", rev);
        url.parm("policy", policy);

        return process(del(url, Result.DocumentCreate.class));
    }

    public Stream<Result.CursorResult> cursor(String query, Map<String, Object> bindVars) {
        Url url = new Url(dbPath(), "/_api/cursor");
        Result.Query q = new Result.Query();
        q.query = query;
        q.bindVars = bindVars;
        q.batchSize = 500;
        return process(post(url, q, Result.CursorResult.class));
    }
    /*
     * Gharial
     */
    public Stream<Result.GharialCreateResult> gharialCreate(Result.GharialCreateOption options) {
        Url url = new Url(dbPath(), "/_api/gharial/");
        return process(post(url, options, Result.GharialCreateResult.class));
    }
    
    public Stream<Result.GharialEdgeCreateResult> gharialEdgeCreate(String graph, String collection, String from, String to) {
        Url url = new Url(dbPath(), "/_api/gharial/" + graph + "/edge/" + collection);
        Result.EdgeBody edge = new Result.EdgeBody();
        edge._from = from;
        edge._to = to;
        
        return process(post(url, edge, Result.GharialEdgeCreateResult.class));
    }

    static class Url {
        StringBuilder sb = new StringBuilder();
        boolean seenParms = false;
        
        public Url(String prefix) {
            sb.append(prefix);
        }
        
        public Url(String prefix, String postfix) {
            sb.append(prefix);
            sb.append(postfix);
        }
        
        public Url(String prefix, String postfix, String handle) {
            sb.append(prefix);
            sb.append(postfix);
            sb.append(handle);
        }
        
        public void parm(String name, String value) {
            sb.append(seenParms ? '&' : '?');
            sb.append(name);
            sb.append('=');
            sb.append(value);
            seenParms = true;
        }

        public void parm(String name, boolean value) {
            sb.append(seenParms ? '&' : '?');
            sb.append(name);
            sb.append(value ? "=true" : "=false");
            seenParms = true;
        }

        public void parm(String name, boolean value, boolean dflt) {
            if (value == dflt) {
                return;
            }
            sb.append(seenParms ? '&' : '?');
            sb.append(name);
            sb.append(value ? "=true" : "=false");
            seenParms = true;
        }
        
        public void parm(String name, long value, long dflt) {
            if (value == dflt) {
                return;
            }
            sb.append(seenParms ? '&' : '?');
            sb.append(name);
            sb.append(Long.toString(value));
            seenParms = true;
        }

        public String toString() {
            return sb.toString();
        }
    }
    
    public Stream<Void> close() {
        return client.close();
    }

    <T extends Result> Stream<T> process(Operation<T> op) {
        return client.process(op);
    }
    
    private <T extends Result> Operation<T> get(Url url, Class<T> resClass) {
        return new Operation<>("GET", url.toString(), null, resClass);
    }
    
    private <T extends Result> Operation<T> put(Url url, Object body, Class<T> resClass) {
        return new Operation<>("PUT", url.toString(), body, resClass);
    }
    
    private <T extends Result> Operation<T> del(Url url, Class<T> resClass) {
        return new Operation<>("DELETE", url.toString(), null, resClass);
    }
    
    private <T extends Result> Operation<T> post(Url url, Object body, Class<T> resClass) {
        return new Operation<>("POST", url.toString(), body, resClass);
    }
    
    private <T extends Result> Operation<T> patch(Url url, Object body, Class<T> resClass) {
        return new Operation<>("PATCH", url.toString(), body, resClass);
    }
}
