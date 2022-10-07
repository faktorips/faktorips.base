package org.faktorips.devtools.model.abstractions;

import java.util.function.Consumer;

import org.faktorips.devtools.model.abstractions.WorkspaceAbstractions.AWorkspaceAbstractionsImplementation;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.IpsModel.EclipseIpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.jdtcontainer.IpsContainer4JdtClasspathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;

public enum EclipseWorkspaceImplementation implements AWorkspaceAbstractionsImplementation {
    INSTANCE;

    static EclipseWorkspaceImplementation get() {
        return INSTANCE;
    }

    @Override
    public IpsModel createIpsModel() {
        return new EclipseIpsModel();
    }

    @Override
    public void addRequiredEntriesToIpsObjectPath(IpsObjectPath ipsObjectPath,
            Consumer<IIpsObjectPathEntry> entryAdder) {
        IpsContainer4JdtClasspathContainer.addRequiredEntriesToIpsObjectPath(ipsObjectPath, entryAdder);
    }
}
