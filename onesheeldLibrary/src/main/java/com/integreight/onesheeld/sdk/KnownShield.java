package com.integreight.onesheeld.sdk;

import java.util.Collections;
import java.util.List;

public class KnownShield {
    private byte id;
    private String name;
    private List<KnownFunction> knownFunctions;

    KnownShield(byte id, String name, List<KnownFunction> knownFunctions) {
        this.id = id;
        this.name = name;
        this.knownFunctions = knownFunctions;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

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
