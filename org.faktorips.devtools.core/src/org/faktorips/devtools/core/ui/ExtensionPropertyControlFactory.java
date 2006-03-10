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

package org.faktorips.devtools.core.ui;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPartContainer;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectPartContainerUIController;

/**
 * @author eidenschink
 *
 * Factory to create the specific Controls for the extensions of a type
 * 
 */
public class ExtensionPropertyControlFactory   {

	private IExtensionPropertyDefinition[] extensionProperties;
	private EditField[] extensionEditFields;
	
	public ExtensionPropertyControlFactory(Class extensionClass) {
	    extensionProperties = IpsPlugin.getDefault().getIpsModel().getExtensionPropertyDefinitions(extensionClass,true);
	    extensionEditFields = new EditField[extensionProperties.length];
	}
	
	
	/*
	 * erzeugt die <code>EditFields</code> zu einer Extension zu einer bestimmten Position <code>where</code>
	 */
	public void createControls(Composite workArea, UIToolkit uiToolkit, IpsObjectPartContainer ipsObjectPart, String where) 
	{
		for(int i=0; i< extensionProperties.length; i++)
	    {
	    	if(extensionProperties[i].getEditedInStandardTextArea().equals(where)
	    			&&(extensionEditFields[i]==null))
	    	{
	            uiToolkit.createFormLabel(workArea, extensionProperties[i].getDisplayName() + ":"); //$NON-NLS-1$
	            extensionEditFields[i]=extensionProperties[i].newEditField(ipsObjectPart, workArea, uiToolkit);    		
	    	}
	    }
		
	}
	
	
	
	/*
	 * erzeugt alle noch nicht explizit  erzeugten EditFields zu einer Extension ausser
	 * denen, die im plugin.xml mit <code>false</code> gekennzeichnet sind
	 */
	public void createControls(Composite workArea, UIToolkit uiToolkit, IpsObjectPartContainer ipsObjectPart)
	{
		for(int i=0; i< extensionProperties.length; i++)
	    {
	    	if((!extensionProperties[i].getEditedInStandardTextArea().equals("false"))  //$NON-NLS-1$
	    		&&(extensionEditFields[i] == null))
	    	{
	            uiToolkit.createFormLabel(workArea, extensionProperties[i].getDisplayName() + ":"); //$NON-NLS-1$
	            extensionEditFields[i]=extensionProperties[i].newEditField(ipsObjectPart, workArea, uiToolkit);    		
	    	}
	    }
		
	}

	

	/*
	 * alle erzeugten EditFields mit dem Modell verbinden
	 */
	public void connectToModel(IpsObjectPartContainerUIController uiController) {
		for(int i=0; i<extensionEditFields.length; i++)
        {
        	if(extensionEditFields[i]!=null)
        	{
        		uiController.add(extensionEditFields[i],extensionProperties[i].getPropertyId());
        	}
        }

	}


}
