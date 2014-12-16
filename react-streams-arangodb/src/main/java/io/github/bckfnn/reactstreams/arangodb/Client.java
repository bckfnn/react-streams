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

public class Client {

    private HttpDriver httpDriver;

    public Client(HttpDriver httpDriver) {
        this.httpDriver = httpDriver;
    }

    /**
     * Connect the server using default host and port.
     * @return a publisher with the result of the connect.
     */
    public Stream<Boolean> init() {
        return init("localhost", 8529);
    }

    /**
     * Connect the server using default host and port.
     * @param host the host string.
     * @param port the port number.
     * @return a publisher of the connection result.
     */
    public Stream<Boolean> init(final String host, final int port) {
        return httpDriver.init(host, port);
    }

    public Database getDatabase(String name) {
        return new Database(this, name);
    }

    public Stream<Void> close() {
        return httpDriver.close();
    }

    public Stream<Result.DatabaseList> databasesList() {
        return process(new Operation<>("GET", "/_api/database", null, Result.DatabaseList.class));
    }

    public Stream<Result.VersionResult> version(boolean details) {
        return process(new Operation<>("GET", "/_api/version?details=" + details, null, Result.VersionResult.class));
    }
    
    public <T extends Result> Stream<T> process(Operation<T> req)  {
        return httpDriver.process(req);
    }
}
