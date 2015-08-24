package com.integreight.onesheeld.sdk;

import java.util.Collections;
import java.util.List;

public class KnownFunction {
    private byte id;
    private String name;
    private List<KnownArgument> knownArguments;
    private boolean hasVariableLengthArguments;
    private boolean hasVariableArgumentsNumber;

    KnownFunction(byte id, String name, List<KnownArgument> knownArguments, boolean hasVariableArgumentsNumber, boolean hasVariableLengthArguments) {
        this.id = id;
        this.name = name;
        this.knownArguments = knownArguments;
        this.hasVariableArgumentsNumber = hasVariableArgumentsNumber;
        this.hasVariableLengthArguments = hasVariableLengthArguments;
    }

    public static KnownFunction getFunctionWithId(byte id) {
        return new KnownFunction(id, null, null, false, false);
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<KnownArgument> getKnownArguments() {
        return Collections.unmodifiableList(knownArguments);
    }

    public boolean hasVariableLengthArguments() {
        return hasVariableLengthArguments;
    }

    public boolean hasVariableArgumentsNumber() {
        return hasVariableArgumentsNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof KnownFunction)
            return this.getId() == ((KnownFunction) o).getId();
        else return false;
    }

}
