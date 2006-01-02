package org.faktorips.devtools.core.ui.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 * A composite that consists of a text control on the left and a button
 * attached to it on the right. If the button is clicked, the method
 * <code>buttonClicked</code> is called. The method has to implemented
 * in subclasses.
 */
public abstract class TextButtonControl extends ControlComposite{

    // text and button controls
    protected Text text;
    private Button button;
    
    /**
     * @param parent
     * @param style
     */
    public TextButtonControl(
            Composite parent, 
            UIToolkit toolkit,
            String buttonText) {
        super(parent, SWT.NONE);
        setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);
        if (toolkit.getFormToolkit()==null) {
            text = toolkit.createText(this);
        } else {
            Composite c = toolkit.getFormToolkit().createComposite(this);
            GridLayout layout2 = new GridLayout(2, false);
            layout2.marginHeight = 3;
            layout2.marginWidth = 1;
            c.setLayout(layout2);
            c.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL));
            toolkit.getFormToolkit().paintBordersFor(c);
            text = toolkit.createText(c);
            toolkit.getFormToolkit().adapt(this); // has to be done after the text control is created!
        }
        text.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));
        button = toolkit.createButton(this, buttonText);
        button.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                buttonClicked();
            }
        });
    }
    
    protected abstract void buttonClicked();
    
    public void setButtonEnabled(boolean value) {
        button.setEnabled(value);
    }
    
    public void setText(String newText) {
        text.setText(newText);
    }
    
    public String getText() {
        return text.getText();
    }
    
    public Text getTextControl() {
        return text;
    }
    
    public boolean setFocus() {
        return text.setFocus();
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.swt.widgets.Widget#addListener(int, org.eclipse.swt.widgets.Listener)
     */
    public void addListener(int eventType, Listener listener) {
        super.addListener(eventType, listener);
        if (eventType != SWT.Paint) {
            listenToControl(text, eventType);
        }
    }
}
