package com.example.myapplication;

import android.util.Log;

public class Bytes {

    public static byte[] addIdentifier(byte id, byte[] msg) {
        byte[] newArray = new byte[msg.length + 1];
        newArray[0] = id;

        for(int i=1; i < newArray.length; i++){
            newArray[i] = msg[i-1];
        }
        return newArray;
    }

    public static byte[] removeIdentifier(byte[] msg) {
        byte[] newArray = new byte[msg.length - 1];
        for(int i=1; i < msg.length; i++){
            newArray[i-1] = msg[i];
        }
        return newArray;
    }

    public static byte[] intToByteArray(int a)
    {
        byte[] ret = new byte[4];
        ret[3] = (byte) (a & 0xFF);
        ret[2] = (byte) ((a >> 8) & 0xFF);
        ret[1] = (byte) ((a >> 16) & 0xFF);
        ret[0] = (byte) ((a >> 24) & 0xFF);
        return ret;
    }

    public static int byteArrayToInt(byte[] b)
    {
        return (b[3] & 0xFF) + ((b[2] & 0xFF) << 8) + ((b[1] & 0xFF) << 16) + ((b[0] & 0xFF) << 24);
    }

    public static byte[] addToArrayEnd(byte[] front, byte[] back, int lengthOfBack) {

        if(front == null) {
            return trimArrayFromEnd(back, lengthOfBack);
        }
        return concatenateArrays(front, trimArrayFromEnd(back, lengthOfBack));
    }

    public static byte[] trimArrayFromEnd(byte[] array, int size) {
        if(array.length == size) {
            return array;
        }

        byte[] newArray = new byte[size];
        for(int i=0; i < size; i++) {
            newArray[i] = array[i];
        }
        return newArray;
    }

    public static byte[] concatenateArrays(byte[] front, byte[] back) {
        byte[] newArray = new byte[front.length + back.length];
        for(int i=0; i < front.length; i++) {
            newArray[i] = front[i];
        }
        for(int i=front.length; i < newArray.length; i++) {
            newArray[i] = back[i - front.length];
        }
        return newArray;
    }

    public static void addToArray(byte[] arrayToAdd, byte[] arrayToBeAdded, int firstIndex) {
        for(int i=firstIndex; i < arrayToBeAdded.length + firstIndex; i++) {
            arrayToAdd[i] = arrayToBeAdded[i - firstIndex];
        }
    }
}
