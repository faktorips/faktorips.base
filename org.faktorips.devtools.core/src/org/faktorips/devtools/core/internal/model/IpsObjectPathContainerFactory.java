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

package org.faktorips.devtools.core.internal.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * A factory to create {@link IIpsObjectPathContainer} based on registered
 * {@link IIpsObjectPathContainerType}s.
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPathContainerFactory {

    /**
     * Creates a new factory with all container types defined as extensions for the extension point
     * {@link ExtensionPoints#IPS_OBJECT_PATH_CONTAINER_TYPE}.
     * 
     * @return The new factory.
     */
    public final static IpsObjectPathContainerFactory newFactoryBasedOnExtensions() {
        List<IIpsObjectPathContainerType> types = new ExtensionPoints().createExecutableExtensions(
                ExtensionPoints.IPS_OBJECT_PATH_CONTAINER_TYPE,
                "containerType", "class", IIpsObjectPathContainerType.class); //$NON-NLS-1$ //$NON-NLS-2$
        return newFactory(types);
    }

    /**
     * Creates a new factory with the given types registered.
     * 
     * @param types A list of types.
     * @return The new factory.
     */
    public final static IpsObjectPathContainerFactory newFactory(List<IIpsObjectPathContainerType> types) {
        IpsObjectPathContainerFactory newFactory = new IpsObjectPathContainerFactory();
        for (IIpsObjectPathContainerType type : types) {
            if (type.getId() == null) {
                IpsPlugin.log(new IpsStatus("Can't register type " + type + " as id is null.")); //$NON-NLS-1$ //$NON-NLS-2$
                continue;
            }
            IIpsObjectPathContainerType otherType = newFactory.getContainerType(type.getId());
            if (otherType != null && otherType != type) {
                IpsPlugin.log(new IpsStatus("Can't register type " + type //$NON-NLS-1$
                        + ", as another type is registered with the same id, id=" + type.getId())); //$NON-NLS-1$
                continue;
            }
            newFactory.registerContainerType(type);
        }
        return newFactory;
    }

    private Map<String, IIpsObjectPathContainerType> typesById = new HashMap<String, IIpsObjectPathContainerType>();

    /**
     * Creates a new container for the given container type id.
     * 
     * @param ipsProject The IPS project to create the container for.
     * @param containerTypeId The kind of the container requested.
     * @param optionalPath The optional container path.
     * 
     * @return a new container.
     */
    public IIpsObjectPathContainer newContainer(IIpsProject ipsProject, String containerTypeId, String optionalPath) {
        IIpsObjectPathContainerType type = getContainerType(containerTypeId);
        if (type == null) {
            return null;
        }
        return type.newContainer(ipsProject, optionalPath);
    }

    /**
     * Returns the container type for the given id or <code>null</code> if none is found.
     */
    public IIpsObjectPathContainerType getContainerType(String containerTypeId) {
        return typesById.get(containerTypeId);
    }

    /**
     * Returns <code>true</code> if the given type is registered, otherwise <code>false</code>.
     * 
     * @param type The type to check.
     * 
     * @throws NullPointerException if type is <code>null</code> or the type's id is
     *             <code>null</code>.
     */
    public boolean isRegistered(IIpsObjectPathContainerType type) {
        return type.equals(getContainerType(type.getId()));
    }

    /**
     * Registers the given container type.
     * 
     * @throws NullPointerException if newType is <code>null</code> or newType's is is
     *             <code>null</code>.
     * @throws IllegalArgumentException if newType has the same ID as a different(!) type registered
     *             before.
     */
    public void registerContainerType(IIpsObjectPathContainerType newType) {
        ArgumentCheck.notNull(newType.getId());
        IIpsObjectPathContainerType type = getContainerType(newType.getId());
        if (type != null & !newType.equals(type)) {
            throw new IllegalArgumentException("There is already the type " + type + " registererd for the ID " //$NON-NLS-1$ //$NON-NLS-2$
                    + newType.getId());
        }
        typesById.put(newType.getId(), newType);
    }

    /**
     * Unregisters the given container type.
     * 
     * @throws NullPointerException if type is <code>null</code>.
     * @throws IllegalArgumentException if the type is not registered.
     */
    public void unregisterContainerType(IIpsObjectPathContainerType type) {
        if (getContainerType(type.getId()) == null) {
            throw new IllegalArgumentException("The type " + type + " hasn't been registererd"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        typesById.remove(type.getId());
    }

    /**
     * Returns the number of registered types.
     */
    public int getNumOfRegisteredTypes() {
        return typesById.size();
    }
}
