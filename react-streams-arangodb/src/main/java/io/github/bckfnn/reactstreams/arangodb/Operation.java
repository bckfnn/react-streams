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

public class Operation<T extends Result> {
    private final String method;
    private String uri;
    private Object body;
    private Class<T> responseClass;

    public Operation(String method) {
        this.method = method;
    }

    public Operation(String method, String uri, Object body, Class<T> responseClass) {
        this.method = method;
        this.uri = uri;
        this.body = body;
        this.responseClass = responseClass;
    }

 
    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Class<T> getResponseClass() {
        return responseClass;
    }
}
