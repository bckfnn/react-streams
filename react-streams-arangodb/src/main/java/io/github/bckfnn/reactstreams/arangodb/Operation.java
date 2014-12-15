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
