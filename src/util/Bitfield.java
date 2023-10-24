package util;

import java.util.Arrays;

public class Bitfield {
    private byte[] bitfield;

    public Bitfield(byte[] bitfield) {
        this.bitfield = bitfield;
    }

    public Bitfield(int numPieces, boolean hasFile) {
        int tempLength = numPieces / 8;
		int length = (tempLength * 8 == numPieces) ? tempLength : (tempLength + 1);
		bitfield = new byte[length];
		byte fillValueByte = hasFile ? (byte) (0b11111111)
				: (byte) (0b00000000);
		Arrays.fill(bitfield, fillValueByte);
    }

    public byte[] getBitfield() {
        return bitfield;
    }

    public int getLength() {
        return bitfield.length;
    }

    // Change index of bit function
}
