/*
* This code is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License version 3 only, as
* published by the Free Software Foundation.
*
* This code is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
* version 3 for more details (a copy is included in the LICENSE file that
* accompanied this code).
*
* Please contact Integreight, Inc. at info@integreight.com or post on our
* support forums www.1sheeld.com/forum if you need additional information
* or have any questions.
*/

package com.integreight.onesheeld.sdk;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a shield frame with 1Sheeld protocol.
 */
public class ShieldFrame {
    static final byte START_OF_FRAME = (byte) 0xFF;
    static final byte END_OF_FRAME = (byte) 0x00;

    private byte shieldId;
    private byte instanceId;
    private byte functionId;
    private ArrayList<byte[]> arguments;

    /**
     * Instantiates a new <tt>ShieldFrame</tt>.
     *
     * @param shieldId   the shield id
     * @param instanceId the instance id
     * @param functionId the function id
     */
    public ShieldFrame(byte shieldId, byte instanceId, byte functionId) {
        this.shieldId = shieldId;
        this.instanceId = instanceId;
        this.functionId = functionId;
        arguments = new ArrayList<>();
    }

    /**
     * Instantiates a new <tt>ShieldFrame</tt>.
     *
     * @param shieldId   the shield id
     * @param functionId the function id
     */
    public ShieldFrame(byte shieldId, byte functionId) {
        this.shieldId = shieldId;
        this.instanceId = 0;
        this.functionId = functionId;
        arguments = new ArrayList<>();
    }

    /**
     * Instantiates a new <tt>ShieldFrame</tt>.
     *
     * @param shieldId the shield id
     */
    public ShieldFrame(byte shieldId) {
        this.shieldId = shieldId;
        this.instanceId = 0;
        this.functionId = 0;
        arguments = new ArrayList<>();
    }

    /**
     * Gets the shield id.
     *
     * @return the shield id
     */
    public byte getShieldId() {
        return shieldId;
    }

    /**
     * Gets the instance id.
     *
     * @return the instance id
     */
    public byte getInstanceId() {
        return instanceId;
    }

    /**
     * Gets the function id.
     *
     * @return the function id
     */
    public byte getFunctionId() {
        return functionId;
    }

    /**
     * Gets a list of all arguments.
     *
     * @return the arguments
     */
    public List<byte[]> getArguments() {
        return arguments;
    }

    /**
     * Gets a specific argument.
     *
     * @param argNo the argument number
     * @return a byte array or null if the argument is not found.
     */
    public byte[] getArgument(int argNo) {
        if (argNo >= arguments.size())
            return null;
        return arguments.get(argNo);
    }

    /**
     * Parses a specific argument as a string and return it.
     *
     * @param argNo the argument number
     * @return a string or null if the argument is not found.
     */
    public String getArgumentAsString(int argNo) {
        if (argNo >= arguments.size())
            return null;
        return new String(arguments.get(argNo));
    }

    /**
     * Parses a specific argument as an integer and return it.
     *
     * @param argNo the argument number
     * @return the argument as integer or 0 if the argument is not found or the arguments bytes > 4.
     */
    public int getArgumentAsInteger(int argNo) {
        if (argNo >= arguments.size() || arguments.get(argNo).length > 4)
            return 0;
        int value = 0;
        for (int i = 0; i < arguments.get(argNo).length; i++) {
            value |= ((arguments.get(argNo)[i] << (8 * i)) & ((0xFF) << (8 * i)));
        }
        return value;
    }

    /**
     * Parses a specific argument as a float and return it.
     *
     * @param argNo the argument number
     * @return the argument as float or 0 if the argument is not found or the arguments bytes != 4.
     */
    public float getArgumentAsFloat(int argNo) {
        if (argNo >= arguments.size() || arguments.get(argNo).length != 4)
            return 0;
        byte[] b = getArgument(argNo);
        for (int i = 0; i < b.length / 2; i++) {
            byte temp = b[i];
            b[i] = b[b.length - i - 1];
            b[b.length - i - 1] = temp;
        }
        return ByteBuffer.wrap(b).getFloat();
    }

    /**
     * Adds a new bytes array argument.
     *
     * @param argument the argument
     */
    public void addArgument(byte[] argument) {
        arguments.add(Arrays.copyOfRange(argument, 0, (argument.length > 255) ? 255 : argument.length));
    }

    /**
     * Adds a new byte argument.
     *
     * @param data the data
     */
    public void addArgument(byte data) {
        arguments.add(new byte[]{data});
    }

    /**
     * Adds a new character argument.
     *
     * @param data the data
     */
    public void addArgument(char data) {
        arguments.add(new byte[]{(byte) data});
    }

    /**
     * Adds a new boolean argument.
     *
     * @param data the data
     */
    public void addArgument(boolean data) {
        arguments.add(new byte[]{(byte) (data ? 1 : 0)});
    }

    /**
     * Adds a new integer argument.
     *
     * @param bytes the number of bytes to be added from the integer
     * @param data  the data
     */
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
        }
    }

    /**
     * Adds a new integer argument.
     *
     * @param floatNumber the float number
     */
    public void addArgument(float floatNumber) {
        byte[] data = ByteBuffer.allocate(4).putFloat(floatNumber).array();
        arguments.add(new byte[]{data[3], data[2], data[1], data[0]});

    }

    /**
     * Adds a new string argument.
     *
     * @param data the data
     */
    public void addArgument(String data) {
        String temp = (data.length() > 255) ? data.substring(0, 255) : data;
        arguments.add(temp.getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Gets the <tt>ShieldFrame</tt> as a hex string.
     *
     */
    @Override
    public String toString() {
        return ArrayUtils.toHexString(getAllFrameAsBytes());
    }

    /**
     * Get all frame as a raw byte array.
     *
     * @return a raw byte array
     */
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
