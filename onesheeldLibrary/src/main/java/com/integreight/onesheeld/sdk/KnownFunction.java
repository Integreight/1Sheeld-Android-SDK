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

import java.util.Collections;
import java.util.List;

/**
 * Represents a known function of a {@link KnownShield}.
 * @see KnownShields
 * @see ArgumentType
 * @see KnownShield
 * @see KnownArgument
 */
public class KnownFunction {
    private byte id;
    private String name;
    private List<KnownArgument> knownArguments;

    KnownFunction(byte id, String name, List<KnownArgument> knownArguments) {
        this.id = id;
        this.name = name;
        this.knownArguments = knownArguments;
    }

    /**
     * Gets a new <tt>KnownFunction</tt> with a specific id.
     * <p>Should be used for checking equality with another <tt>KnownFunction</tt></p>
     *
     * @param id the id of the new <tt>KnownFunction</tt>
     * @return a new object of <tt>KnownFunction</tt>
     */
    public static KnownFunction getFunctionWithId(byte id) {
        return new KnownFunction(id, null, null);
    }

    /**
     * Gets the id of the function.
     *
     * @return the id
     */
    public byte getId() {
        return id;
    }

    /**
     * Gets the name of the function.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets a list of {@link KnownArgument}s for this function.
     *
     * @return a list of {@link KnownArgument}s
     * @see KnownArgument
     */
    public List<KnownArgument> getKnownArguments() {
        return Collections.unmodifiableList(knownArguments);
    }

    /**
     * Checks whether this function has any argument with variable length.
     *
     * @return the boolean
     */
    public boolean hasVariableLengthArguments() {
        if (knownArguments != null) {
            for (KnownArgument arg : knownArguments) {
                if (arg.isVariableLength()) return true;
            }
            return false;
        } else return false;
    }

    /**
     * Checks whether this function has a variable number of arguments.
     *
     * @return the boolean
     */
    public boolean hasVariableArgumentsNumber() {
        if (knownArguments != null) {
            for (KnownArgument arg : knownArguments) {
                if (arg.isOptional() || arg.canBeMultiple()) return true;
            }
            return false;
        } else return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof KnownFunction)
            return this.getId() == ((KnownFunction) o).getId();
        else return false;
    }

}
