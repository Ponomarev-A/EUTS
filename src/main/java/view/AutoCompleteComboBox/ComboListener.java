package view.AutoCompleteComboBox;

import org.apache.commons.lang.WordUtils;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

class ComboListener extends KeyAdapter {
    @SuppressWarnings("rawtypes")
    private
    JComboBox cbListener;
    @SuppressWarnings("rawtypes")
    private
    Vector vector;

    @SuppressWarnings("rawtypes")
    ComboListener(JComboBox cbListenerParam, Vector vectorParam) {
        cbListener = cbListenerParam;
        vector = vectorParam;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void keyReleased(KeyEvent key) {
        String text = ((JTextField) key.getSource()).getText();
        changeAutoCompleteVariants(text);

        cbListener.showPopup();
    }

    void changeAutoCompleteVariants(String text) {
        cbListener.setModel(new DefaultComboBoxModel(getFilteredList(text)));
        cbListener.setSelectedIndex(-1);
        ((JTextField) cbListener.getEditor().getEditorComponent()).setText(text);
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    private Vector getFilteredList(String text) {
        Vector v = new Vector();
        for (Object aVector : vector) {
            if (aVector.toString().startsWith(text)) {
                v.add(aVector.toString());
            } else if (aVector.toString().startsWith(text.toLowerCase())) {
                v.add(aVector.toString());
            } else if (aVector.toString().startsWith(text.toUpperCase())) {
                v.add(aVector.toString());
            } else if (aVector.toString().startsWith(WordUtils.capitalizeFully(text))) {
                v.add(aVector.toString());
            } else if (aVector.toString().startsWith(WordUtils.uncapitalize(text))) {
                v.add(aVector.toString());
            }
        }
        return v;
    }
}
