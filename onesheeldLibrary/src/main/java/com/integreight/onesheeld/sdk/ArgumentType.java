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
 * Represents various arguments types for a {@link KnownFunction} of {@link KnownShield}.
 *
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
