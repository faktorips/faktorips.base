/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.runtime;

/**
 * Base interface for all model objects.
 * 
 * @author Jan Ortmann
 */
public interface IModelObject {

    /**
     * Validates the policy component and returns a list of messages. If no message is generated the
     * method returns an empty list. Note that also messages like warnings or informations can be
     * returned for valid objects.
     * 
     * @param businessFunction a rule might check against the provided business function if it is to
     *          be applied. This parameter can be null. Implementations have to check for null before
     *          they check against the provided business function
     */
    public MessageList validate(String businessFunction);
    
    /**
     * Adds the given listener to the set of listeners that are notified when this policy component
     * or one of it's children changed.
     * 
     * <p><strong>
     * The listener support is experimental in this version.
     * The API might change without notice until it is finalized in a future version.
     * </strong>
     * 
     * @param listener The listener to add.
     */
    public void addChangeListener(IModelObjectChangeListener listener);

    /**
     * Remoces the given listener from the set of listeners that are notified when this policy component
     * or one of it's children changed. Does nothing if listener is <code>null</code>.
     * 
     * <p><strong>
     * The listener support is experimental in this version.
     * The API might change without notice until it is finalized in a future version.
     * </strong>
     * 
     * @param listener The listener to remove.
     */
    public void removeChangeListener(IModelObjectChangeListener listener);
    
}
