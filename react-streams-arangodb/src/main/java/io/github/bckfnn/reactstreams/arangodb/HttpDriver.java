package io.github.bckfnn.reactstreams.arangodb;

import io.github.bckfnn.reactstreams.Stream;

public interface HttpDriver {
    public Stream<Boolean> init(String host, int port);
    public <T extends Result> Stream<T> process(Operation<T> req);
    public Stream<Void> close();
}
