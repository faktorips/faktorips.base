/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablecontents;

import java.io.InputStream;
import java.util.GregorianCalendar;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectGeneration;
import org.faktorips.devtools.core.internal.model.ipsobject.TimedIpsObject;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;


/**
 *
 */
public class TableContents extends TimedIpsObject implements ITableContents {

    /**
     * The name of the table structure property
     */
    public final static String PROPERTY_TABLE_STRUCTURE = "tableStructure"; //$NON-NLS-1$

    private String structure = ""; //$NON-NLS-1$
    private int numOfColumns = 0;

    /**
     * @param file
     */
    public TableContents(IIpsSrcFile file) {
        super(file);
    }

    IpsObjectGeneration createNewGenerationInternal(GregorianCalendar validFrom) {
        TableContentsGeneration generation = (TableContentsGeneration)super.newGenerationInternal(getNextPartId());
        generation.setValidFromInternal(validFrom);
        return generation;
    }
    
    /**
     * {@inheritDoc}
     */
    protected IpsObjectGeneration createNewGeneration(int id) {
        TableContentsGeneration tableContentsGeneration = new TableContentsGeneration(this, id);
        initUniqueKeyValidator(tableContentsGeneration);
        return tableContentsGeneration;
    }

    /*
     * Creates an unique key validator for the given table contents generation
     */
    private void initUniqueKeyValidator(TableContentsGeneration tableContentsGeneration) {
        ITableStructure tableStructure;
        try {
            tableStructure = findTableStructure(getIpsProject());
        } catch (CoreException e){
            // will be handled as validation error
            IpsPlugin.log(e);
            return;
        } 
        tableContentsGeneration.initUniqueKeyValidator(tableStructure, new UniqueKeyValidator());
    }

    /**
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.TABLE_CONTENTS;
    }

    /**
     * {@inheritDoc}
     */
    public String getTableStructure() {
        return structure;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setTableStructure(String qName) {
        String oldStructure = structure;
        setTableStructureInternal(qName);
        valueChanged(oldStructure, structure);
    }
    
    protected void setTableStructureInternal(String qName){
        structure = qName;
    }
   
    /**
     * {@inheritDoc}
     */
    public ITableStructure findTableStructure(IIpsProject ipsProject) throws CoreException {
        return (ITableStructure)getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, structure);
    }
    
    /**
     * {@inheritDoc}
     */
    public int getNumOfColumns() {
        return numOfColumns;
    }

    /**
     * Sets the number of columns
     * @param numOfColumns 
     */
    public void setNumOfColumnsInternal(int numOfColumns){
        this.numOfColumns = numOfColumns;
    }
    
    /**
     * {@inheritDoc}
     */
    public int newColumn(String defaultValue) {
    	newColumnAt(numOfColumns, defaultValue);
        return numOfColumns;
    }

    /**
     * {@inheritDoc}
     */
    public void newColumnAt(int index, String defaultValue) {
        IIpsObjectGeneration[] generations = getGenerationsOrderedByValidDate();
        for (int i=0; i<generations.length; i++) {
            ((TableContentsGeneration)generations[i]).newColumn(index, defaultValue);
        }
        numOfColumns++;
        objectHasChanged();
	}

    /**
     * {@inheritDoc}
     */
    public void deleteColumn(int columnIndex) {
        if (columnIndex<0 || columnIndex>=numOfColumns) {
            throw new IllegalArgumentException("Illegal column index " + columnIndex); //$NON-NLS-1$
        }
        IIpsObjectGeneration[] generations = getGenerationsOrderedByValidDate();
        for (int i=0; i<generations.length; i++) {
            ((TableContentsGeneration)generations[i]).removeColumn(columnIndex);
        }
        numOfColumns--;
        objectHasChanged();        
    }
    
    /**
     * {@inheritDoc}
     */
    public IDependency[] dependsOn() throws CoreException {
        if (StringUtils.isEmpty(getTableStructure())) {
            return new IDependency[0];
        }
        return new IDependency[] { IpsObjectDependency.createInstanceOfDependency(this.getQualifiedNameType(), new QualifiedNameType(
                getTableStructure(), IpsObjectType.TABLE_STRUCTURE)) };
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsObjectPart newPart(Class partType) {
        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_TABLESTRUCTURE, structure);
        newElement.setAttribute(PROPERTY_NUMOFCOLUMNS, "" + numOfColumns); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        structure = element.getAttribute(PROPERTY_TABLESTRUCTURE);
        numOfColumns = Integer.parseInt(element.getAttribute(PROPERTY_NUMOFCOLUMNS)); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public void initFromInputStream(InputStream is) throws CoreException {
        try {
            reinitPartCollections();
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(new InputSource(is), new TableContentsSaxHandler(this));
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        ITableStructure tableStructure = findTableStructure(ipsProject);
        if (tableStructure  == null) {
            String text = NLS.bind(Messages.TableContents_msgMissingTablestructure, this.structure);
            list.add(new Message(MSGCODE_UNKNWON_STRUCTURE, text, Message.ERROR, this, PROPERTY_TABLE_STRUCTURE)); 
            return;
        }
        
        if (tableStructure .getNumOfColumns() != getNumOfColumns()) {
        	Integer structCols = new Integer(tableStructure .getNumOfColumns());
        	Integer contentCols = new Integer(getNumOfColumns());
        	String text = NLS.bind(Messages.TableContents_msgColumncountMismatch, structCols, contentCols);
        	list.add(new Message(MSGCODE_COLUMNCOUNT_MISMATCH, text, Message.ERROR, this, PROPERTY_TABLE_STRUCTURE));
        }
    }
    
    ValueDatatype[] findColumnDatatypes(ITableStructure structure, IIpsProject ipsProject) throws CoreException {
        if (structure == null){
            return new ValueDatatype[0];
        }
        IColumn[] columns= structure.getColumns();
        ValueDatatype[] datatypes= new ValueDatatype[columns.length];
        for (int i = 0; i < columns.length; i++) {
            datatypes[i]= columns[i].findValueDatatype(ipsProject);
        }
        return datatypes;
    }
    
    /** 
     * {@inheritDoc}
     */
    public void addExtensionProperty(String propertyId, String extPropertyValue) {
        addExtensionPropertyValue(propertyId, extPropertyValue);
    }

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.core.model.IIpsMetaObject#findMetaClass(org.faktorips.devtools.core.model.ipsproject.IIpsProject)
	 */
	/**
	 * {@inheritDoc}
	 */
	public IIpsSrcFile findMetaClassSrcFile(IIpsProject ipsProject)
			throws CoreException {
		return ipsProject.findIpsSrcFile(IpsObjectType.TABLE_STRUCTURE, getTableStructure());
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.core.model.ipsobject.IFixDifferencesToModelSupport#containsDifferenceToModel(org.faktorips.devtools.core.model.ipsproject.IIpsProject)
	 */
	/**
	 * This Method always returns false because differences to model is not supported at the moment
	 */
	public boolean containsDifferenceToModel(IIpsProject ipsProject)
			throws CoreException {
		//TODO throw new NotImplementedException();
		return false;
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.core.model.ipsobject.IFixDifferencesToModelSupport#fixAllDifferencesToModel(org.faktorips.devtools.core.model.ipsproject.IIpsProject)
	 */
	/**
	 * This method does nothing because there is nothing to do at the moment
	 * 
	 */
	public void fixAllDifferencesToModel(IIpsProject ipsProject)
			throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.core.model.IIpsMetaObject#getMetaClass()
	 */
	/**
	 * {@inheritDoc}
	 */
	public String getMetaClass() {
		return getTableStructure();
	}
}
