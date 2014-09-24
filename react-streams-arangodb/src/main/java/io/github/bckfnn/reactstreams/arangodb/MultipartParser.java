package io.github.bckfnn.reactstreams.arangodb;

/**
 * https://github.com/iafonov/multipart-parser-c
 *
 */
public class MultipartParser {

    final static int s_uninitialized = 1;

    final static int s_start = 2;
    final static int s_start_boundary = 3;
    final static int s_header_field_start = 4;
    final static int s_header_field = 5;
    final static int s_headers_almost_done = 6;
    final static int s_header_value_start = 7;
    final static int s_header_value = 8;
    final static int s_header_value_almost_done = 9;
    final static int s_part_data_start = 10;
    final static int s_part_data = 11;
    final static int s_part_data_almost_boundary = 12;
    final static int s_part_data_boundary = 13;
    final static int s_part_data_almost_end = 14;
    final static int s_part_data_end = 15;
    final static int s_part_data_final_hyphen = 16;
    final static int s_end  = 17;

    final byte CR = '\r';
    final byte LF = '\n';

    int state;
    String boundary;
    int index;
    byte[] lookbehind = null;

    public MultipartParser(String boundary) {
        state = s_start;
        index = 0;
        this.boundary = boundary;
        lookbehind = new byte[boundary.length() + 9];
    }

    public int execute(byte[] buf, int len) {
        int i = 0;
        int mark = 0;
        while (i < len) {
            byte c = buf[i];
            boolean is_last = (i == len - 1);

            switch (state) {
            case s_start:
                log("s_start");
                index = 0;
                state = s_start_boundary;
                /* fallthrough */

            case s_start_boundary:
                log("s_start_boundary");
                if (index == boundary.length()) {
                    if (c != CR) {
                        return i;
                    }
                    index++;
                    break;
                } else if (index == boundary.length() + 1) {
                    if (c != LF) {
                        return i;
                    }
                    index = 0;
                    part_data_begin();
                    state = s_header_field_start;
                    break;
                }
                if (c != boundary.charAt(index)) {
                    return i;
                }
                index++;
                break;

            case s_header_field_start:
                log("s_header_field_start");
                mark = i;
                state = s_header_field;

                /* fallthrough */
            case s_header_field:
                log("s_header_field");
                if (c == CR) {
                    state = s_headers_almost_done;
                    break;
                }

                if (c == '-') {
                    break;
                }

                if (c == ':') {
                    data_header_field(buf, mark, i - mark);
                    state = s_header_value_start;
                    break;
                }

                byte cl = tolower(c);
                if (cl < 'a' || cl > 'z') {
                    log("invalid character in header name");
                    return i;
                }
                if (is_last)
                    data_header_field(buf, mark, i - mark + 1);
                break;

            case s_headers_almost_done:
                log("s_headers_almost_done");
                if (c != LF) {
                    return i;
                }

                state = s_part_data_start;
                break;

            case s_header_value_start:
                log("s_header_value_start");
                if (c == ' ') {
                    break;
                }

                mark = i;
                state = s_header_value;

                /* fallthrough */
            case s_header_value:
                log("s_header_value");
                if (c == CR) {
                    data_header_value(buf, mark, i - mark);
                    state = s_header_value_almost_done;
                }
                if (is_last)
                    data_header_value(buf, mark, i - mark + 1);
                break;

            case s_header_value_almost_done:
                log("s_header_value_almost_done");
                if (c != LF) {
                    return i;
                }
                state = s_header_field_start;
                break;

            case s_part_data_start:
                log("s_part_data_start");
                headers_complete();
                mark = i;
                state = s_part_data;


                /* fallthrough */
            case s_part_data:
                log("s_part_data");
                if (c == CR) {
                    data_part_data(buf, mark, i - mark);
                    mark = i;
                    state = s_part_data_almost_boundary;
                    lookbehind[0] = CR;
                    break;
                }
                if (is_last) {
                    data_part_data(buf, mark, i - mark + 1);
                }
                break;
            case s_part_data_almost_boundary:
                log("s_part_data_almost_boundary");
                if (c == LF) {
                    state = s_part_data_boundary;
                    lookbehind[1] = LF;
                    index = 0;
                    break;
                }
                data_part_data(lookbehind, 0, 1);
                state = s_part_data;
                mark = i --;
                break;

            case s_part_data_boundary:
                log("s_part_data_boundary");
                if (boundary.charAt(index) != c) {
                    data_part_data(lookbehind, 0, 2 + index);
                    state = s_part_data;
                    mark = i --;
                    break;
                }
                lookbehind[2 + index] = c;
                if (++index == boundary.length()) {
                    part_data_end();
                    state = s_part_data_almost_end;
                }
                break;

            case s_part_data_almost_end:
                log("s_part_data_almost_end");
                if (c == '-') {
                    state = s_part_data_final_hyphen;
                    break;
                }
                if (c == CR) {
                    state = s_part_data_end;
                    break;
                }
                return i;

            case s_part_data_final_hyphen:
                log("s_part_data_final_hyphen");
                if (c == '-') {
                    body_end();
                    state = s_end;
                    break;
                }
                return i;

            case s_part_data_end:
                log("s_part_data_end");
                if (c == LF) {
                    state = s_header_field_start;
                    part_data_begin();
                    break;
                }
                return i;

            case s_end:
                log("s_end: 0x" + Integer.toString(c, 16));
                break;

            default:
                log("Multipart parser unrecoverable error");
                return 0;
            }
            i++;
        }
        return len;
    }
    
    public void part_data_begin() {
        System.out.println("  data_begin");
    }

    public void headers_complete() {
        System.out.println("  headers_complete");
    }

    public void data_header_field(byte[] buf, int start, int len) {
        System.out.println("  headers_field " + new String(buf, start, len));
    }

    public void data_header_value(byte[] buf, int start, int len) {
        System.out.println("  headers_value " + new String(buf, start, len));

    }

    public void data_part_data(byte[] buf, int start, int len) {
        System.out.println("  part_data " + new String(buf, start, len));

    }

    public void body_end() {
        System.out.println("  body_end");

    }
    
    public void part_data_end() {
        System.out.println("  data_end");
        
    }
    
    public byte tolower(byte c) {
        if (c >= 'A' && c <= 'Z') {
            c += ' ';
        }
        return c;
    }
    
    public void log(String msg) {
        //System.out.println(msg);
    }


}
