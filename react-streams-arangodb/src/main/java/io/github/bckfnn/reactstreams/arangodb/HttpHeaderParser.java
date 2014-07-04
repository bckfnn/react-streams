package io.github.bckfnn.reactstreams.arangodb;



/**
 * This code is a rewrite of Matthew Endsley tinyhhtp:
 * http://mendsley.github.io/2012/12/19/tinyhttp.html
 */
public class HttpHeaderParser {
    
    /** Action. */
    static final int DONE = 1;
    static final int CONT = 2;
    static final int CHAR = 3;
    static final int CODE = 4;
    static final int ERR = 5;
    
    static final int VERS = 6;
    static final int STAT = 7;
    static final int KEY = 8;
    static final int VAL = 9;


    public int states[] = {
            /*        *          sp      \n        \r        ,           :    */
            /*0 */ 0, CHAR,  1, VERS,  1, ERR,   1, ERR,   0, CHAR,  0, CHAR, // HTTP version 
            /*1 */ 1, CODE,  2, CONT,  1, ERR,   1, ERR,   1, CONT,  1, CONT, // Response code    
            /*2 */ 2, CHAR,  2, CHAR,  4, STAT,  3, CONT,  2, CHAR,  2, CHAR, // Response reason
            /*3 */ 1, ERR,   1, ERR,   4, STAT,  1, ERR,   1, ERR,   1, ERR,  // HTTP version newline     
            /*4 */ 4, CHAR,  1, ERR,   0, ERR,   5, CONT,  1, ERR,   6, KEY,  // Start of header field
            /*5 */ 1, ERR,   1, ERR,   0, DONE,  1, ERR,   1, ERR,   1, ERR,  // Last CR before end of header
            /*6 */ 7, CHAR,  6, CONT,  1, DONE,  1, ERR,   7, CHAR,  7, CHAR, // leading whitespace before header value
            /*7 */ 7, CHAR,  7, CHAR,  4, VAL,   10,CONT,  8, CHAR,  7, CHAR, // header field value
            /*8 */ 7, CHAR,  8, CHAR,  6, CONT,  9, CONT,  8, CHAR,  7, CHAR, // Split value field value
            /*9 */ 1, ERR,   1, ERR,   6, CONT,  1, ERR,   1, ERR,   1, ERR,  // CR after split value field
            /*10*/ 1, ERR,   1, ERR,   4, VAL,   1, ERR,   1, ERR,   1, ERR,  // CR after header value
    };

    /**
     * Characters classes. 
     */
    public byte[] clas = new byte[256];
    {
        clas[(byte) '\t'] = 1;
        clas[(byte) ' '] = 1;
        clas[(byte) '\n'] = 2;
        clas[(byte) '\r'] = 3;
        clas[(byte) ','] = 4;
        clas[(byte) ':'] = 5;
    }

    private int state;
    private int status;

    final private int state(byte ch) {
        int code = clas[ch]; 

        int idx = ((state * 6) + code) << 1;
        state = states[idx];
        return states[idx+1];
    }

    int l;
    final public int parse(final byte[] input, int i) {
        l = input.length;
        while (i < l) {
            byte ch = input[i];

            int s = state(ch);
            //System.out.println("s:" + s + " " + state + " " + new String(buf, 0, buflen) + " pos:" + i + " " + ch);
            switch (s) {
            case DONE:
                onDone();
                return i + 1;
            case CHAR:
            	addBuf(ch);
                break;
            case CONT:
                break;
            case CODE:
            	status = status * 10 + (ch - 48);
                break;
            case ERR:
                onError();
                return i + 1;
            default:
                msg(s);
                break;
            }
            i++;
        }
        return l;
    }


    final private void msg(int type) {
    	String v = new String(buf, 0, buflen);
        switch (type) {
        case VERS:
            onVersion(v);
            break;
        case STAT:
            onStatus(status, v);
            status = 0;
            break;
        case KEY:
        	onKey(v);
            break;
        case VAL:
            onValue(v);
            break;
        }
        buflen = 0;
    }
    
    int blen = 64;
    char[] buf = new char[blen];

    int buflen = 0;

    final private void addBuf(byte v) {
    	if (buflen == blen) {
    		blen = buflen << 1;
    		char[] b = new char[blen];
    		System.arraycopy(buf, 0, b, 0, buflen);
    		buf = b;
    	}
        buf[buflen++] = (char) v;
    }

    public void onVersion(String buf) {
    }

    public void onStatus(int status, String buf) {
    }
    
    public void onKey(String buf) {
    }

    public void onValue(String buf) {
    }
    
    public void onDone() { }

    public void onError() { }
    
    public static void main(String[] args) {
        final String t = 
                "HTTP/1.1 202 OK\r\n"
                        + "Server:ArangoDB\r\n"
                        + "Connection: Keep-Alive\r\n"
                        + "Content-Type: application/json; charset=utf-8\r\n"
                        + "Content-Length: 81\r\n"
                        + "\r\n";

        final byte[] b = t.getBytes();
        final HttpHeaderParser p = new HttpHeaderParser() {
            String skey;

            @Override
            public void onVersion(String buf) {
                System.out.println("version: " + buf);
            }

            @Override
            public void onStatus(int status, String buf) {
                System.out.println("status: " + status + " " + buf);
            }

            @Override
            public void onKey(String buf) {
                skey = buf;
            }

            @Override
            public void onValue(String buf) {
                System.out.println("header: " + skey + " = " + buf);
            }
        };
        p.parse(b, 0);

        final HttpHeaderParser p2 = new HttpHeaderParser() {
            //String skey;

            @Override
            public void onVersion(String buf) {
            }

            @Override
            public void onStatus(int status, String buf) {
            }

            @Override
            public void onKey(String buf) {
            }

            @Override
            public void onValue(String buf) {
            }
        };

        int cnt = 3000000;
        p2.speedTest(10, cnt, new Runnable() {
            @Override
            public void run() {
                p2.parse(b, 0);
            }
        });


    }

    public boolean match(String m, byte[] buf, int len) {
        int l = m.length();
        if (l == len) {
            for (int i = 0; i < l; i++) {
                byte c = buf[i];
                if (c >= 'A' && c <= 'Z') {
                    c += ' ';
                }
                if (m.charAt(i) != c) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void speedTest(int iterations, int count, Runnable r) {
        for (int i = 0; i < 100000; i++) {
            r.run();
        }

        long sum = 0;
        for (int j = 0; j < iterations; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                r.run();
            }
            long time = System.currentTimeMillis() - start;
            System.out.println(time);
            sum += time;
        }
        System.out.println("Done " + (sum / iterations));
    }
}
