/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.runtime;

import java.util.Collection;
import java.util.List;

/**
 * 
 * @author Jan Ortmann
 */
public interface IObjectReferenceStore {

    public void putObject(Object id, Object entityObject);

    public void addUnresolvedReference(IUnresolvedReference reference);

    public Object getObject(Class<?> clazz, Object id);

    /**
     * Returns a list containing all objects registered.
     */
    public Collection<List<Object>> getAllObjects();

    public Collection<IUnresolvedReference> getAllUnresolvedReferences();

    public void resolveReferences() throws Exception;

}
