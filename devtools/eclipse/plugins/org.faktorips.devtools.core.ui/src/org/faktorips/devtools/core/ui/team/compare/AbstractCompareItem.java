/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare;

import java.io.ByteArrayInputStream;
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
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.team.compare.productcmpt.ProductCmptCompareItemCreator;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;

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
 */
public abstract class AbstractCompareItem
        implements IStreamContentAccessor, IStructureComparator, ITypedElement, IDocumentRange {

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
    private final String changingNamingConventionGenerationString;

    /**
     * The parent of this <code>AbstractCompareItem</code>. May be null if this CompareItem is root.
     */
    private final AbstractCompareItem parent;
    /**
     * The referenced <code>IIpsElement</code>.
     */
    private final IIpsElement ipsElement;
    /**
     * Boolean that indicates if this <code>AbstractCompareItem</code> is root of a structure. True
     * if the parent of this <code>AbstractCompareItem</code> is null, false otherwise.
     */
    private boolean isRoot = false;

    /**
     * The list of children of this <code>AbstractCompareItem</code>. This list is empty if this
     * compare-item is a leaf in its tree.
     */
    private final List<AbstractCompareItem> childItems = new ArrayList<>();

    /**
     * The range this <code>AbstractCompareItem</code>'s string representation inhabits in the
     * complete document (whole IpsObject).
     * <p>
     * The concrete value is initialized in the {@link #init()} method.
     * 
     * @see #init()
     * @see #initTreeContentString(StringBuilder, int)
     */
    private final Position range = new Position(0, 0);

    /**
     * {@link DateFormat} used to convert the validFrom date of generations into a String.
     */
    private final DateFormat dateFormat = IpsPlugin.getDefault().getIpsPreferences().getDateFormat();

    /**
     * If this compare item is the root of its structure, this document contains the string
     * representation of the <code>IIpsSrcfile</code> and the contains <code>IIpsObject</code>.
     */
    private IDocument document;

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
     * represents. The string is appended to the given <code>StringBuilder</code>. The
     * <code>StringBuilder</code> is the used as argument for the recursive method call.
     * <p>
     * This method also calculates the length of the string-representation of this CompareItem. The
     * string representation has a length equal to the contentString of this compareItem in addition
     * to the contentStrings of all its children. The given offset is the starting-index of the
     * contents of this element relative to the string-representation of the
     * <code>IIpsSrcFile</code> and the contained <code>IIpsObject</code>.
     */
    protected int initTreeContentString(StringBuilder sb, int offset) {
        int startIndex = sb.length();
        sb.append(getContentString());
        for (AbstractCompareItem item : getChildItems()) {
            sb.append(NEWLINE);
            item.initTreeContentString(sb, sb.length() - startIndex + offset);
            if (item.needsTextSeparator()) {
                sb.append(NEWLINE).append(DASH);
            }
        }
        setRange(offset, sb.length() - startIndex);
        return sb.length() - startIndex;
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
     * attributes of the wrapped IpsElement. Child contents are not included.
     * <p>
     * Only to be used at instantiation.
     */
    protected abstract String initContentString();

    /**
     * Returns the name of this CompareItem. Includes type and name of the wrapped IpsElement but
     * not its children.
     * <p>
     * The returned string is used as text for labels in the structure compare tree and headers of
     * textviewers.
     * <p>
     * Only to be used at instantiation.
     */
    protected abstract String initName();

    /**
     * Adds the given ProductCmptCompareItem to the list of children.
     * 
     */
    private void addChild(AbstractCompareItem child) {
        getChildItems().add(child);
    }

    /**
     * This method and the implementation of the interface {@link IStreamContentAccessor} should not
     * be necessary because the content is received by {@link #getDocument()}. Because of a bug in
     * eclipse 3.5 (and still 3.6 and 3.7) https://bugs.eclipse.org/bugs/show_bug.cgi?id=293926 we
     * need to implement {@link IStreamContentAccessor} and return a non null value here.
     */
    @Override
    public InputStream getContents() {
        return new ByteArrayInputStream(new byte[0]);
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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object[] getChildren() {
        return getChildItems().toArray();
    }

    /**
     * Returns whether this ProductCmptCompareItem has children.
     */
    protected boolean hasChildren() {
        return !getChildItems().isEmpty();
    }

    /**
     * Equals if this and other compare item represents the same structural item.
     * 
     * @see IStructureComparator#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        AbstractCompareItem other = (AbstractCompareItem)obj;
        if (ipsElement == null) {
            if (other.ipsElement != null) {
                return false;
            }
        }
        return isEqualIpsElementInStructure(ipsElement, other.ipsElement);
    }

    private boolean isEqualIpsElementInStructure(IIpsElement element1, IIpsElement element2) {
        if (element1 == element2) {
            return true;
        } else if ((element1 == null || element2 == null)
                || (element1.getClass() != element2.getClass())) {
            return false;
        } else if (element1 instanceof IIpsObject) {
            IIpsObject object1 = (IIpsObject)element1;
            IIpsObject object2 = (IIpsObject)element2;
            // only compare type to allow compares of two different ips objects with each other
            return object1.getIpsObjectType().equals(object2.getIpsObjectType());
        } else if (element1 instanceof IIpsObjectPart) {
            IIpsObjectPart part1 = (IIpsObjectPart)element1;
            IIpsObjectPart part2 = (IIpsObjectPart)element2;
            return isEqualIpsElementInStructure(part1.getParent(), part2.getParent())
                    && isEqualIpsObjectPart(part1, part2);
        } else {
            return element1.getEnclosingResource().equals(element2.getEnclosingResource());
        }

    }

    protected boolean isEqualIpsObjectPart(IIpsObjectPart part1, IIpsObjectPart part2) {
        return part1.getName().equals(part2.getName());
    }

    /**
     * If the compared element is an {@link IIpsObjectPart}, that part's name's hash code.<br>
     * If the compared element is an {@link IIpsObject}, that object's type's hash code.<br>
     * Else 0, to compare items solely by their content.
     */
    @Override
    public int hashCode() {
        if (ipsElement instanceof IIpsObjectPart) {
            IIpsObjectPart part = (IIpsObjectPart)ipsElement;
            return part.getName().hashCode();
        } else if (ipsElement instanceof IIpsObject) {
            IIpsObject ipsObject = (IIpsObject)ipsElement;
            return ipsObject.getIpsObjectType().hashCode();
        } else {
            return 0;
        }
    }

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
            StringBuilder sb = new StringBuilder();
            initTreeContentString(sb, 0);
            document = new Document(sb.toString());
        }
    }

    /**
     * Initializes the name, the contentString, and the contentStringWithoutWhitespace field of this
     * compare item and calls this method recursively on all its children.
     * 
     */
    private void initStrings() {
        contentString = initContentString();
        contentStringWithoutWhiteSpace = initContentStringWithoutWhiteSpace();
        name = initName();
        for (AbstractCompareItem item : getChildItems()) {
            item.initStrings();
        }
    }

    protected String initContentStringWithoutWhiteSpace() {
        return StringUtils.deleteWhitespace(contentString);
    }

    protected String getChangingNamingConventionGenerationString() {
        return changingNamingConventionGenerationString;
    }

    protected List<AbstractCompareItem> getChildItems() {
        return childItems;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }

}
