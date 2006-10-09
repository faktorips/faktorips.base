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

package org.faktorips.devtools.core.ui.team.compare.tablecontents;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.ui.team.compare.AbstractCompareItem;

/**
 * A CompareItem for building a structure that represents a <code>ITableContents</code> object.
 * The <code>IIpsSrcFile</code>, the <code>ITableContents</code>, its generations and all
 * contained rows are each represented by a <code>TableContentsCompareItem</code>.
 * 
 * @see org.faktorips.devtools.core.ui.team.compare.AbstractCompareItem
 * @author Stefan Widmaier
 */
public class TableContentsCompareItem extends AbstractCompareItem {
    /**
     * Array containing the widths of all columns of the table counted in tabulators. 
     * The array has a length of the tables' columnNumber plus one. The first entry defines the width
     * of the column of rownumbers, the others contain the widths of the content-columns of the table.
     * Thus columnWidthsInTabs[2] contains the width of the second content column in tabs.
     */
    private int[] columnWidthsInTabs= null;

    /**
     * Creates an <code>TableContentsCompareItem</code> using the given parent and
     * <code>IIpsElement</code>. If the given parent is null, this
     * <code>TableContentsCompareItem</code> is marked as a root element, as indicated by the
     * method isRoot(). The given <code>IIpsElement</code> must not be <code>null</code>.
     * 
     * @param parent The parent of this <code>AbstractCompareItem</code>, or null if it is the
     *            root of a tree/structure.
     * @param content The referenced content. Must not be null.
     */
    public TableContentsCompareItem(AbstractCompareItem parent, IIpsElement content) {
        super(parent, content);
    }

    /**
     * A row is represented by its rownumber and all values of its colums separated by tabs. The number of
     * tabs needed to separate the values is calculated. This is necessary to align values of different rows 
     * to columns.
     * <p>
     * A tableContent item is represented by it name and the attribute "tableStructure".
     * <p>
     * No text is created for CompareItems that represent generations.
     * {@inheritDoc}
     */
    protected String initContentString() {
        StringBuffer sb= new StringBuffer();
        if(getIpsElement() instanceof IRow){
            IRow row = (IRow)getIpsElement();
            ITableContents table= (ITableContents) row.getIpsObject();
            int[] columnWidths= getColumnWidths();
            
            String rowNumber= row.getRowNumber()+COLON_BLANK;
            sb.append(rowNumber);
            sb.append(getNeededTabs(columnWidths[0], rowNumber));
            for (int colCounter = 0; colCounter < table.getNumOfColumns(); colCounter++) {
                String value= row.getValue(colCounter);
                sb.append(value);
                sb.append(getNeededTabs(columnWidths[colCounter+1], value));
            }
        }else if(getIpsElement() instanceof IIpsObjectGeneration){
            // for now do not display generation in text representation
//            IIpsObjectGeneration gen= (IIpsObjectGeneration) getIpsElement();
//            sb.append(TAB).append(Messages.ProductCmptCompareItem_Generation).append(COLON_BLANK);
//            sb.append(QUOTE).append(gen.getName()).append(QUOTE).append(NEWLINE);
//            sb.append(TAB).append(TAB).append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.GenerationEditDialog_labelValidFrom).append(BLANK);
//            sb.append(dateFormat.format(gen.getValidFrom().getTime()));
        }else if(getIpsElement() instanceof ITableContents){
            ITableContents table= (ITableContents) getIpsElement();
            sb.append(Messages.TableContentsCompareItem_TableContents).append(COLON_BLANK);
            sb.append(QUOTE).append(table.getName()).append(QUOTE).append(NEWLINE);
            sb.append(TAB).append(Messages.TableContentsCompareItem_TableStructure).append(COLON_BLANK).append(QUOTE).append(table.getTableStructure()).append(QUOTE);
        }else if(getIpsElement() instanceof IIpsSrcFile){
            sb.append(Messages.TableContentsCompareItem_SrcFile);
        }
        return sb.toString();
    }

    /**
     * Calculates the number of tabs needed to align the next value with its column.
     * @param widthInTabs The width of the column in tabs.
     * @param value The value of this table cell.
     * @return the number of tabs needed to reach the next column.
     */
    private StringBuffer getNeededTabs(int widthInTabs, String value) {
        StringBuffer sb= new StringBuffer();
        int neededTabs= widthInTabs - value.length()/TableContentsCompareViewer.TAB_WIDTH;
        for(int tabCounter=0; tabCounter<neededTabs; tabCounter++){
            sb.append(TAB);
        }
        return sb;
    }

    /**
     * {@inheritDoc}
     */
    protected String initName() {
        StringBuffer sb= new StringBuffer();
        if(getIpsElement() instanceof IRow){
            IRow row = (IRow)getIpsElement();
            sb.append(Messages.TableContentsCompareItem_Row).append(COLON_BLANK).append(row.getRowNumber());
        }else if(getIpsElement() instanceof IIpsObjectGeneration){
            IIpsObjectGeneration gen= (IIpsObjectGeneration) getIpsElement();
            sb.append(Messages.TableContentsCompareItem_Generation).append(COLON_BLANK).append(QUOTE).append(dateFormat.format(gen.getValidFrom().getTime())).append(QUOTE);
        }else if(getIpsElement() instanceof ITableContents){
            ITableContents table= (ITableContents) getIpsElement();
            sb.append(Messages.TableContentsCompareItem_TableContents).append(COLON_BLANK);
            sb.append(QUOTE).append(table.getName()).append(QUOTE);
        }else if(getIpsElement() instanceof IIpsSrcFile){
            sb.append(Messages.TableContentsCompareItem_SrcFile);
        }
        return sb.toString();
    }

    /**
     * Returns "ipstablecontents".
     * {@inheritDoc}
     */
    public String getType() {
        return "ipstablecontents"; //$NON-NLS-1$
    }
    
    /**
     * Initializes the columnwidth for all columns of all generations.
     * {@inheritDoc}
     */
    public void init() {
        if(isRoot()){
            if(hasChildren()){
                IIpsElement element= ((AbstractCompareItem)children.get(0)).getIpsElement();
                if(element instanceof ITableContents){
                    initColumnWidths((ITableContents)element);
                }
            }
        }
        super.init();
    }

    /**
     * Calculates the widths of all columns in tabs by searching for the longest (String) value in
     * the tablecontent. The columnwidth is at least one tab greater than needed to fit in the
     * string value (number of characters).
     * <p>
     * This calculation is based on the base tabWidth (number of characters a tab is displayed as)
     * of the displaying textViewer. The tabwidth is set by the <code>TableContentsCompareViewer</code>
     * and can be accessed as a static field. @see TableContentsCompareViewer#TAB_WIDTH
     * <p>
     * This method is called exactly once on the root TableContentsCompareItem.
     */ 
    private void initColumnWidths(ITableContents table) {
        columnWidthsInTabs = new int[table.getNumOfColumns()+1];
        IIpsObjectGeneration[] gens= table.getGenerations();
        for (int genCounter = 0; genCounter < gens.length; genCounter++) {
            IRow[] rows = ((ITableContentsGeneration)gens[genCounter]).getRows();
            for(int colCounter=0; colCounter<table.getNumOfColumns(); colCounter++){
                int maxWidth= 0;
                for (int rowCounter = 0; rowCounter < rows.length; rowCounter++) {
                    maxWidth= Math.max(maxWidth, rows[rowCounter].getValue(colCounter).length());
                }
                columnWidthsInTabs[colCounter+1]= getColumnTabWidthForLength(maxWidth);
            }
        }
        // calculate width of the rowNumber column
        String maxRowNumberString= String.valueOf(table.getNumOfColumns())+COLON_BLANK;
        columnWidthsInTabs[0]= getColumnTabWidthForLength(maxRowNumberString.length());
    }
    
    /**
     * Returns at least one tab more than needed to fit the given stringlength.
     * <p>
     * Example: tablength is 4
     * <ul>
     * <li>length is 7 -> returns 3 (2 tabs to fit 7 characters + 1 additional tab)</li>
     * <li>length is 8 -> returns 3 (2 tabs to fit 8 characters exactly + 1 additional tab)</li>
     * <li>length is 9 -> returns 4 (3 tabs to fit 9 characters + 1 additional tab)</li>
     * <ul>
     * @param length
     * @return
     */
    private int getColumnTabWidthForLength(int stringLength){
        int tabs= stringLength/TableContentsCompareViewer.TAB_WIDTH + 1;
        if(stringLength%TableContentsCompareViewer.TAB_WIDTH>0){
            tabs++;
        }
        return tabs;
    }
    
    /**
     * Returns an int array containing the widths of all columns in tabs. This includes the 
     * column of Rownumbers, that are part of the string representation of the IpsObject.
     * @return
     */
    public int[] getColumnWidths(){
        if(isRoot()){
            return columnWidthsInTabs;
        }else{
            return ((TableContentsCompareItem)getParent()).getColumnWidths();
        }
    }
}
