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

package org.faktorips.devtools.core;

import java.util.ArrayList;
import java.util.List;

public class TestEnumType {

	public final static TestEnumType FIRSTVALUE = new TestEnumType("1", "first");
	public final static TestEnumType SECONDVALUE = new TestEnumType("2", "second");
	public final static TestEnumType THIRDVALUE = new TestEnumType("3", "third");
	
	private final static List allValues;
	
	static{
		allValues = new ArrayList();
		allValues.add(FIRSTVALUE);
		allValues.add(SECONDVALUE);
		allValues.add(THIRDVALUE);
	}
	
	private String id;
	private String name;

	public TestEnumType(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public final static TestEnumType[] getAllValues(){
		return (TestEnumType[])allValues.toArray(new TestEnumType[allValues.size()]);
	}
	
	public boolean isValueOf(String id){
		try{
			valueOf(id);
			return true;
		}
		catch(IllegalArgumentException e){
			return false;
		}
	}
	
	public final static TestEnumType valueOf(String id){
	
		TestEnumType[] allValues = getAllValues();
		for (int i = 0; i < allValues.length; i++) {
			if(allValues[i].id.equals(id)){
				return allValues[i];
			}
		}
		throw new IllegalArgumentException("Not a valid id for this enum type " + id);
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString(){
		return getId();
	}
}
