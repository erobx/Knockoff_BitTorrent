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
        this.bitfield = new byte[length];
        byte fillValueByte = hasFile ? (byte) (0b11111111) : (byte) (0b00000000);
        Arrays.fill(this.bitfield, fillValueByte);
    }

    public byte[] getBitfield() {
        return bitfield;
    }

    public int getLength() {
        return bitfield.length;
    }

    public boolean isEmpty() {
        for (byte b : bitfield) {
            if (b != 0) {
                return false; // The bitfield is not empty
            }
        }
        return true; // The bitfield is empty
    }

    public boolean isFull() {
        for (byte b : bitfield) {
            if (b == 0) {
                return false; // The bitfield is not empty
            }
        }
        return true; // The bitfield is empty
    }

    /*
     * public boolean hasPiece(int pieceIndex) {
     * int index = pieceIndex / 8;
     * int offset = pieceIndex % 8;
     * int andResult = bitfield[index] & (1 << offset);
     * return (andResult != 0);
     * }
     * 
     * public void setPiece(int pieceIndex, boolean havePiece) {
     * int index = pieceIndex / 8;
     * int offset = pieceIndex % 8;
     * byte temp = (byte) (1 << offset);
     * if (havePiece) {
     * bitfield[index] |= temp;
     * } else {
     * temp = (byte) (~temp);
     * bitfield[index] &= temp;
     * }
     * }
     */

    public boolean hasPiece(int pieceIndex) {
        if (pieceIndex < 0 || pieceIndex >= bitfield.length * 8) {
            throw new IllegalArgumentException("Invalid index");
        }

        int byteIndex = pieceIndex / 8;
        int bitIndex = pieceIndex % 8;

        // Check if the corresponding bit is set
        return (bitfield[byteIndex] & (1 << (7 - bitIndex))) != 0;
    }

    public void setPiece(int index, boolean value) {
        if (index < 0 || index >= bitfield.length * 8) {
            throw new IllegalArgumentException("Invalid index");
        }

        int byteIndex = index / 8;
        int bitIndex = index % 8;

        // Set or clear the corresponding bit based on the boolean value
        if (value) {
            bitfield[byteIndex] |= (1 << (7 - bitIndex)); // Set the bit to 1
        } else {
            bitfield[byteIndex] &= ~(1 << (7 - bitIndex)); // Clear the bit to 0
        }
    }

    // Change index of bit function
}
