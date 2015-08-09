package com.integreight.onesheeld.sdk;

public class KnownArgument {
    private String name;
    private ArgumentType type;
    private int length;
    private boolean isVariableLength;

    KnownArgument(String name, ArgumentType type, int length, boolean isVariableLength) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.isVariableLength = isVariableLength;
    }

    public String getName() {
        return name;
    }

    public ArgumentType getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public boolean isVariableLength() {
        return isVariableLength;
    }
}
