package com.integreight.onesheeld.sdk;

import java.util.Collections;
import java.util.List;

public class KnownFunction {
    private byte id;
    private String name;
    private List<KnownArgument> knownArguments;

    KnownFunction(byte id, String name, List<KnownArgument> knownArguments) {
        this.id = id;
        this.name = name;
        this.knownArguments = knownArguments;
    }

    public static KnownFunction getFunctionWithId(byte id) {
        return new KnownFunction(id, null, null);
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
        if(knownArguments!=null){
            for(KnownArgument arg:knownArguments){
                if(arg.isVariableLength())return true;
            }
            return false;
        }
        else return false;
    }

    public boolean hasVariableArgumentsNumber() {
        if(knownArguments!=null){
            for(KnownArgument arg:knownArguments){
                if(arg.isOptional()||arg.canBeMultiple())return true;
            }
            return false;
        }
        else return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof KnownFunction)
            return this.getId() == ((KnownFunction) o).getId();
        else return false;
    }

}
