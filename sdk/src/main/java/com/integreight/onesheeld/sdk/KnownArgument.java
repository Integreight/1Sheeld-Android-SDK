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

/**
 * Represents a known argument for a {@link KnownFunction} of {@link KnownShield}.
 *
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
