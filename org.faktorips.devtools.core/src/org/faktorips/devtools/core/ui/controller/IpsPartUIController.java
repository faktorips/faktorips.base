/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller;

import org.faktorips.devtools.core.model.IIpsObjectPart;


/**
 * A controller to link edit fields against the model and, in addition to 
 * the <code>DefaultUIController</code>, does validation to the part created for.
 * 
 * @author Jan Ortmann
 */
public class IpsPartUIController extends IpsObjectPartContainerUIController {

    public IpsPartUIController(IIpsObjectPart part) {
        super(part);
    }
    
    public IIpsObjectPart getIpsObjectPart() {
        return (IIpsObjectPart)getIpsObjectPartContainer();
    }
    
    
}
