package com.integreight.onesheeld.sdk;

public class KnownArgument {
    private String name;
    private ArgumentType type;
    private int length;
    private boolean isVariableLength;
    private boolean isOptional;
    private boolean canBeMultiple;

    KnownArgument(String name, ArgumentType type, int length, boolean isOptional) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.isVariableLength = false;
        this.isOptional = isOptional;
        this.canBeMultiple = false;
    }

    KnownArgument(String name, ArgumentType type, int length, boolean isOptional, boolean canBeMultiple) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.isVariableLength = false;
        this.isOptional = isOptional;
        this.canBeMultiple = canBeMultiple;
    }

    KnownArgument(String name, ArgumentType type, boolean isVariableLength, boolean isOptional) {
        this.name = name;
        this.type = type;
        this.length = 0;
        this.isVariableLength = isVariableLength;
        this.isOptional = isOptional;
        this.canBeMultiple = false;
    }

    KnownArgument(String name, ArgumentType type, boolean isVariableLength, boolean isOptional, boolean canBeMultiple) {
        this.name = name;
        this.type = type;
        this.length = 0;
        this.isVariableLength = isVariableLength;
        this.isOptional = isOptional;
        this.canBeMultiple = canBeMultiple;
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

    public boolean isOptional() {
        return isOptional;
    }

    public boolean canBeMultiple() {
        return canBeMultiple;
    }
}
