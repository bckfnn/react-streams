package io.github.bckfnn.reactstreams.arangodb;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class KeepAliveTest {
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 8; i++) {
            Thread t = new T();
            t.setDaemon(false);
            t.start();
        }
    }

    public static void get()  {
        final Random random = ThreadLocalRandom.current();

        final int id = random.nextInt(10000) + 1;
        try {
            URL url = new URL("http://localhost:8529/_db/techempower/_api/document/World/" + id);
            HttpURLConnection httpc = (HttpURLConnection) url.openConnection();

            int l = httpc.getContentLength();

            byte[] buf = new byte[l];
            InputStream istr = httpc.getInputStream();
            istr.read(buf);
            istr.close();
            httpc.disconnect();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
    
    static class T extends Thread {
        @Override
        public void run() {
            for (int i = 0; i < 40000; i++) {
                long now = System.currentTimeMillis();
                get();
                long time = System.currentTimeMillis() - now;
                if (time > 100) {
                    System.out.println("request took " + time + " on thread " + Thread.currentThread());
                }
            }
            System.out.println("done " + Thread.currentThread());
        }
    }
}
