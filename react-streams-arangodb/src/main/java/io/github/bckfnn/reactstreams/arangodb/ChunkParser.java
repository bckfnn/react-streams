package io.github.bckfnn.reactstreams.arangodb;

public class ChunkParser {
	
	int[] stateTable = {
		/*     *     LF    CR    HEX */
		/* 0*/ 0xC1, 0xC1, 0xC1, 1,    /* s0: initial hex char */
		/* 1*/ 0xC1, 0xC1, 2,    0x81, /* s1: additional hex chars, followed by CR */
		/* 2*/ 0xC1, 0x83, 0xC1, 0xC1, /* s2: trailing LF */
		/* 3*/ 0xC1, 0xC1, 4,    0xC1, /* s3: CR after chunk block */
		/* 4*/ 0xC1, 0xC0, 0xC1, 0xC1, /* s4: LF after chunk block */
	};
	
	byte[] charclass = new byte[256];
	byte[] hexlookup = new byte[256];
	{
		for (char ch : "0123456789abcdefABCDEF".toCharArray()) {
			charclass[ch] = 3;
			hexlookup[ch] = (byte) Character.digit(ch, 16);
		}
		charclass['\r'] = 2;
		charclass['\n'] = 1;
	}


	private int state;
	int size;

	public int parse(byte[] input, int i) {
		int l = input.length;
		while (i < l) {
			byte ch = input[i];
			int code = charclass[ch];

			int newstate = stateTable[state * 4 + code];
			state = newstate & 0xF;
			switch (newstate) {
			case 0xC0:
				onDone();
				return i + 1;

			case 0xC1: /* error */
				onError();
				return i + 1;

			case 0x01: /* initial char */
				size = 0;
				/* fallthrough */
			case 0x81: /* size char */
				size = (size << 4) + hexlookup[ch];
				break;

			case 0x83:
				onLength(size);
				return i + 1;
			}
			i++;
		}
		return i;
	}

	public void onLength(int len) {

	}
	
	public void onError() {
		
	}
	
	public void onDone() {
		
	}
}

