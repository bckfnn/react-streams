package io.github.bckfnn.reactstreams.arangodb;


import io.github.bckfnn.reactstreams.Stream;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AsyncHttpClient implements HttpDriver {
    private static Logger LOG = LoggerFactory.getLogger(Client.class);

    private String host;
    private int port;
    CloseableHttpAsyncClient httpClient;
    private ObjectMapper mapper = new ObjectMapper();
    
    public AsyncHttpClient() {
        httpClient = HttpAsyncClients.createDefault();
    }

    @Override
    public <T extends Result> Stream<T> process(Operation<T> req) {
        LOG.debug("req -> " + req.getUri());
        return Stream.asOne(subscription -> {
            httpClient.execute(new HttpHost(host, port), new HttpGet(new URI(req.getUri())), new FutureCallback<HttpResponse>() {
                public void completed(HttpResponse result) {
                    LOG.debug("res <- " + result.getStatusLine().getStatusCode());
                    HttpEntity ent = result.getEntity();
                    try {
                        T val = mapper.readValue(ent.getContent(), req.getResponseClass());
                        subscription.sendNext(val);
                        subscription.sendComplete();
                    } catch (Exception e) {
                        subscription.sendError(e);
                    }
                }

                public void failed(Exception ex) {
                    subscription.sendError(ex);
                }

                public void cancelled() {
                    subscription.sendError(new Throwable("cancelled"));
                }
            });
        });
    }

    @Override
    public Stream<Boolean> init(String host, int port) {
        this.host = host;
        this.port = port;
        return Stream.from();
    }

    @Override
    public Stream<Void> close() {
        return Stream.asOne(subscription -> {
            try {
                httpClient.close();
                subscription.sendComplete();
            } catch (IOException e) {
                subscription.sendError(e);
            }
        });
    }
}