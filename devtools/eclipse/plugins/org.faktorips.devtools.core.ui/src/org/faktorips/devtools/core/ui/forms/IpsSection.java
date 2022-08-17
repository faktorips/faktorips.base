/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.forms;

import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.ui.IDataChangeableReadAccessWithListenerSupport;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.IDataChangeableStateChangeListener;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.IpsWorkspacePreferences;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;

/**
 * A section is an area of the user interface. The <code>IpsSection</code> is a composite that acts
 * as a wrapper for a SWT section.
 */
public abstract class IpsSection extends Composite implements IDataChangeableReadWriteAccess,
        IDataChangeableReadAccessWithListenerSupport, DisposeListener {

    private final BindingContext bindingContext = new BindingContext();

    private final String id;

    private final boolean collapsible;

    /** The SWT style (for example {@link Section#TITLE_BAR}) */
    private final int style;

    /** SWT info how to layout the data (for example GridData.FILL_BOTH) */
    private final int layoutData;

    /** The UI toolkit to create new UI elements with */
    private final UIToolkit toolkit;

    /** Flag indicating whether the content of this section can be changed by the user */
    private boolean changeable = true;

    /** The UI control for the section */
    private Section section;

    /**
     * Flag indicating how much space this section will request from its parent. If
     * <code>true</code> this section requests as much space as possible (default). If
     * <code>false</code> this section will only request as much space as needed to display its
     * contents.
     */
    private boolean isGrabVerticalSpace = true;

    /**
     * Configures this {@link IpsSection} to be initialized in collapsed state if no content is
     * available ({@link #hasContentToDisplay()}). The default is <code>false</code>.
     * <p>
     * If this flag is set to <code>true</code>, this section must provide its title by overriding
     * {@link #getSectionTitle()}.
     */
    private boolean isInitCollapsedIfNoContent = false;

    private CopyOnWriteArrayList<IDataChangeableStateChangeListener> dataChangeableStateChangeListeners;

    private Composite clientComposite;

    private final IpsWorkspacePreferences preferences = new IpsWorkspacePreferences();

    /**
     * Creates a new {@link IpsSection} with the style
     * <code>ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE</code>.
     * 
     * @param parent The parent UI composite
     * @param layoutData How to layout the data within the section
     * @param toolkit The UI toolkit to create new UI elements with
     */
    protected IpsSection(String id, Composite parent, int layoutData, UIToolkit toolkit) {
        super(parent, SWT.NONE);
        this.id = id;
        collapsible = true;

        style = ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE;
        this.layoutData = layoutData;
        this.toolkit = toolkit;

        addDisposeListener(this);
    }

    /**
     * @param parent The parent UI composite
     * @param style The section style to use, see {@link Section}
     * @param layoutData How to layout the data within the section
     * @param toolkit The UI toolkit to create new UI elements with
     */
    protected IpsSection(Composite parent, int style, int layoutData, UIToolkit toolkit) {
        super(parent, SWT.NONE);
        id = ""; //$NON-NLS-1$
        collapsible = false;

        this.style = style;
        this.layoutData = layoutData;
        this.toolkit = toolkit;

        addDisposeListener(this);
    }

    /**
     * Constructs the section's controls.
     * <p>
     * This has to to be called explicitly by subclasses <strong>after</strong> they have
     * initialized any subclass specific instance variables.
     * <p>
     * The <code>IpsSection</code> does not call this method in it's constructor, because in
     * subclasses in might be necessary to initialize instance variable first, but the call to the
     * super constructor has to be the first statement in the subclass constructor.
     */
    protected void initControls() {
        // Adapt this section to the form toolkit
        toolkit.getFormToolkit().adapt(this);

        // Set layout
        setLayoutData(new GridData(layoutData));
        setLayout(toolkit.createNoMarginGridLayout(1, false));

        // Create the UI section widget that is being wrapped by this composite
        section = toolkit.getFormToolkit().createSection(this, style);
        section.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

        // Create the client composite for the section
        clientComposite = toolkit.createGridComposite(section, 1, false, false);
        clientComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        initClientComposite(clientComposite, toolkit);
        section.setClient(clientComposite);
        toolkit.getFormToolkit().paintBordersFor(clientComposite);
        updateSectionTitle();
        if (collapsible) {
            initExpandedState();
            if (!hasContentToDisplay() && isInitCollapsedIfNoContent()) {
                setExpanded(false);
            }
            addStorePreferenceExpansionListener();
            relayoutSection(isExpanded());
        }

        createToolBar();
        /*
         * Required due to FIPS-4600
         */
        getBindingContext().updateUI();
    }

    private void createToolBar() {
        ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
        ToolBar toolBar = toolBarManager.createControl(getSectionControl());

        populateToolBar(toolBarManager);

        toolBarManager.update(false);
        // Aligns the tool bar to the right
        section.setTextClient(toolBar);
    }

    /**
     * Subclasses are supposed to override this method if they desire to populate this section's
     * toolbar.
     * <p>
     * The default implementation does nothing.
     * 
     * @param toolBarManager the {@link IToolBarManager} to add {@link IAction}s or
     *            {@link IContributionItem}s to
     */
    protected void populateToolBar(IToolBarManager toolBarManager) {
        // Empty default implementation
    }

    /**
     * Updates the section's title to reflect the current state.
     * <p>
     * <strong>Subclassing:</strong><br>
     * This implementation calls {@link #getSectionTitle()} to obtain the current title string.
     * Then, if both {@link #isExpanded()} and {@link #hasContentToDisplay()} return false, the
     * {@link Messages#IpsSection_EmptyTitleExtension} string is appended to the title string.
     * Finally, {@link #setText(String)} is invoked to set the section title.
     */
    protected final void updateSectionTitle() {
        String newTitle = getSectionTitle();
        if (!isExpanded() && !hasContentToDisplay()) {
            newTitle += ' ' + Messages.IpsSection_EmptyTitleExtension;
        }
        setText(newTitle);
    }

    /**
     * Returns the title text that is displayed in the section header. Subclasses may override or
     * call {@link #setText(String)} directly.
     * 
     * @return this sections title text.
     */
    protected String getSectionTitle() {
        return ""; //$NON-NLS-1$
    }

    /**
     * A Subclass should return <code>true</code> if it has content that needs to be displayed,
     * <code>false</code> else. The Section will then be initialized with a "(empty)" annotation in
     * collapsed state.
     * <p>
     * The default implementation returns <code>true</code>.
     * 
     * @return <code>true</code>
     */
    protected boolean hasContentToDisplay() {
        return true;
    }

    /**
     * Initializes the expanded state from the preferences.
     */
    protected void initExpandedState() {
        boolean expanded = preferences.getBoolean(id + IpsUIPlugin.PREFERENCE_ID_SUFFIX_SECTION_EXPANDED, true);
        setExpanded(expanded);
    }

    /**
     * Stores the expanded state in the plug-in preferences as soon as the user changes the expanded
     * state.
     */
    private void addStorePreferenceExpansionListener() {
        getSectionControl().addExpansionListener(new IExpansionListener() {
            @Override
            public void expansionStateChanging(ExpansionEvent e) {
                // Nothing to do
            }

            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                boolean expanded = e.getState();
                preferences.putBoolean(id + IpsUIPlugin.PREFERENCE_ID_SUFFIX_SECTION_EXPANDED, expanded);
                relayoutSection(expanded);
            }

        });
    }

    /**
     * Expands or collapses this section. Re-layouts the editor page. The layout-behavior of this
     * section can be configured using {@link #setGrabVerticalSpace(boolean)}.
     * 
     * @param expanded whether or not this section should be expanded.
     */
    public final void setExpanded(boolean expanded) {
        section.setExpanded(expanded);
    }

    /**
     * @return whether this section is currently expanded (<code>true</code>) or collapsed (
     *             <code>false</code>).
     */
    protected boolean isExpanded() {
        return getSectionControl().isExpanded();
    }

    /**
     * Re-layouts the IpsSection's root-composite to assign appropriate amount of space to a changed
     * section. If the section was expanded let it grab as much space as possible (as do most
     * sections), if it was collapsed make it as small as possible (grabVertical=false).
     * <p>
     * Only re-layouts if this section is configured to do so. See
     * {@link #setGrabVerticalSpace(boolean)}.
     */
    protected void relayoutSection(boolean expanded) {
        if (isGrabVerticalSpace()) {
            setGrabVerticalLayoutData(expanded);
        } else {
            setGrabVerticalLayoutData(false);
        }
        getParent().layout();
    }

    private void setGrabVerticalLayoutData(boolean expanded) {
        GridData gridData = (GridData)getLayoutData();
        gridData.grabExcessVerticalSpace = expanded;
    }

    /**
     * Creates the UI controls for this section. All subclasses must implement this method on their
     * own.
     * 
     * @param client The composite to create the UI elements into.
     * @param toolkit The UI toolkit used to create new UI elements.
     */
    protected abstract void initClientComposite(Composite client, UIToolkit toolkit);

    /**
     * Returns the UI section widget being wrapped by this composite.
     * 
     * @return A handle to the UI section widget being wrapped by this composite.
     */
    protected Section getSectionControl() {
        return section;
    }

    /**
     * Sets the title of the section.
     * 
     * @param text The text to be displayed as title.
     */
    public void setText(String text) {
        section.setText(text);
        // till target platform is 3.7, where https://bugs.eclipse.org/bugs/show_bug.cgi?id=168745
        // is fixed
        getSectionControl().layout();
    }

    /**
     * @see org.eclipse.ui.forms.widgets.Section#setDescription(String)
     */
    public void setDescription(String description) {
        section.setDescription(description);
    }

    /**
     * Refreshes the section with the data from the model object(s).
     */
    public void refresh() {
        performRefresh();
    }

    /**
     * Subclass implementation that refreshes the section with actual data from the model object(s).
     * <p>
     * Called by {@link #refresh()} right after {@link BindingContext#updateUI()} is called.
     * <p>
     * The default implementation does nothing.
     * 
     * @see #refresh()
     */
    protected void performRefresh() {
        // Empty default implementation
    }

    /**
     * Returns whether the contents of this section can be changed by the user.
     */
    @Override
    public boolean isDataChangeable() {
        return changeable;
    }

    /**
     * Enables or disables whether the section's content can be changed by the user.
     */
    @Override
    public void setDataChangeable(boolean changeable) {
        this.changeable = changeable;
        toolkit.setDataChangeable(section.getClient(), changeable);

        if (dataChangeableStateChangeListeners == null) {
            return;
        }

        for (IDataChangeableStateChangeListener iDataChangeableStateChangeListener : dataChangeableStateChangeListeners) {
            iDataChangeableStateChangeListener.dataChangeableStateHasChanged(this);
        }
    }

    @Override
    public void addDataChangeableStateChangeListener(IDataChangeableStateChangeListener listener) {
        if (listener == null) {
            return;
        }

        if (dataChangeableStateChangeListeners == null) {
            dataChangeableStateChangeListeners = new CopyOnWriteArrayList<>();
        }

        dataChangeableStateChangeListeners.add(listener);
    }

    @Override
    public void removeDataChangeableStateChangeListener(IDataChangeableStateChangeListener listener) {
        if (dataChangeableStateChangeListeners == null) {
            return;
        }

        dataChangeableStateChangeListeners.remove(listener);
    }

    @Override
    public void widgetDisposed(DisposeEvent e) {
        if (e.widget == this) {
            bindingContext.dispose();
        }
    }

    protected final UIToolkit getToolkit() {
        return toolkit;
    }

    protected final BindingContext getBindingContext() {
        return bindingContext;
    }

    protected Composite getClientComposite() {
        return clientComposite;
    }

    /**
     * Configures this section to grab excess vertical space if it is expanded. This method does
     * <em>not</em> affect the initial layout of this section, which can only be defined by the the
     * style bits passed to the constructor.
     * <p>
     * Call this method before {@link #initControls()} in the constructor of a subclass.
     * 
     * @param grab whether to grab vertical space
     */
    public void setGrabVerticalSpace(boolean grab) {
        isGrabVerticalSpace = grab;
    }

    /**
     * 
     * @return whether this section will grab excess vertical space if it is expanded. Default is
     *             <code>true</code>.
     */
    public boolean isGrabVerticalSpace() {
        return isGrabVerticalSpace;
    }

    /**
     * 
     * @return whether this section will be initialized collapsed if it is empty.
     */
    public boolean isInitCollapsedIfNoContent() {
        return isInitCollapsedIfNoContent;
    }

    /**
     * Whether this section will be initialized collapsed if it is empty.
     * <p>
     * Override {@link #getSectionTitle()} to use this functionality.
     * 
     * @param initCollapsed the new flag value
     */
    public void setInitCollapsedIfNoContent(boolean initCollapsed) {
        isInitCollapsedIfNoContent = initCollapsed;
    }

}
