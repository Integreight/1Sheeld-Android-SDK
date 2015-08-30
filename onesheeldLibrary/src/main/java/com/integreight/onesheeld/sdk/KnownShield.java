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
 * Represents a known shield.
 * @see KnownShields
 * @see ArgumentType
 * @see KnownFunction
 * @see KnownArgument
 */
public class KnownShield {
    private byte id;
    private String name;
    private List<KnownFunction> knownFunctions;

    KnownShield(byte id, String name, List<KnownFunction> knownFunctions) {
        this.id = id;
        this.name = name;
        this.knownFunctions = knownFunctions;
    }

    /**
     * Gets the id of the shield.
     *
     * @return the id
     */
    public byte getId() {
        return id;
    }

    /**
     * Gets the name of the shield.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets a list of {@link KnownFunction}s for this shield.
     *
     * @return a list of {@link KnownFunction}s
     * @see KnownFunctionW
     */
    public List<KnownFunction> getKnownFunctions() {
        return Collections.unmodifiableList(knownFunctions);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof KnownShield)
            return this.getId() == ((KnownShield) o).getId();
        else return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * getId();
    }
}
