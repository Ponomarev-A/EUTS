package view.widget;

import javax.swing.*;
import java.util.Vector;

/**
 * Auto complete ComboBox class
 */
public class AutoComboBox extends JComboBox<Object> {

    private final ComboListener comboListener;
    private String keyWord[] = {};
    private Vector myVector = new Vector();

    public AutoComboBox() {

        setModel(new DefaultComboBoxModel(myVector));
        setSelectedIndex(-1);
        setEditable(true);
        JTextField text = (JTextField) this.getEditor().getEditorComponent();
        text.setFocusable(true);
        text.setText("");
        comboListener = new ComboListener(this, myVector);
        text.addKeyListener(comboListener);
        setMyVector();
    }

    private void setMyVector() {
        int a;
        for (a = 0; a < keyWord.length; a++) {
            myVector.add(keyWord[a]);
        }
    }

    /**
     * set the item list of the AutoComboBox
     *
     * @param keyWord an String array
     */
    public void setKeyWord(String[] keyWord) {
        this.keyWord = keyWord;
        setMyVectorInitial();
    }

    private void setMyVectorInitial() {
        myVector.clear();
        int a;
        for (a = 0; a < keyWord.length; a++) {
            myVector.add(keyWord[a]);
        }
    }

    public void resetSelection() {
        comboListener.changeAutoCompleteVariants("");
    }

}