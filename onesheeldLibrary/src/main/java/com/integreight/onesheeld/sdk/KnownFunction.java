package com.integreight.onesheeld.sdk;

import java.util.Collections;
import java.util.List;

public class KnownFunction {
    private byte id;
    private String name;
    private List<KnownArgument> knownArguments;
    private boolean hasVariableLengthArguments;

    KnownFunction(byte id, String name, List<KnownArgument> knownArguments, boolean hasVariableLengthArguments) {
        this.id = id;
        this.name = name;
        this.knownArguments = knownArguments;
        this.hasVariableLengthArguments = hasVariableLengthArguments;
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

}
