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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectNamingConventions;

public interface IBFElement extends IIpsObjectPart {

    public final static String XML_TAG = "BFElement";

    public final static String PROPERTY_TYPE = "type";
    public final static String PROPERTY_LOCATION = "location";
    public final static String PROPERTY_SIZE = "size";
    public final static String PROPERTY_INCOMMING_EDGES = "incommingEdges";
    public final static String PROPERTY_OUTGOING_EDGES = "outgoingEdges";
    
    public static final String MSGCODE_PREFIX = "BFELEMENT-";
    public static final String MSGCODE_NAME_NOT_SPECIFIED = MSGCODE_PREFIX + "nameNotSpecified";
    public static final String MSGCODE_NAME_NOT_VALID = IIpsProjectNamingConventions.INVALID_NAME;


    public IBusinessFunction getBusinessFunction();
    
    public String getDisplayString();
    
    public Point getLocation();
    
    public void setLocation(Point location);
    
    public Dimension getSize();
    
    public void setSize(Dimension size);
    
    public BFElementType getType();
    
    public void setName(String name);
    
    public void addIncomingControlFlow(IControlFlow controlFlow);
    
    public List<IControlFlow> getIncomingControlFlow();
    
    public boolean removeIncomingControlFlow(IControlFlow controlFlow);
    
    public void addOutgoingControlFlow(IControlFlow controlFlow);
    
    public List<IControlFlow> getOutgoingControlFlow();
    
    public boolean removeOutgoingControlFlow(IControlFlow controlFlow);
    
    public List<IControlFlow> getAllControlFlows();
}
