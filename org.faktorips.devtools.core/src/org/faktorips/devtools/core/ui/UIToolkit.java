package org.faktorips.devtools.core.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.controls.TableStructureRefControl;
import org.faktorips.values.EnumType;
import org.faktorips.values.EnumValue;


/**
 * A toolkit to create controls with a common look and feel.
 */
public class UIToolkit {
    
    private FormToolkit formToolkit;
    
    /**
     * Creates a new toolkit.
     */
    public UIToolkit(FormToolkit formToolkit) {
        this.formToolkit = formToolkit;
    }
    
    public FormToolkit getFormToolkit() {
        return formToolkit;
    }
    
    public GridLayout createNoMarginGridLayout(int numColumns, boolean equalSize) {
        GridLayout layout = new GridLayout(numColumns, equalSize);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        return layout;
    }
    
    /**
     * Creates a new composite with a grid layout and no borders. 
     */
    public Composite createGridComposite(
            Composite parent, 
            int numColumns, 
            boolean equalSize,
            boolean margin) {
        Composite newComposite;
        if (formToolkit!=null) {
            newComposite = formToolkit.createComposite(parent);
        } else {
            newComposite = new Composite(parent, SWT.NONE);
        }
        GridLayout layout = new GridLayout(numColumns, equalSize);
        if (!margin) {
	        layout.marginHeight = 0;
	        layout.marginWidth = 0;
        }
        newComposite.setLayout(layout);
        newComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        return newComposite;
    }
    
    /**
     * Creates a new composite. 
     */
    public Composite createComposite(Composite parent) {
        if (formToolkit!=null) {
            return formToolkit.createComposite(parent);
        }
        return new Composite(parent, SWT.NONE);
    }
    
    /**
     * Creates a new composite with a two column grid layout. 
     */
    public Composite createLabelEditColumnComposite(Composite parent) {
        Composite newComposite;
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 12;
        if (formToolkit!=null) {
            newComposite = formToolkit.createComposite(parent);
            formToolkit.paintBordersFor(newComposite);
            layout.marginHeight = 2;
            layout.marginWidth = 2;
        } else {
            newComposite = new Composite(parent, SWT.NONE); 
            newComposite.setBackground(parent.getBackground());
            layout.marginHeight = 0;
            layout.marginWidth = 0;
        }
        newComposite.setLayout(layout);
        if (parent.getLayout() instanceof GridLayout) {
            newComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }
        return newComposite;
    }
    
    
    /**
     * Creates a new composite with a three column grid layout. First column for 
     * structure-label, second column for input-label, third column for the input 
     * control. 
     */
    public Composite createStructuredLabelEditColumnComposite(Composite parent) {
        Composite newComposite;
        GridLayout layout = new GridLayout(3, false);
        layout.horizontalSpacing = 12;
        if (formToolkit!=null) {
            newComposite = formToolkit.createComposite(parent);
            formToolkit.paintBordersFor(newComposite);
            layout.marginHeight = 2;
            layout.marginWidth = 2;
        } else {
            newComposite = new Composite(parent, SWT.NONE); 
            newComposite.setBackground(parent.getBackground());
            layout.marginHeight = 0;
            layout.marginWidth = 0;
        }
        newComposite.setLayout(layout);
        if (parent.getLayout() instanceof GridLayout) {
            newComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }
        return newComposite;
    }

    /**
     * Creates a new label with the given text.
     * 
     * @param parent The parent composite.
     * @param text The text to display.
     * @param grab <code>true</code> to let the label grab more space then initial preferred, if available.
     */
    public Label createLabel(Composite parent, String text, boolean grab) {
    	Label label = createLabel(parent, text);
		((GridData)label.getLayoutData()).grabExcessHorizontalSpace = grab;
		((GridData)label.getLayoutData()).grabExcessVerticalSpace = grab;
		return label;
    }

    /**
     * Creates a new label.
     */
    public Label createLabel(Composite parent, String text) {
        Label newLabel;
        if (formToolkit!=null) {
            newLabel = formToolkit.createLabel(parent, text);
        } else {
            newLabel = new Label(parent, SWT.NONE);    
            newLabel.setBackground(parent.getBackground());
            if (text!=null) {
                newLabel.setText(text);
            }
        }
        newLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_END));
        return newLabel;
    }
    
    /**
     * Creates a new form label with margin.
     */
    public Label createFormLabel(Composite parent, String text) {
        Composite c = this.createComposite(parent);
        c.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_END));
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        layout.marginHeight = 1;
        layout.marginWidth = 0;
        c.setLayout(layout);
        Label newLabel = this.createLabel(c, text);
        newLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_END));
        return newLabel;
    }
    
    
    /**
     * Creates a spacer-control.
     * 
     * @param parent The parent composite.
     * @param height The preferred height.
     * @param grab <code>true</code> to let the spacer grab available space (horizontal and vertical) to  
     *             become bigger then the preferred size.
     */
    public Control createVerticalSpacer(Composite parent, int height, boolean grab) {
    	if (grab) {
    		return createVerticalSpacer(parent, height);
    	}
    	else {
    		Control c = createVerticalSpacer(parent, height);
    		((GridData)c.getLayoutData()).grabExcessHorizontalSpace = false;
    		((GridData)c.getLayoutData()).grabExcessVerticalSpace = false;
    		return c;
    	}
    }
    
    public Control createVerticalSpacer(Composite parent, int height) {
	    Composite spacer = new Composite(parent, SWT.NONE);
	    spacer.setLayout(new GridLayout());
	    GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	    data.heightHint = height;
	    data.widthHint = 1;
	    spacer.setLayoutData(data);
        return spacer;
    }

    /**
     * Creates a new form label with margin.
     */
    public Hyperlink createHyperlink(Composite parent, String text) {
        Hyperlink newLink; 
        if (formToolkit!=null) {
            newLink = formToolkit.createHyperlink(parent, text, SWT.NONE);; 
        } else {
            throw new RuntimeException("Not implemented yet!");
        }
        newLink.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_END));
        return newLink;
    }
    
    public Text createText(Composite parent) {
        Text newText;
        if (formToolkit!=null) {
            newText = formToolkit.createText(parent, null);
        } else {
            newText = new Text(parent, SWT.SINGLE | SWT.BORDER);
        }
        newText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return newText;
    }
    
    public Text createMultilineText(Composite parent) {
        Text newText;
        if (formToolkit!=null) {
            newText = formToolkit.createText(parent, null, SWT.MULTI);
        } else {
            newText = new Text(parent, SWT.MULTI | SWT.BORDER);
        }
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 60;
        newText.setLayoutData(gridData);
        return newText;
    }
    
    
    
    /**
     * Creates a new checkbox.
     */
    public Checkbox createCheckbox(Composite parent) {
        return new Checkbox(parent, this);
    }
    
    /**
     * Creates a new checkbox.
     */
    public Checkbox createCheckbox(Composite parent, String text) {
        Checkbox checkbox = new Checkbox(parent, this);
        if (text!=null) {
            checkbox.setText(text);
        }
        return checkbox;
    }
    
    /**
     * Creates a new push button.
     */
    public Button createButton(Composite parent, String text) {
        if (formToolkit!=null) {
            return formToolkit.createButton(parent, text, SWT.PUSH);
        }
        Button newButton = new Button(parent, SWT.PUSH);
        newButton.setText(text);
        return newButton;
    }
    
    /**
     * Creates a new package fragment reference control.
     */
    public IpsPckFragmentRefControl createPdPackageFragmentRefControl(Composite parent) {
        return new IpsPckFragmentRefControl(parent, this);
    }

    /**
     * Creates a new package fragment reference control.
     */
    public IpsPckFragmentRefControl createPdPackageFragmentRefControl(IIpsPackageFragmentRoot root, Composite parent) {
        IpsPckFragmentRefControl control = createPdPackageFragmentRefControl(parent);
        control.setPdPckFragmentRoot(root);
        return control;
    }

    /**
     * Creates a new package fragment reference control.
     */
    public IpsPckFragmentRootRefControl createPdPackageFragmentRootRefControl(Composite parent, boolean onlySourceRoots) {
        return new IpsPckFragmentRootRefControl(parent, onlySourceRoots, this);
    }

    /**
     * Creates a new PcTypeRefControl.
     */
    public PcTypeRefControl createPcTypeRefControl(IIpsProject project, Composite parent) {
        return new PcTypeRefControl(project, parent, this);
    }

    /**
     * Creates a new PcTypeRefControl.
     */
    public TableStructureRefControl createTableStructureRefControl(IIpsProject project, Composite parent) {
        return new TableStructureRefControl(project, parent, this);
    }
    
    /**
     * Creates a new DatatypeRefControl.
     */
    public DatatypeRefControl createDatatypeRefEdit(IIpsProject project, Composite parent) {
        return new DatatypeRefControl(project, parent, this);
    }
    
    public Combo createCombo(Composite parent) {
        if (formToolkit!=null) {
            Combo newCombo = new Combo(parent, SWT.READ_ONLY);
            newCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END));
            return newCombo;
        }
        Combo newCombo = new Combo(parent, SWT.READ_ONLY);
        newCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END));
        return newCombo;
    }
    
    public Combo createCombo(Composite parent, EnumType type) {
        Combo newCombo = new Combo(parent, SWT.READ_ONLY);
        newCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END));
        String[] names = new String[type.getNumOfValues()];
        EnumValue[] values = type.getValues();
        for (int i=0; i<values.length; i++) {
            names[i] = values[i].getName();        
        }
        newCombo.setItems(names);
        return newCombo;
    }
    
    public Group createGroup(Composite parent, String text) {
        return createGroup(parent, SWT.NONE, text);
    }
    
    public Group createGroup(Composite parent, int style, String text) {
        if (formToolkit!=null) {
            throw new RuntimeException("Not implemented for forms!");
        }
        Group newGroup = new Group(parent, style);
        if (text!=null) {
            newGroup.setText(text);
        }
        newGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout();
        newGroup.setLayout(layout);
        
        return newGroup;
    }
    
    public Group createGridGroup(
            Composite parent, 
            String text,
            int numOfColumns,
            boolean equalSize) {
        return createGridGroup(parent, SWT.NONE, text, numOfColumns, equalSize);
    }
    
    public Group createGridGroup(
            Composite parent, 
            int style, 
            String text,
            int numOfColumns,
            boolean equalSize) {
        Group newGroup = createGroup(parent, style, text);
        GridLayout layout = new GridLayout(numOfColumns, equalSize);
        newGroup.setLayout(layout);
        return newGroup;
    }
    
    public Label createHorizonzalLine(Composite parent) {
        Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        line.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return line;
    }
}
