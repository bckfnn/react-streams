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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Result {
    public int code;
    public boolean error;
    public String errorMessage;
    public int errorNum;

    public void setHttpStatus(int status) {

    }

    public void addHttpHeader(String name, String value) {

    }
    
    public String toString() {
        ObjectMapper map = new ObjectMapper();
        try {
            return map.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "error";
        }
    }

    public static class VersionResult extends Result {
        public String server;
        public String version;
        public VersionDetail details;
    }

    // @JsonIgnoreProperties(ignoreUnknown=true)
    // @JsonInclude(Include.NON_NULL)
    public static class VersionDetail {
        public String architecture;
        @JsonProperty("build-date")
        public String buildDate;
        public String configure;
        @JsonProperty("icu-version")
        public String icuVersion;
        @JsonProperty("libev-version")
        public String libevVersion;
        @JsonProperty("maintainer-mode")
        public String maintainerMode;
        @JsonProperty("openssl-version")
        public String opensslVersion;
        @JsonProperty("repository-version")
        public String repositoryVersion;
        @JsonProperty("server-version")
        public String serverVersion;
        @JsonProperty("sizeof int")
        public String sizeofInt;
        @JsonProperty("sizeof void*")
        public String sizeofVoid;
        @JsonProperty("v8-version")
        public String v8Version;
        @JsonProperty("zlib-version")
        public String zlibVersion;
    }
    
    public static class DatabaseList extends Result {
        public List<String> result;
    }

    
    public static class DatabaseCreateOptions {
        public String name;
        public List<UserOptions> users;
    }
    
    public static class UserOptions {
        public String username;
        public String passwd;
        public boolean active;
        public Map<String, Object> extra;
    }
    
    public static class DatabaseCreateResult extends Result {
        public boolean result;
    }
    
    public static class DatabaseDeleteResult extends Result {
        public boolean result;
    }
    
    static class CollectionInfoResult extends Result {
        public String id;
        public String name;
        public int status;
        public int type;
    }


    static class CollectionCreateOptions {
        public String name;
        public boolean waitForSync;
        public boolean doCompact;
        public int journalSize;
    }
    
    static class CollectionCreateResult extends Result {
        public String id;
        public String name;
        public boolean waitForSync; 
        public boolean isVolatile; 
        public boolean isSystem;
        public int status;
        public int type;
    }
    
    public static class CollectionDeleteResult extends Result {
        public String id;
    }
    
    public static class CollectionListResult extends Result {
        public List<CollectionResult> collections;
        public Map<String, CollectionResult> names;
    }

    static class CollectionResult extends CollectionInfoResult {
        public boolean isSystem;
    }
    
    static class CollectionPropertiesResult extends CollectionResult {
        public boolean waitForSync; 
        public boolean doCompact; 
        public long journalSize;
        public boolean isVolatile; 
        public int numberOfShards;
        public String shardKeys;
    }
    
    static class CollectionCountResult extends CollectionPropertiesResult {
        public long count;
    }
    
    static class CollectionFiguresResult extends CollectionCountResult {
        public long count;
        public CollectionFiguresInfo figures;
    }
    
    static class CollectionFiguresInfo {
        public Map<String, Object> alive;
        public Map<String, Object> dead;
        public Map<String, Object> datafiles;
        public Map<String, Object> journals;
        public Map<String, Object> compactors;
        public Map<String, Object> shapefiles;
        public Map<String, Object> shapes;
        public Map<String, Object> attributes;
        public Map<String, Object> indexes;
        public String lastTick;
        public long uncollectedLogfileEntries;
    }
    
    static class CollectionLoadResult extends CollectionResult {
        public long count;
    }
    
    static class CollectionUnloadResult extends CollectionInfoResult {
    }
    

    static class CollectionTruncateResult extends CollectionResult {
    }
    

    public static class CollectionRenameResult extends CollectionInfoResult {
    }
    
    public static class IndexInfo extends Result {
        public String id;
        public String type;
        public boolean unique;
        public List<String> fields;
    }
    
    public static class IndexList extends Result {
        public List<IndexInfo> indexes;
        public Map<String, IndexInfo> identifiers;
    }
    
    public static class IndexDelete extends Result {
        public String id;
    }
    
    public static class Document extends Result {
        public String _id;
        public String _key;
        public String _rev;
        public Map<String, Object> doc;
        
        @JsonAnySetter
        public void anySetter(String key, Object value) {
            if (doc == null) {
                doc = new HashMap<>();
            }
            doc.put(key, value);
        }
    }
    
    public static class DocumentCreate extends Result {
        public String _id;
        public String _key;
        public String _rev;
    }

    public static class Query {
        public String query;
        public boolean count;
        public long batchSize;
        public Map<String, Object> bindVars;
        
    }
    
    public static class CursorResult extends Result {
        public boolean hasMore;
        public long count;
        public List<Document> result;
        public Map<String, Object> extra;
    }
    
    public static class GharialGraph {
        public String name;
        public List<Edge> edgeDefinitions;
        public List<String> orphanCollections;
    }

    public static class GharialCreateOption extends GharialGraph {
    }

    public static class GharialCreateResult extends Result {
        public GharialGraphResult graph;
    }
    
    public static class GharialGraphResult {
        public String _id;
        public String _rev;
        public String name;
        public List<Edge> edgeDefinitions;
        public List<String> orphanCollections;
    }

    public static class Edge {
        public String collection;
        public List<String> from;
        public List<String> to;
    }
    
    public static class GharialEdgeCreateResult extends Result {
        public DocumentCreate edge;
    }
    
    public static class EdgeBody {
        public String _from;
        public String _to;
    }
}


