/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.internal.toc;

import org.faktorips.runtime.IRuntimeObject;
import org.faktorips.runtime.IRuntimeRepository;

public abstract class TypedTocEntryObject<T extends IRuntimeObject> extends TocEntryObject {

    protected TypedTocEntryObject(String ipsObjectQualifiedName, String xmlResourceName, String implementationClassName) {
        super("", ipsObjectQualifiedName, xmlResourceName, implementationClassName);
    }

    public abstract T createRuntimeObject(IRuntimeRepository repository);

    public abstract Class<T> getRuntimeObjectClass();

    public abstract String getIpsObjectTypeId();

}
