package org.faktorips.devtools.model.plainjava.internal;

import java.util.function.Consumer;

import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.abstractions.WorkspaceAbstractions.AWorkspaceAbstractionsImplementation;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;

public enum PlainJavaWorkspaceAbstractionsImplementation implements AWorkspaceAbstractionsImplementation {
    INSTANCE;

    static PlainJavaWorkspaceAbstractionsImplementation get() {
        return INSTANCE;
    }

    @Override
    public IpsModel createIpsModel() {
        return new PlainJavaIpsModel();
    }

    @Override
    public void addRequiredEntriesToIpsObjectPath(IpsObjectPath ipsObjectPath,
            Consumer<IIpsObjectPathEntry> entryAdder) {
        // nothing to do (yet? maybe with Maven....)
    }

    @Override
    public IIpsModelExtensions getIpsModelExtensions() {
        return PlainJavaIpsModelExtensions.get();
    }
}
