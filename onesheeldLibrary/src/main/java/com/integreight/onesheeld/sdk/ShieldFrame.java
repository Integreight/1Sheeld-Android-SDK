package com.integreight.onesheeld.sdk;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShieldFrame {
    static final byte START_OF_FRAME = (byte) 0xFF;
    static final byte END_OF_FRAME = (byte) 0x00;

    private byte shieldId;
    private byte instanceId;
    private byte functionId;
    private ArrayList<byte[]> arguments;

    public ShieldFrame(byte shieldId, byte instanceId, byte functionId) {
        this.shieldId = shieldId;
        this.instanceId = instanceId;
        this.functionId = functionId;
        arguments = new ArrayList<byte[]>();
    }

    public ShieldFrame(byte shieldId, byte functionId) {
        this.shieldId = shieldId;
        this.instanceId = 0;
        this.functionId = functionId;
        arguments = new ArrayList<byte[]>();
    }

    public ShieldFrame(byte shieldId) {
        this.shieldId = shieldId;
        this.instanceId = 0;
        this.functionId = 0;
        arguments = new ArrayList<byte[]>();
    }

    public byte getShieldId() {
        return shieldId;
    }

    public byte getInstanceId() {
        return instanceId;
    }

    public byte getFunctionId() {
        return functionId;
    }

    public List<byte[]> getArguments() {
        return arguments;
    }

    public byte[] getArgument(int n) {
        if (n >= arguments.size())
            return null;
        return arguments.get(n);
    }

    public String getArgumentAsString(int n) {
        if (n >= arguments.size())
            return null;
        return new String(arguments.get(n));
    }

    public int getArgumentAsInteger(int bytes, int n) {
        if (n >= arguments.size() && (bytes > 4 || bytes < 1) && arguments.get(n).length != bytes)
            return 0;
        int value = 0;
        for (int i = 0; i < bytes; i++) {
            value |= ((arguments.get(n)[i] << (8 * i)) & ((0xFF) << (8 * i)));
        }
        return value;
    }

    public int getArgumentAsInteger(int n) {
        return getArgumentAsInteger(2, n);
    }

    public float getArgumentAsFloat(int n) {
        byte[] b = getArgument(n);
        if (n >= arguments.size() || b.length != 4)
            return 0;
        for (int i = 0; i < b.length / 2; i++) {
            byte temp = b[i];
            b[i] = b[b.length - i - 1];
            b[b.length - i - 1] = temp;
        }
        return ByteBuffer.wrap(b).getFloat();
    }

    public void addArgument(byte[] argument) {
        arguments.add(Arrays.copyOfRange(argument, 0, (argument.length > 255) ? 255 : argument.length));
    }

    public void addArgument(byte data) {
        arguments.add(new byte[]{data});
    }

    public void addArgument(char data) {
        arguments.add(new byte[]{(byte) data});
    }

    public void addArgument(boolean data) {
        arguments.add(new byte[]{(byte) (data ? 1 : 0)});
    }

    public void addArgument(int bytes, int data) {
        switch (bytes) {
            case 0:
                return;
            case 1:
                arguments.add(new byte[]{(byte) data});
                break;
            case 2:
                arguments.add(new byte[]{(byte) data, (byte) (data >> 8)});
                break;
            case 3:
                arguments.add(new byte[]{(byte) data, (byte) (data >> 8),
                        (byte) (data >> 16)});
                break;
            case 4:
                arguments.add(new byte[]{(byte) data, (byte) (data >> 8),
                        (byte) (data >> 16), (byte) (data >>> 24)});
                break;
            default:
                return;
        }
    }

    public void addArgument(float f) {
        byte[] data = ByteBuffer.allocate(4).putFloat(f).array();
        arguments.add(new byte[]{data[3], data[2], data[1], data[0]});

    }

    public void addArgument(String data) {
        String temp = (data.length() > 255) ? data.substring(0, 255) : data;
        arguments.add(temp.getBytes(Charset.forName("UTF-8")));
    }

    @Override
    public String toString() {
        String s = "";
        for (byte b : getAllFrameAsBytes()) {
            if ((Integer.toHexString(b).length() < 2))
                s += "0" + Integer.toHexString(b) + " ";
            else if ((Integer.toHexString(b).length() == 2))
                s += Integer.toHexString(b) + " ";
            else {
                String temp = Integer.toHexString(b);
                temp = temp.substring(temp.length() - 2);
                s += temp + " ";
            }
        }
        return s;
    }

    public byte[] getAllFrameAsBytes() {
        int totalSizeOfArguments = 0;
        for (byte[] argument : arguments) {
            totalSizeOfArguments += argument.length;
        }
        int frameSize = 7 + arguments.size() * 2 + totalSizeOfArguments;
        byte[] data = new byte[frameSize];
        data[0] = START_OF_FRAME;
        data[1] = shieldId;
        data[2] = instanceId;
        data[3] = functionId;
        data[4] = (byte) arguments.size();
        data[5] = (byte) (255 - arguments.size());

        for (int i = 0, j = 6; i < arguments.size(); i++) {
            data[j] = (byte) arguments.get(i).length;
            data[j + 1] = (byte) (255 - arguments.get(i).length);
            for (int k = 0; k < arguments.get(i).length; k++) {
                data[j + k + 2] = arguments.get(i)[k];
            }
            j += arguments.get(i).length + 2;
        }
        data[frameSize - 1] = END_OF_FRAME;
        return data;
    }

}
