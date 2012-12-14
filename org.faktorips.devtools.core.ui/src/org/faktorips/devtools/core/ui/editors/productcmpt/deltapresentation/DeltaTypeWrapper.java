/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation;

import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainerToTypeDelta;

/**
 * Small helper class which provides the {@link DeltaType} and its parent, the
 * {@link IPropertyValueContainerToTypeDelta}. This class is necessary for the
 * {@link DeltaContentProvider} and {@link DeltaLabelProvider}.
 */
class DeltaTypeWrapper {
    DeltaType type;
    IPropertyValueContainerToTypeDelta delta;

    DeltaTypeWrapper(DeltaType type, IPropertyValueContainerToTypeDelta delta) {
        this.type = type;
        this.delta = delta;
    }
}