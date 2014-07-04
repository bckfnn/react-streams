package io.github.bckfnn.reactstreams.arangodb.test;

import io.github.bckfnn.reactstreams.arangodb.MultipartParser;

import org.junit.Test;

public class MultipartTest {

    @Test
    public void test1() {
        MultipartParser p = new MultipartParser("--AaB03x");

        String txt = 
                "--AaB03x\r\n" +
                        "content-disposition: form-data; name=\"field1\"\r\n"+
                        "\r\n"+
                        "Joe Blow\r\nalmost tricked you!\r\n"+
                        "--AaB03x\r\n"+
                        "content-disposition: form-data; name=\"pics\"; filename=\"file1.txt\"\r\n"+
                        "Content-Type: text/plain\r\n"+
                        "\r\n"+
                        "... contents of file1.txt ...\r\r\n"+
                        "--AaB03x--\r\n";

        p.execute(txt.getBytes(), txt.getBytes().length);
    }
}
