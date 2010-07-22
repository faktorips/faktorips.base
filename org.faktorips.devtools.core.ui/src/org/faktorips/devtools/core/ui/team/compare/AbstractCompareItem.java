/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.team.compare;

import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.IDocumentRange;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.compare.structuremergeviewer.StructureDiffViewer;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.team.compare.productcmpt.ProductCmptCompareItemCreator;

/**
 * Treeitem used to create a tree/structure for representing an <code>IIpsObject</code> in the
 * ProductCmptCompareViewer (more specifically the {@link StructureDiffViewer}). The
 * <code>IpsSrcFile</code> , the <code>IIpsObject</code> , its generations,
 * <code>IIpsObjectParts</code> (relations and configelements or rows) are represented by
 * <code>AbstractCompareItem</code>. Instances of this class are used to compare the structures of
 * an <code>IIpsObject</code>.
 * <p>
 * Since <code>IIpsObject</code> are displayed in the compareViewer using a simple text format,
 * differences between objects are displayed as ranges in the text representation. The
 * <code>AbstractCompareItem</code> class therefore implements the {@link IDocumentRange} Interface.
 * It lets the TextMergeViewer retrieve the document corresponding to this product component and the
 * (text-)range the compareitem represents in the document. The TextMergeViewer uses this
 * information to display differences in a way similar to the java source compare.
 * 
 * @see org.eclipse.compare.contentmergeviewer.IDocumentRange
 * @author Stefan Widmaier, Faktor Zehn AG
 */
public abstract class AbstractCompareItem implements IStreamContentAccessor, IStructureComparator, ITypedElement,
        IDocumentRange {
    protected static final String COLON_BLANK = ": "; //$NON-NLS-1$
    protected static final String COLON = ":"; //$NON-NLS-1$
    protected static final String BLANK = " "; //$NON-NLS-1$
    protected static final String QUOTE = "\""; //$NON-NLS-1$
    protected static final String NEWLINE = "\n"; //$NON-NLS-1$
    protected static final String TAB = "\t"; //$NON-NLS-1$
    protected static final String COMMA = ","; //$NON-NLS-1$
    protected static final String DASH = "-"; //$NON-NLS-1$

    /**
     * String that contains the word "generation", localized and/or configured by the
     * <code>ChangesOverTimeNamingConvention</code>. Since <code>AbstractCompareItem</code>s do not
     * change over time, an initialization in the constructor is sufficient.
     */
    protected String changingNamingConventionGenerationString;

    /**
     * The parent of this <code>AbstractCompareItem</code>. May be null if this CompareItem is root.
     */
    private AbstractCompareItem parent = null;
    /**
     * The referenced <code>IIpsElement</code>.
     */
    private IIpsElement ipsElement = null;
    /**
     * Boolean that indicates if this <code>AbstractCompareItem</code> is root of a structure. True
     * if the parent of this <code>AbstractCompareItem</code> is null, false otherwise.
     */
    private boolean isRoot = false;

    /**
     * The list of children of this <code>AbstractCompareItem</code>. This list is empty if this
     * compare-item is a leaf in its tree.
     */
    protected List<AbstractCompareItem> children = new ArrayList<AbstractCompareItem>();

    /**
     * The range this <code>AbstractCompareItem</code>'s string representation inhabits in the
     * complete document (whole IpsObject).
     * <p>
     * The concrete value is initialized in the {@link #init()} method.
     * 
     * @see #init()
     * @see #initTreeContentString(StringBuffer, int)
     */
    protected Position range = new Position(0, 0);

    /**
     * {@link DateFormat} used to convert the validFrom date of generations into a String.
     */
    protected DateFormat dateFormat = IpsPlugin.getDefault().getIpsPreferences().getDateFormat();

    /**
     * If this compareitem is the root of its structure, this document contains the string
     * representation of the <code>IIpsSrcfile</code> and the containes <code>IIpsObject</code>.
     */
    private Document document;

    /**
     * String contents of this CompareItem (not including children). Used for displaying items in
     * the compare viewer.
     */
    private String contentString;

    /**
     * String contents of this CompareItem (not including children) without whitespace. Used for
     * comparing items by the compare-framework classes.
     */
    private String contentStringWithoutWhiteSpace;

    /**
     * Name of this compareItem. Used as Labels in the compare GUI (DiffNodes, Tree in
     * StructureDiffVIewer).
     */
    private String name;

    /**
     * Creates an <code>AbstractCompareItem</code> using the given parent and
     * <code>IIpsElement</code>. If the given parent is null, this <code>AbstractCompareItem</code>
     * is marked as a root element, as indicated by the method isRoot(). The given
     * <code>IIpsElement</code> must not be <code>null</code>.
     * 
     * @param parent The parent of this <code>AbstractCompareItem</code>, or null if it is the root
     *            of a tree/structure.
     * @param content The referenced content. Must not be null.
     */
    public AbstractCompareItem(AbstractCompareItem parent, IIpsElement content) {
        Assert.isNotNull(content);
        ipsElement = content;
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        } else {
            isRoot = true;
        }
        changingNamingConventionGenerationString = IpsPlugin.getDefault().getIpsPreferences()
                .getChangesOverTimeNamingConvention().getGenerationConceptNameSingular();
    }

    /**
     * Returns a StringRepresentation of the tree (subtree respectively) this CompareItem
     * represents. The string is appended to the given <code>StringBuffer</code>. The
     * <code>StringBuffer</code> is the used as argument for the recursive method call.
     * <p>
     * This method also calculates the length of the string-representation of this CompareItem. The
     * string representation has a length equal to the contentString of this compareItem in addition
     * to the contentStrings of all its children. The given offset is the starting-index of the
     * contents of this element relative to the string-representation of the
     * <code>IIpsSrcFile</code> and the contained <code>IIpsObject</code>.
     */
    protected int initTreeContentString(StringBuffer sb, int offset) {
        int currentLength = 0;
        sb.append(getContentString());
        currentLength += getContentString().length();
        for (AbstractCompareItem item : children) {
            sb.append(NEWLINE);
            currentLength += NEWLINE.length();
            currentLength += item.initTreeContentString(sb, currentLength + offset);
            if (item.needsTextSeparator()) {
                sb.append(NEWLINE).append(DASH);
                currentLength += NEWLINE.length() + DASH.length();
            }
        }
        setRange(offset, currentLength);
        return currentLength;
    }

    /**
     * Returns true if the given CompareItem contains an <code>IIpsElement</code> of type
     * <code>IIpsObjectGeneration</code>, false otherwise.
     * 
     */
    protected boolean needsTextSeparator() {
        return getIpsElement() instanceof IIpsObjectGeneration;
    }

    /**
     * Returns the contents of this CompareItem as a string. This includes the type, name and other
     * attributes of the wrapped IpsElement. Childcontents are not included.
     * <p>
     * Only to be used at instanciation.
     */
    protected abstract String initContentString();

    /**
     * Returns the name of this CompareItem. Includes type and name of the wrapped IpsElement but
     * not its children.
     * <p>
     * The returned string is used as text for labels in the structurecompare tree and headers of
     * textviewers.
     * <p>
     * Only to be used at instanciation.
     */
    protected abstract String initName();

    /**
     * Adds the given ProductCmptCompareItem to the list of children.
     * 
     */
    private void addChild(AbstractCompareItem child) {
        children.add(child);
    }

    /**
     * This method is <em>not</em> called when comparing <code>AbstractCompareItem</code>s. The
     * standard implementation of the <code>Differencer</code> calls this method to compare the
     * leafs of structures by their content. The <code>StructureDiffViewer</code> (which is used in
     * this compare viewer) subclasses <code>Differencer</code> and lets it use the
     * <code>AbstractCompareItemCreator</code> method <code>getContents()</code> for comparing
     * contents.
     * <p>
     * 
     * @return null.
     * 
     * @see ProductCmptCompareItemCreator#getContents(Object, boolean)
     */
    @Override
    public InputStream getContents() throws CoreException {
        return null;
    }

    /**
     * Returns a String representation of this <code>AbstractCompareItem</code> including its
     * attributes but <em>not</em> including its children. For a content string including children
     * the document must be retrieved.
     * 
     * @see #getDocument()
     */
    public String getContentString() {
        return contentString;
    }

    /**
     * Returns the contentString of this <code>AbstractCompareItem</code> without its whitespace.
     * 
     * @see #getContentString()
     */
    public String getContentStringWithoutWhiteSpace() {
        return contentStringWithoutWhiteSpace;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] getChildren() {
        return children.toArray();
    }

    /**
     * Returns whether this ProductCmptCompareItem has children.
     */
    protected boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Returns true if this compareitem and the given compareitem are equal in name,
     * policycomponenttype and runtime id. Children are not included in the compare. {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof AbstractCompareItem) {
            if (isRoot() && ((AbstractCompareItem)o).isRoot()) {
                return true;
            }
            AbstractCompareItem aci = (AbstractCompareItem)o;
            return getContentStringWithoutWhiteSpace().equals(aci.getContentStringWithoutWhiteSpace());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getContentStringWithoutWhiteSpace().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Image getImage() {
        if (ipsElement != null) {
            return IpsUIPlugin.getImageHandling().getImage(ipsElement);
        }
        return null;
    }

    /**
     * Returns true if this <code>ProductCmptCompareItem</code> is the root of a tree, false
     * otherwise. A ProductCmptCompareItem is a root if it does not have a parent.
     * 
     */
    public boolean isRoot() {
        return isRoot;
    }

    /**
     * Returns the parent of this <code>ProductCmptCompareItem</code>, <code>null</code> if this
     * <code>ProductCmptCompareItem</code> is the root of a tree.
     * 
     */
    public AbstractCompareItem getParent() {
        return parent;
    }

    /**
     * Returns the <code>IIpsElement</code> this <code>AbstractCompareItem</code> references.
     * 
     * @return the referenced <code>IIpsElement</code>.
     */
    public IIpsElement getIpsElement() {
        return ipsElement;
    }

    /**
     * Returns the document the tree of CompareItems represents. This document is used only for
     * displaying a files' contents in the CompareViewer.
     * 
     * @see ProductCmptCompareItemCreator#getContents(Object, boolean)
     */
    @Override
    public IDocument getDocument() {
        if (isRoot()) {
            return document;
        } else {
            return getParent().getDocument();
        }
    }

    /**
     * Returns the text-range this compareitem inhabits. Creates a defensive copy.
     */
    @Override
    public Position getRange() {
        return new Position(range.getOffset(), range.getLength());
    }

    /**
     * Sets the range (offset and length) of this compareItem's textrepresentation relative to the
     * document (srcFile).
     */
    protected void setRange(int offset, int length) {
        range.setOffset(offset);
        range.setLength(length);
    }

    /**
     * For all CompareItems the name and content strings (with and without whitespace) are
     * initialized.
     * <p>
     * If this CompareItem is the root of its structure the following commands are performed:
     * Sorting the list of children as in sortChildren(). Initializing the range for this
     * CompareItem and for all its children. Initializing the document for this CompareItem, as
     * returned by getDocument().
     */
    public void init() {
        if (isRoot()) {
            initStrings();
            StringBuffer sb = new StringBuffer();
            initTreeContentString(sb, 0);
            document = new Document(sb.toString());
        }
    }

    /**
     * Inits the name, the contentString, and the contentStringWithoutWhitespace field of this
     * compare item and calls this method recursively on all its children.
     * 
     */
    private void initStrings() {
        contentString = initContentString();
        contentStringWithoutWhiteSpace = StringUtils.deleteWhitespace(contentString);
        name = initName();
        for (AbstractCompareItem item : children) {
            item.initStrings();
        }
    }

}
