package com.swissas.util;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * TODO: write you class description here
 *
 * @author TALA
 */

/*
 * ComboBox AutoCompletion taken from http://www.orbital-computer.de/JComboBox/source/AutoCompletion.java and refreshed with actual java code
 * @author Tavan Alain
 */
public class AutoCompletion<T> extends PlainDocument {
    private final JComboBox<T> comboBox;
    private ComboBoxModel<T> model;
    private JTextComponent editor;
    // flag to indicate if setSelectedItem has been called
    // subsequent calls to remove/insertString should be ignored
    private boolean selecting = false;
    private final boolean hidePopupOnFocusLoss;
    private boolean hitBackspace = false;
    private boolean hitBackspaceOnSelection;

    private final KeyListener editorKeyListener;
    private final FocusListener editorFocusListener;

    private AutoCompletion(final JComboBox<T> comboBox) {
        this.comboBox = comboBox;
        this.model = comboBox.getModel();
        comboBox.addActionListener(e -> {
            if (!this.selecting) highlightCompletedText(0);
        });
        comboBox.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("editor")) configureEditor((ComboBoxEditor) e.getNewValue());
            if (e.getPropertyName().equals("model")) this.model = (ComboBoxModel<T>) e.getNewValue();
        });
        this.editorKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (comboBox.isDisplayable()) comboBox.setPopupVisible(true);
                AutoCompletion.this.hitBackspace = false;
                switch (e.getKeyCode()) {
                    // determine if the pressed key is backspace (needed by the remove method)
                    case KeyEvent.VK_BACK_SPACE:
                        AutoCompletion.this.hitBackspace = true;
                        AutoCompletion.this.hitBackspaceOnSelection = AutoCompletion.this.editor.getSelectionStart() != AutoCompletion.this.editor.getSelectionEnd();
                        break;
                    // ignore delete key
                    case KeyEvent.VK_DELETE:
                        e.consume();
                        comboBox.getToolkit().beep();
                        break;
                }
            }
        };
        // Bug 5100422 on Java 1.5: Editable JComboBox won't hide popup when tabbing out
        this.hidePopupOnFocusLoss = System.getProperty("java.version").startsWith("1.5");
        // Highlight whole text when gaining focus
        this.editorFocusListener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                highlightCompletedText(0);
            }

            @Override
            public void focusLost(FocusEvent e) {
                // Workaround for Bug 5100422 - Hide Popup on focus loss
                if (AutoCompletion.this.hidePopupOnFocusLoss) comboBox.setPopupVisible(false);
            }
        };
        configureEditor(comboBox.getEditor());
        // Handle initially selected object
        Object selected = comboBox.getSelectedItem();
        if (selected != null) setText(selected.toString());
        highlightCompletedText(0);
    }

    public static void enable(JComboBox<?> comboBox) {
        // has to be editable
        comboBox.setEditable(true);
        // change the editor's document
        new AutoCompletion(comboBox);
    }

    private void configureEditor(ComboBoxEditor newEditor) {
        if (this.editor != null) {
            this.editor.removeKeyListener(this.editorKeyListener);
            this.editor.removeFocusListener(this.editorFocusListener);
        }

        if (newEditor != null) {
            this.editor = (JTextComponent) newEditor.getEditorComponent();
            this.editor.addKeyListener(this.editorKeyListener);
            this.editor.addFocusListener(this.editorFocusListener);
            this.editor.setDocument(this);
        }
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException {
        // return immediately when selecting an item
        if (this.selecting) return;
        if (this.hitBackspace) {
            // user hit backspace => move the selection backwards
            // old item keeps being selected
            if (offs > 0) {
                if (this.hitBackspaceOnSelection) offs--;
            } else {
                // User hit backspace with the cursor positioned on the start => beep
                this.comboBox.getToolkit().beep(); // when available use: UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
            }
            highlightCompletedText(offs);
        } else {
            super.remove(offs, len);
        }
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        // return immediately when selecting an item
        if (this.selecting) return;
        // insert the string into the document
        super.insertString(offs, str, a);
        // lookup and select a matching item
        Object item = lookupItem(getText(0, getLength()));
        if (item != null) {
            setSelectedItem(item);
        } else {
            // keep old item selected if there is no match
            item = this.comboBox.getSelectedItem();
            // imitate no insert (later on offs will be incremented by str.length(): selection won't move forward)
            offs = offs - str.length();
            // provide feedback to the user that his input has been received but can not be accepted
            this.comboBox.getToolkit().beep(); // when available use: UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
        }
        setText(item.toString());
        // select the completed part
        highlightCompletedText(offs + str.length());
    }

    private void setText(String text) {
        try {
            // remove all text and insert the completed string
            super.remove(0, getLength());
            super.insertString(0, text, null);
        } catch (BadLocationException e) {
            throw new RuntimeException(e.toString());
        }
    }

    private void highlightCompletedText(int start) {
        this.editor.setCaretPosition(getLength());
        this.editor.moveCaretPosition(start);
    }

    private void setSelectedItem(Object item) {
        this.selecting = true;
        this.model.setSelectedItem(item);
        this.selecting = false;
    }

    private Object lookupItem(String pattern) {
        Object selectedItem = this.model.getSelectedItem();
        // only search for a different item if the currently selected does not match
        if (selectedItem != null && startsWithIgnoreCase(selectedItem.toString(), pattern)) {
            return selectedItem;
        } else {
            // iterate over all items
            for (int i = 0, n = this.model.getSize(); i < n; i++) {
                Object currentItem = this.model.getElementAt(i);
                // current item starts with the pattern?
                if (currentItem != null && startsWithIgnoreCase(currentItem.toString(), pattern)) {
                    return currentItem;
                }
            }
        }
        // no item starts with the pattern => return null
        return null;
    }

    // checks if str1 starts with str2 - ignores case
    private boolean startsWithIgnoreCase(String str1, String str2) {
        return str1.toUpperCase().startsWith(str2.toUpperCase());
    }
}