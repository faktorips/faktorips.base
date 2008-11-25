/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.model.bf;

import java.util.List;

import org.eclipse.draw2d.Bendpoint;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

public interface IControlFlow extends IIpsObjectPart {

    public final static String XML_TAG = "ControlFlow";
    public static final String PROPERTY_BENDPOINT = "bendPoint";
    public static final String PROPERTY_TARGET = "target";
    public static final String PROPERTY_SOURCE = "source";
    
    public IBusinessFunction getBusinessFunction();
    
    public IBFElement getSource();

    public void setSource(IBFElement source);
    
    public IBFElement getTarget();
    
    public void setTarget(IBFElement target);
    
    public void setBendpoint(int index, Bendpoint bendpoint);
    
    public List<Bendpoint> getBendpoints();
    
    public void addBendpoint(int index, Bendpoint bendpoint);
    
    public void removeBendpoint(int index);

}
