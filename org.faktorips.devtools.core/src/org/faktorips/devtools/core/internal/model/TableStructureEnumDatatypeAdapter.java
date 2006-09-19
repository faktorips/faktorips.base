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

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.util.ArgumentCheck;

/**
 * An adatper that adapts a given table structure to an EnumDatatype.
 * 
 * @author Jan Ortmann
 */
public class TableStructureEnumDatatypeAdapter extends AbstractDatatype implements EnumDatatype {

	private ITableStructure table;
	private IColumn idColumn;
	private IIpsProject project;
	private ITableContents contents;
	private int idIndex;
	private ValueDatatype datatype;
	
	public TableStructureEnumDatatypeAdapter(ITableStructure table, IIpsProject project) {
		ArgumentCheck.notNull(table);
		this.table = table;
		this.project = project;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getAllValueIds(boolean includeNull) {
		if (getContentGeneration() == null) {
			return new String[0];
		}
		
		IRow[] rows = getContentGeneration().getRows();
		int size = rows.length;

		if (includeNull) {
			size++;
		}
		
		String[] result = new String[size];
		
		for (int i = 0; i < rows.length; i++) {
			result[i] = rows[i].getValue(getIdIndex());
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSupportingNames() {
		return false;
	}

	/**
	 * Not supported
	 */
	public String getValueName(String id) {
		throw new UnsupportedOperationException("Not supported."); //$NON-NLS-1$
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
		if (value == null) {
			return true;
		}
		
		if (datatype == null) {
			return Arrays.asList(getAllValueIds(false)).contains(value);
		}

		String[] values = getAllValueIds(false);
		for (int i = 0; i < values.length; i++) {
			if (datatype.areValuesEqual(value, values[i])) {
				return true;
			}
		}
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
		try {
			return project.getArtefactBuilderSet().getDatatypeHelperForTableBasedEnum(this).getJavaClassName();
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isNull(String value) {
		return value == null;
	}

	public boolean supportsCompare() {
		return false;
	}

	public int compare(String valueA, String valueB) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("TableStructureEnumDatatype " + getQualifiedName() + "does not support comparison for values"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public boolean areValuesEqual(String valueA, String valueB) {
		if (datatype == null) {
			return ObjectUtils.equals(valueA, valueB);
		}
		
		return ((ValueDatatype)datatype).areValuesEqual(valueA, valueB);
	}
	
	private ITableContents getContents() {
		init(false);
		return contents;
	}
	
	private ITableContentsGeneration getContentGeneration() {
		ITableContents contents = getContents();
		if (contents != null) {
			return (ITableContentsGeneration)contents.getFirstGeneration();
		}
		return null;
	}
	
	private int getIdIndex() {
		init(false);
		return idIndex;
	}

	private void init(boolean force) {
		if (contents != null && !force) {
			return;
		}
		try {
			IIpsObject[] allContents = project.findIpsObjects(IpsObjectType.TABLE_CONTENTS);
			for (int i = 0; i < allContents.length; i++) {
				if (((ITableContents)allContents[i]).getTableStructure().equals(table.getQualifiedName())) {
					contents = (ITableContents)allContents[i];
				}
			}
		} catch (CoreException e) {
			throw new RuntimeException("Error during search for table contents for " + table.getQualifiedName(), e); //$NON-NLS-1$
		}
		
		IUniqueKey[] keys = table.getUniqueKeys();
		for (int i = 0; i < keys.length; i++) {
			if (keys[i].getNumOfKeyItems() == 1 && !keys[i].containsRanges()) {
				idColumn = keys[i].getKeyItems()[0].getColumns()[0];
				idIndex = Arrays.asList(table.getColumns()).indexOf(idColumn);
			}
		}
		
		try {
			Datatype type = project.findDatatype(idColumn.getDatatype());
			if (type.isValueDatatype()) {
				datatype = (ValueDatatype)type;
			}
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}
	}
}
