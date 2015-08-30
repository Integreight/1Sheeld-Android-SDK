package com.integreight.onesheeld.sdk;

/**
 * Represents various arguments types for a {@link KnownFunction} of {@link KnownShield}.
 * @see KnownShields
 * @see KnownArgument
 * @see KnownShield
 * @see KnownFunction
 */
public enum ArgumentType {
    /**
     * Represents a float argument.
     */
    FLOAT,
    /**
     * Represents an integer argument.
     */
    INTEGER,
    /**
     * Represents a raw byte array argument.
     */
    RAW_BYTE_ARRAY,
    /**
     * Represents a string argument.
     */
    STRING,
    /**
     * Represents a boolean argument.
     */
    BOOLEAN,
    /**
     * Represents a single byte argument.
     */
    BYTE,
}
