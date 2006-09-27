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

package org.faktorips.devtools.core.ui.team.compare;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.IDocumentRange;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.AllValuesValueSet;
import org.faktorips.devtools.core.internal.model.EnumValueSet;
import org.faktorips.devtools.core.internal.model.RangeValueSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;

/**
 * Treeitem used to create a tree/structure for representing a product component in the
 * ProductCmptCompareViewer. The IpsSrcFile, the product component, its generations and the
 * relations defined in each generation are represented by ProductCmptCompareItems. Instances of
 * this class are used to compare the contents (and thus structures) of a product component.
 * <p>
 * As product components are displayed in the compareViewer using a single text format, differences
 * between products are displayed as ranges in the text representation.
 * <p>
 * Each compare item contains a range that defines its position (offset) and length in this text
 * representation. The TextMergeViewer uses this information to display differences in a way similar
 * to the java source compare.
 * 
 * @see org.eclipse.compare.contentmergeviewer.IDocumentRange
 * @author Stefan Widmaier
 */
public class ProductCmptCompareItem implements IStreamContentAccessor, IStructureComparator, ITypedElement,
        IDocumentRange {
    private static final String COLON_BLANK = ": "; //$NON-NLS-1$
    private static final String BLANK = " "; //$NON-NLS-1$
    private static final String QUOTE = "\""; //$NON-NLS-1$
    private static final String NEWLINE = "\n"; //$NON-NLS-1$
    private static final String TAB = "\t"; //$NON-NLS-1$
    private static final String COMMA = ","; //$NON-NLS-1$
    private static final String DASH = "-"; //$NON-NLS-1$

    private DateFormat dateFormat = IpsPlugin.getDefault().getIpsPreferences().getValidFromFormat();
    private DateFormat simpleDateFormat = DateFormat.getDateInstance(SimpleDateFormat.MEDIUM);

    private ProductCmptCompareItem parent = null;
    private IIpsElement ipsElement = null;
    private List children = new ArrayList();
    private boolean isRoot = false;

    private Position range = new Position(0, 0);

    /**
     * String contents of this CompareItem (not including children). Used for comparing items by the
     * compare classes.
     */
    private String contentString;
    /**
     * Name of this compareItem. Used as Labels in the compare GUI (DiffNodes, Tree in
     * StructureDiffVIewer).
     */
    private String name;
    private Document document;

    /**
     * Creates a ProductCmptCompareItem with the given parent and the given content. If parent is
     * null this ProductCmptCompareItem is marked as a root element, as indicated by the method
     * isRoot().
     */
    public ProductCmptCompareItem(ProductCmptCompareItem parent, IIpsElement content) {
        Assert.isNotNull(content);
        this.ipsElement = content;
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        } else {
            isRoot = true;
        }
        contentString = initContentString();
        name = initName();
    }

    /**
     * Adds the given ProductCmptCompareItem to the list of children.
     * 
     * @param child
     */
    private void addChild(ProductCmptCompareItem child) {
        children.add(child);
    }

    /**
     * Returns null.
     * <p>
     * Used for by the compare-engine (Differencer) to guess the filecontents in order to open the
     * appropriate ContentMergeViewer. In case of product components the ContentMergeViewer is
     * identified using the file extension (ipsproduct).
     * <p>
     * For compare operations the compare class retrieves the contents of this item via the
     * StructureCreator.
     * 
     * @see ProductCmptCompareItemCreator#getContents(Object, boolean) {@inheritDoc}
     */
    public InputStream getContents() throws CoreException {
        return null;
    }

    /**
     * Returns a StringRepresentation of the tree (subtree respectively) this ProductCmptCompareItem
     * represents. The string is appended to the given <code>StringBuffer</code> and can be
     * retrieved by calling StringBuffer#toString().
     * <p>
     * This method also calculates the length of the string-representation of this CompareItem. The
     * string representation has a length equal to the contentString of this compareItem in addition
     * to the contentStrings of all its children. The given offset is the startingindex of the
     * contents of this element relative to the string-representaition of the
     * <code>IIpsSrcFile</code> and the contained <code>IProductCmpt</code>.
     * 
     */
    private int getTreeContentString(StringBuffer sb, int offset) {
        int currentLength = 0;
        sb.append(getContentString());
        currentLength += getContentString().length();
        if (hasChildren()) {
            sb.append(NEWLINE);
            currentLength += NEWLINE.length();
            if (ipsElement instanceof IIpsObjectGeneration) {
                List ces = getConfigElements(children);
                List rels = getRelations(children);
                if (!ces.isEmpty()) {
                    String attrHeader = getAttributeListHeader((IIpsObjectGeneration)ipsElement);
                    sb.append(attrHeader);
                    currentLength += attrHeader.length();
                    currentLength += iterateOverList(ces, sb, currentLength + offset);
                }
                if (!ces.isEmpty() && !rels.isEmpty()) {
                    sb.append(NEWLINE);
                    currentLength += NEWLINE.length();
                }
                if (!rels.isEmpty()) {
                    String relationsHeader = getRelationListHeader((IIpsObjectGeneration)ipsElement);
                    sb.append(relationsHeader);
                    currentLength += relationsHeader.length();
                    currentLength += iterateOverList(rels, sb, currentLength + offset);
                }
                range.setOffset(offset);
                range.setLength(currentLength);
                // do not include generation-separator in range
                sb.append(NEWLINE).append(DASH);
                currentLength += NEWLINE.length() + DASH.length();
            } else {
                currentLength += iterateOverList(children, sb, currentLength + offset);
                range.setOffset(offset);
                range.setLength(currentLength);
            }
        }
        return currentLength;
    }

    /**
     * Iterates over the given list of <code>ProductCmptCompareItem</code>s and calls
     * #getTreeContentString() on each of them without setting the range (offset and length) of this
     * <code>ProductCmptCompareItem</code>.
     * 
     * @return The length of the string-representation of the given list of
     *         <code>ProductCmptCompareItem</code>s combined. This also includes linebreakes
     *         between these items.
     */
    private int iterateOverList(List list, StringBuffer sb, int offset) {
        int currentLength = 0;
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            ProductCmptCompareItem child = (ProductCmptCompareItem)iter.next();
            currentLength += child.getTreeContentString(sb, currentLength + offset);
            if (iter.hasNext()) {
                sb.append(NEWLINE);
                currentLength += NEWLINE.length();
            }
        }
        return currentLength;
    }

    private String getAttributeListHeader(IIpsObjectGeneration gen) {
        StringBuffer sb = new StringBuffer();
        String attrString = org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductAttributesSection_attribute;
        String validFrom = simpleDateFormat.format(gen.getValidFrom().getTime());
        sb.append(validFrom).append(TAB).append(TAB).append(attrString).append(COLON_BLANK).append(NEWLINE);
        return sb.toString();
    }

    private String getRelationListHeader(IIpsObjectGeneration gen) {
        StringBuffer sb = new StringBuffer();
        String relString = org.faktorips.devtools.core.ui.editors.productcmpt.Messages.PropertiesPage_relations;
        String validFrom = simpleDateFormat.format(gen.getValidFrom().getTime());
        sb.append(validFrom).append(TAB).append(TAB).append(relString).append(COLON_BLANK).append(NEWLINE);
        return sb.toString();
    }

    /**
     * Returns a list containing all <code>ProductCmptCompareItem</code>s in the given list that
     * contain a <code>IConfigElement</code>.
     */
    private List getConfigElements(List elements) {
        List ces = new ArrayList();
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            ProductCmptCompareItem item = (ProductCmptCompareItem)iter.next();
            if (item.getIpsElement() instanceof IConfigElement) {
                ces.add(item);
            }
        }
        return ces;
    }

    /**
     * Returns a list containing all <code>ProductCmptCompareItem</code>s in the given list that
     * contain a <code>IProductComponentRelation</code>.
     */
    private List getRelations(List elements) {
        List rels = new ArrayList();
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            ProductCmptCompareItem item = (ProductCmptCompareItem)iter.next();
            if (item.getIpsElement() instanceof IProductCmptRelation) {
                rels.add(item);
            }
        }
        return rels;
    }

    /**
     * Returns the contents of this CompareItem as a string. This includes the type, name and other
     * attributes of the wrapped IpsElement. Childcontents are not included. The returned string is
     * used by the Differencer for comparing two CompareItems.
     * <p>
     * Only to be used at instanciation.
     * <p>
     * For every generation as well as its configelements and relations the validFrom date is
     * prepended to every line of the text representation. The format of the date prefix is not
     * necessaryly equal to the faktorips format, instead SimpleDateFormat.MEDIUM is used. The
     * prepended date is needed for the rangedifferencing (calculation of differences based on the
     * text representation) that is performed by the <code>TextMergeViewer</code> internally.
     */
    private String initContentString() {
        StringBuffer sb = new StringBuffer();
        if (ipsElement != null) {
            if (ipsElement instanceof IProductCmptRelation) {
                IProductCmptRelation rel = (IProductCmptRelation)ipsElement;
                String validFromSimple = simpleDateFormat.format(rel.getProductCmptGeneration().getValidFrom()
                        .getTime());
                sb.append(validFromSimple).append(TAB).append(TAB).append(TAB).append(rel.getTarget());
            } else if (ipsElement instanceof IConfigElement) {
                IConfigElement ce = (IConfigElement)ipsElement;
                String validFromSimple = simpleDateFormat
                        .format(ce.getProductCmptGeneration().getValidFrom().getTime());
                sb.append(validFromSimple).append(TAB).append(TAB).append(TAB).append(ce.getName()).append(NEWLINE);
                sb.append(validFromSimple).append(TAB).append(TAB).append(TAB).append(TAB)
                        .append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.PolicyAttributeEditDialog_defaultValue)
                        .append(BLANK);
                sb.append(ce.getValue()).append(NEWLINE);
                sb.append(validFromSimple).append(TAB).append(TAB).append(TAB).append(TAB)
                        .append(Messages.ProductCmptCompareItem_ValueSet).append(COLON_BLANK);
                sb.append(getValueSetContent(ce));
            } else if (ipsElement instanceof IIpsObjectGeneration) {
                IIpsObjectGeneration gen = (IIpsObjectGeneration)ipsElement;
                String validFromSimple = simpleDateFormat.format(gen.getValidFrom().getTime());
                sb.append(validFromSimple).append(TAB).append(Messages.ProductCmptCompareItem_Generation).append(COLON_BLANK);
                sb.append(gen.getName()).append(NEWLINE);
                sb.append(validFromSimple).append(TAB).append(TAB)
                        .append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.GenerationEditDialog_labelValidFrom)
                        .append(BLANK);
                sb.append(dateFormat.format(gen.getValidFrom().getTime()));
            } else if (ipsElement instanceof IProductCmpt) {
                IProductCmpt product = (IProductCmpt)ipsElement;
                sb.append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductCmptEditor_productComponent);
                sb.append(QUOTE).append(product.getName()).append(QUOTE).append(NEWLINE);
                sb.append(TAB).append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductAttributesSection_attribute)
                        .append(COLON_BLANK).append(NEWLINE);
                sb.append(TAB).append(TAB).append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductAttributesSection_template)
                        .append(COLON_BLANK);
                sb.append(QUOTE).append(product.getPolicyCmptType()).append(QUOTE).append(NEWLINE);
                sb.append(TAB).append(TAB)
                        .append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductAttributesSection_labelRuntimeId)
                        .append(COLON_BLANK);
                sb.append(QUOTE).append(product.getRuntimeId()).append(QUOTE);
            } else if (ipsElement instanceof IIpsSrcFile) {
                sb.append(Messages.ProductCmptCompareItem_SourceFile);
            }
        }
        return sb.toString();
    }

    /**
     * Returns a string representation of a <code>ValueSet</code>. An <code>EnumValueSet</code>
     * is represented as a list of values separated by comma ("[1,5,7]"). A
     * <code>RangeValueSet</code> is represented by its upper and lower bound ("[1..7]"). A
     * <code>AllValuesValueSet</code> is represented by "<all values>".
     */
    private StringBuffer getValueSetContent(IConfigElement ce) {
        StringBuffer sb = new StringBuffer();
        IValueSet set = ce.getValueSet();
        if (set instanceof EnumValueSet) {
            sb.append("["); //$NON-NLS-1$
            String[] values = ((EnumValueSet)set).getValues();
            for (int i = 0; i < values.length; i++) {
                sb.append(values[i]);
                if (i < values.length - 1) {
                    sb.append(COMMA);
                }
            }
            sb.append("]"); //$NON-NLS-1$
        } else if (set instanceof RangeValueSet) {
            RangeValueSet rangeSet = (RangeValueSet)set;
            sb.append("["); //$NON-NLS-1$
            sb.append(rangeSet.getUpperBound());
            sb.append(".."); //$NON-NLS-1$
            sb.append(rangeSet.getLowerBound());
            sb.append("]"); //$NON-NLS-1$
        } else if (set instanceof AllValuesValueSet) {
            sb.append(Messages.ProductCmptCompareItem_AllValues);
        }
        return sb;
    }

    /**
     * Returns a String representation of this ProductCmptCompareItem including its attributes but
     * <em>not</em> including its children. For a content string including children the document
     * must be retrieved.
     * 
     * @see #getDocument()
     */
    public String getContentString() {
        return contentString;
    }

    /**
     * Returns the name of this CompareItem. Includes type and name of the wrapped IpsElement but
     * not its children.
     * <p>
     * The returned string is used as text for labels in structurecompare and headers of textfields.
     * <p>
     * Only to be used at instanciation.
     */
    private String initName() {
        StringBuffer sb = new StringBuffer();
        if (ipsElement != null) {
            if (ipsElement instanceof IProductCmptRelation) {
                IProductCmptRelation rel = (IProductCmptRelation)ipsElement;
                sb.append(Messages.ProductCmptCompareItem_Relation).append(COLON_BLANK).append(QUOTE).append(
                        rel.getName()).append(QUOTE);
            } else if (ipsElement instanceof IConfigElement) {
                IConfigElement ce = (IConfigElement)ipsElement;
                sb.append(Messages.ProductCmptCompareItem_Attribute).append(COLON_BLANK).append(QUOTE).append(
                        ce.getName()).append(QUOTE);
            } else if (ipsElement instanceof IIpsObjectGeneration) {
                IIpsObjectGeneration gen = (IIpsObjectGeneration)ipsElement;
                sb.append(Messages.ProductCmptCompareItem_Generation).append(COLON_BLANK).append(QUOTE).append(
                        gen.getName()).append(QUOTE);
            } else if (ipsElement instanceof IProductCmpt) {
                IProductCmpt product = (IProductCmpt)ipsElement;
                sb.append(
                        org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductCmptEditor_productComponent)
                        .append(QUOTE).append(product.getName()).append(QUOTE);
            } else if (ipsElement instanceof IIpsSrcFile) {
                IIpsSrcFile srcFile = (IIpsSrcFile)ipsElement;
                IFile file = srcFile.getCorrespondingFile();
                if (file != null) {
                    sb.append(Messages.ProductCmptCompareItem_SourceFile).append(COLON_BLANK).append(QUOTE).append(
                            file.getName()).append(QUOTE);
                } else {
                    sb.append(Messages.ProductCmptCompareItem_SourceFile);
                }
            }
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns wether this ProductCmptCompareItem has children.
     */
    private boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getChildren() {
        return children.toArray();
    }

    /**
     * Returns true if this compareitem and the given compareitem are equal in name,
     * policycomponenttype and runtime id. Children are not included in the compare. {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (o instanceof ProductCmptCompareItem) {
            if (isRoot() && ((ProductCmptCompareItem)o).isRoot()) {
                return true;
            }
            ProductCmptCompareItem pi = (ProductCmptCompareItem)o;
            return getContentString().equals(pi.getContentString());
        }
        return false;
    }

    public int hashCode() {
        return getContentString().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        if (ipsElement != null) {
            return ipsElement.getImage();
        }
        return null;
    }

    /**
     * Returns "ipsproduct". {@inheritDoc}
     */
    public String getType() {
        return "ipsproduct"; //$NON-NLS-1$
    }

    /**
     * Returns true if this <code>ProductCmptCompareItem</code> is the root of a tree, false
     * otherwise. A ProductCmptCompareItem is a root if it does not have a parent.
     * 
     * @return
     */
    public boolean isRoot() {
        return isRoot;
    }

    /**
     * Returns the parent of this <code>ProductCmptCompareItem</code>, <code>null</code> if
     * this <code>ProductCmptCompareItem</code> is the root of a tree.
     * 
     * @return
     */
    public ProductCmptCompareItem getParent() {
        return parent;
    }

    public IIpsElement getIpsElement() {
        return ipsElement;
    }

    /**
     * Returns the document the tree of all CompareItems represents. This Document is used when
     * displaying a files contents in the TextMergeViewer.
     * <p>
     * For compare operations the compare class retrieves the contents of this item via the
     * StructureCreator.
     * 
     * @see ProductCmptCompareItemCreator#getContents(Object, boolean)
     */
    public IDocument getDocument() {
        if (isRoot()) {
            return document;
        } else {
            // return new Document(contentString);
            return getParent().getDocument();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Position getRange() {
        return range;
    }

    /**
     * Sorts the list of children as in sortChildren(). Inits the range for all
     * ProductCmptCompareItems that are descendants of this CompareItem. Additionally the document
     * for this CompareItem, as returned by getDocument(), is initialized.
     * <p>
     * initDocumentRange() has effect only if this CompareItem is the root of its structure.
     * 
     */
    public void initDocumentRange() {
        if (isRoot()) {
            sortChildren();
            StringBuffer sb = new StringBuffer();
            getTreeContentString(sb, 0);
            document = new Document(sb.toString());
        }
    }

    /**
     * Sorts the list of child <code>ProductCmptCompareItem</code>s using the 
     * <code>ProductCmptCompareItemComparator</code>.
     * @see ProductCmptCompareItemComparator
     */
    private void sortChildren() {
        Collections.sort(children, new ProductCmptCompareItemComparator());
        for (Iterator iter = children.iterator(); iter.hasNext();) {
            ProductCmptCompareItem element = (ProductCmptCompareItem)iter.next();
            element.sortChildren();
        }
    }

}
