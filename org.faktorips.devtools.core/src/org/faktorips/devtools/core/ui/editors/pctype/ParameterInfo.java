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

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.internal.corext.Assert;
import org.faktorips.devtools.core.model.pctype.Parameter;


public class ParameterInfo {
	
	public static final int INDEX_FOR_ADDED= -1;
	private final String fOldName;
	private final String fOldTypeName;
	private final int fOldIndex;

	private String fNewTypeName;
	private String fDefaultValue;
	private String fNewName;
	private Object fData;
	private boolean fIsDeleted;
	
	public final static ParameterInfo[] createInfos(Parameter[] params) {
	    ParameterInfo infos[] = new ParameterInfo[params.length];
	    for (int i=0; i<params.length; i++) {
	        infos[i] = new ParameterInfo(params[i].getDatatype(), params[i].getName(), i);
	    }
	    return infos;
	}
	
	public final static List createInfosAsList(Parameter[] params) {
	    List result = new ArrayList(params.length);
	    for (int i=0; i<params.length; i++) {
	        result.add(new ParameterInfo(params[i].getDatatype(), params[i].getName(), i));
	    }
	    return result;
	}
	
	public final static Parameter[] createParameters(List infolist) {
	    List params = new ArrayList();
	    int i=0;
	    for (Iterator it=infolist.iterator(); it.hasNext(); i++) {
	        ParameterInfo info = (ParameterInfo)it.next();
	        if (!info.isDeleted()) {
	            Parameter p = new Parameter(i);
	            i++;
		        p.setName(info.fNewName);
		        p.setDatatype(info.fNewTypeName);
		        params.add(p);
	        }
	    }
	    return (Parameter[])params.toArray(new Parameter[params.size()]);
	}
	
	public ParameterInfo(String type, String name, int index) {
		fOldTypeName= type;
		fNewTypeName= type;
		fOldName= name;
		fNewName= name;
		fOldIndex= index;
		fDefaultValue= ""; //$NON-NLS-1$
		fIsDeleted= false;
	}

	public static ParameterInfo createInfoForAddedParameter(){
		ParameterInfo info= new ParameterInfo("String", "newParam", INDEX_FOR_ADDED); //$NON-NLS-1$ //$NON-NLS-2$
		info.setDefaultValue("null"); //$NON-NLS-1$
		return info;
	}
	
	public boolean isDeleted(){
		return fIsDeleted;
	}
	
	public void markAsDeleted(){
		Assert.isTrue(! isAdded()); //added param infos should be simply removed from the list
		fIsDeleted= true;
	}
	
	public boolean isAdded(){
		return fOldIndex == INDEX_FOR_ADDED;
	}
	
	public String getDefaultValue(){
		return fDefaultValue;
	}
	
	public void setDefaultValue(String value){
		Assert.isNotNull(value);
		fDefaultValue= value;
	}

	public String getOldTypeName() {
		return fOldTypeName;
	}
	
	public String getNewTypeName() {
		return fNewTypeName;
	}
	
	public void setNewTypeName(String type){
		Assert.isNotNull(type);
		fNewTypeName= type;
	}

	public String getOldName() {
		return fOldName;
	}

	public int getOldIndex() {
		return fOldIndex;
	}

	public void setNewName(String newName) {
		Assert.isNotNull(newName);
		fNewName= newName;
	}

	public String getNewName() {
		return fNewName;
	}

	public boolean isRenamed() {
		return !fOldName.equals(fNewName);
	}
	
	public boolean isTypeNameChanged() {
		return !fOldTypeName.equals(fNewTypeName);
	}
	
	public String toString() {
		return fOldTypeName + " " + fOldName + " @" + fOldIndex + " -> " //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		+ fNewTypeName + " " + fNewName + ": " + fDefaultValue  //$NON-NLS-1$//$NON-NLS-2$
		+ (fIsDeleted ? " (deleted)" : " (stays)");  //$NON-NLS-1$//$NON-NLS-2$
	}
}
