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

package org.faktorips.devtools.core.internal.model.tablestructure;

/**
 * This enumeration defines all possible value for the type of the table structure. 
 * 
 * @author Thorsten Guenther
 */
public class TableStructureType {
	/**
	 * Single content - for this table structure only on table content is allowed.
	 */
	public static final TableStructureType SINGLE_CONTENT = new TableStructureType("singleContent", "Single Content");
	
	/**
	 * Multiple contents - for this table structure one or more table contents are allowed.
	 */
	public static final TableStructureType MULTIPLE_CONTENTS = new TableStructureType("multipleContents", "Multiple Contents");
	
	/**
	 * EnumType, values are model-defined - this table structure represents an EnumType. All values of this EnumType
	 * are defined in the model.
	 */
	public static final TableStructureType ENUMTYPE_MODEL = new TableStructureType("enumTypeModel", "EnumType (values are part of the model)");
	
	/**
	 * EnumType, values are part of the product definition - this table structure represents an EnumType.
	 */
	public static final TableStructureType ENUMTYPE_PRODUCTDEFINTION = new TableStructureType("enumTypeProductDefinition", "EnumType (values are part of the product definiton");

	private static TableStructureType[] allValues = {SINGLE_CONTENT, MULTIPLE_CONTENTS, ENUMTYPE_MODEL, ENUMTYPE_PRODUCTDEFINTION};
	
	private String name;
	private String id;
	
	private TableStructureType(String id, String name) {
		this.name = new String(name);
		this.id = id;
	}
	
	/**
	 * @return The name (human readable text) for this type. This value can be used for labels in the UI, for 
	 * example.
	 */
	public String getName() {
		return new String(name);
	}
	
	/**
	 * @return The id for this type. Used for persistance-purposes, for example. 
	 */
	public String getId() {
		return new String(id);
	}
	
	/**
	 * @param index The index of the type.
	 * @return The type defined for the given index.
	 * @throws IndexOutOfBoundsException If the index is out of bounds.
	 */
	public TableStructureType getType(int index) throws IndexOutOfBoundsException {
		return allValues[index];
	}
	
	/**
	 * @return The numer of types avaliable.
	 */
	public int getNumberOfTypes() {
		return allValues.length;
	}
	
	/**
	 * @param id The id defining the type
	 * @return The type defined by the given id.
	 * @throws IllegalArgumentException If the given id does not represent a valid type.
	 */
	public static TableStructureType getTypeForId(String id) throws IllegalArgumentException {
		if (id.equals(SINGLE_CONTENT.getId())) {
			return SINGLE_CONTENT;
		}
		else if (id.equals(MULTIPLE_CONTENTS.getId())) {
			return MULTIPLE_CONTENTS;
		}
		else if (id.equals(ENUMTYPE_MODEL.getId())) {
			return ENUMTYPE_MODEL;
		}
		else if (id.equals(ENUMTYPE_PRODUCTDEFINTION.getId())) {
			return ENUMTYPE_PRODUCTDEFINTION;
		}
		throw new IllegalArgumentException("Unknown type-id " + id);
	}
	
	/**
	 * @return All types defined as array.
	 */
	public static TableStructureType[] getAll() {
		return allValues;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return new String(name);
	}
}