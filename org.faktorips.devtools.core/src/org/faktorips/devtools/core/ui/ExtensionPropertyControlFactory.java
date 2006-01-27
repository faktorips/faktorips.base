package org.faktorips.devtools.core.ui;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPartContainer;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectPartContainerUIController;

/**
 * @author eidenschink
 *
 * Factory to create the specific Controls for the extensions of a type
 * 
 */
public class ExtensionPropertyControlFactory   {

	private ExtensionPropertyDefinition[] extensionProperties;
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
	            uiToolkit.createFormLabel(workArea, extensionProperties[i].getDisplayName() + ":");
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
	    	if((!extensionProperties[i].getEditedInStandardTextArea().equals("false")) 
	    		&&(extensionEditFields[i] == null))
	    	{
	            uiToolkit.createFormLabel(workArea, extensionProperties[i].getDisplayName() + ":");
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
