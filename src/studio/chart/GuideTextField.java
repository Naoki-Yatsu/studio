package studio.chart;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;

public class GuideTextField extends JTextField implements FocusListener {

    private static final long serialVersionUID = 1L;
    private String helpMessage;
    private String backupText = "";

    public GuideTextField(String msg) {
        helpMessage = msg;
        addFocusListener(this);
        showHelpMessage();
    }
    
    public GuideTextField(String msg, int columns) {
        super(columns);
        helpMessage = msg;
        addFocusListener(this);
        showHelpMessage();
    }

    public GuideTextField(String msg, String text, int columns) {
        super(text, columns);
        helpMessage = msg;
        addFocusListener(this);
        backupText = text;
    }
    
    private void showHelpMessage() {
        setForeground(Color.LIGHT_GRAY);
        setText(helpMessage);
    }

    @Override
    public void focusGained(FocusEvent arg0) {
        setForeground(Color.BLACK);
        setText(backupText);
    }

    @Override
    public void focusLost(FocusEvent arg0) {
        backupText = getText();
        if (backupText.equals("")) {
            showHelpMessage();
        }
    }
    
    @Override
    public String getText() {
        String text = super.getText();
        if (text.equals(helpMessage)  ) {
            return "";
        }
        return text;
    }
    
    public void clearText(String init) {
        if (StringUtils.isEmpty(init)) {
            setText("");
        } else {
            setForeground(Color.BLACK);
            setText(init);
        }
        backupText = getText();
        if (backupText.equals("")) {
            showHelpMessage();
        }
    }
}
