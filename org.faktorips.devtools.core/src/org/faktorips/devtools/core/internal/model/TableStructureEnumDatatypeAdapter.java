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

package org.faktorips.devtools.core.internal.model;

import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.ArgumentCheck;

/**
 * An adatper that adapts a given table structure to an EnumDatatype.
 * 
 * @author Jan Ortmann
 */
public class TableStructureEnumDatatypeAdapter extends AbstractDatatype implements EnumDatatype {

	private ITableStructure table;
	
	public TableStructureEnumDatatypeAdapter(ITableStructure table) {
		ArgumentCheck.notNull(table);
		this.table = table;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getAllValueIds(boolean includeNull) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSupportingNames() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getValueName(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Datatype getWrapperType() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isParsable(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getValue(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String valueToString(Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isNull(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return table.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getQualifiedName() {
		return table.getQualifiedName();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPrimitive() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isValueDatatype() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getJavaClassName() {
		// TODO Auto-generated method stub
		return null;
	}

}
