package com.integreight.onesheeld.sdk;

/**
 * Represents a known argument for a {@link KnownFunction} of {@link KnownShield}.
 * @see KnownShields
 * @see ArgumentType
 * @see KnownShield
 * @see KnownFunction
 */
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

    /**
     * Gets the name of the argument.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type of the argument.
     *
     * @return the type
     * @see ArgumentType
     */
    public ArgumentType getType() {
        return type;
    }

    /**
     * Gets the length of the argument's bytes.
     *
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * Checks whether this argument has a fixed byte length or not.
     *
     * @return the boolean
     */
    public boolean isVariableLength() {
        return isVariableLength;
    }

    /**
     * Checks whether this argument is optional in the {@link KnownFunction} or not.
     *
     * @return the boolean
     */
    public boolean isOptional() {
        return isOptional;
    }

    /**
     * Checks whether this argument can be repeated multipe times in the {@link KnownFunction} or not.
     *
     * @return the boolean
     */
    public boolean canBeMultiple() {
        return canBeMultiple;
    }
}
