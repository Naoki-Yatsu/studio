/* Studio for kdb+ by Charles Skelton
   is licensed under a Creative Commons Attribution-Noncommercial-Share Alike 3.0 Germany License
   http://creativecommons.org/licenses/by-nc-sa/3.0
   except for the netbeans components which retain their original copyright notice
*/
package org.netbeans.editor.ext.q;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.ext.CompletionQuery;

import studio.kdb.Config;
import studio.kdb.ConnectionPool;
import studio.kdb.K;
import studio.kdb.Server;
import studio.ui.Util;

public class QCompletionQuery implements CompletionQuery
{
    private static ImageIcon currentIcon;
    private static boolean lowerCase;

    public CompletionQuery.Result query(JTextComponent component, int offset, SyntaxSupport support) {
        CompletionQuery.Result r = null;
        currentIcon = null;

        try {
            if (component instanceof JEditorPane) {
                Server s = (Server) ((JEditorPane) component).getDocument().getProperty("server");

                if (s != null) {
                    Document doc = ((JEditorPane) component).getDocument();
                    String text = doc.getText(0, offset);
                    String latterText = doc.getText(offset, doc.getLength() - offset);

                    // get text of this line
                    String[] lines = text.split("\n");
                    String foreThisLine = "";
                    if (!text.endsWith("\n") && lines.length > 0) {
                        foreThisLine = lines[lines.length - 1];
                    }
                    String[] latterlines = latterText.split("\n");
                    String latterThisLine = "";
                    if (latterlines.length > 0) {
                        latterThisLine = latterlines[0];
                    }

                    StringTokenizer t = new StringTokenizer(text, " %$!&()=~#;:><?,+-'\"/*\n");
                    if (text.endsWith("\n")) {
                        text = "";
                    } else {
                        while (t.hasMoreTokens()) {
                            text = t.nextToken();
                        }
                    }

                    // tablename, using to search column
                    String tablename = "";

                    // edit fore of curser
                    // If end without " " or ",", set last word as prefix
                    String prefix = "";
                    if (!foreThisLine.endsWith(" ") && !foreThisLine.endsWith(",")) {
                        StringTokenizer token = new StringTokenizer(foreThisLine, " %$!&()=~#;:><?,+-'\"/*\n");
                        while (token.hasMoreTokens()) {
                            prefix = token.nextToken();
                        }
                    }

                    // if there is "where" in front of cursor, use the word before where as tablename.
                    if (foreThisLine.contains(" where ")) {
                        String[] strArray = foreThisLine.split(" +");
                        for (int i = 0; i < strArray.length; i++) {
                            if (strArray[i].equals("where") && i > 0) {
                                tablename = strArray[i - 1];
                                break;
                            }
                        }
                    }

                    // edit latter of cursor
                    // If there is "from" after cursor, use the word after "from" as tablaname.
                    if (latterThisLine.contains("from ")) {
                        String[] strArray = latterThisLine.split(" +");
                        for (int i = 0; i < strArray.length; i++) {
                            if (strArray[i].equals("from") && i + 1 < strArray.length) {
                                tablename = strArray[i + 1];
                                break;
                            }
                        }
                    }

                    kx.c c = null;

                    try {
                        c = ConnectionPool.getInstance().leaseConnection(s);
                        ConnectionPool.getInstance().checkConnected(c);

                        String queryName = "";

                        if (text.endsWith(".")) {
                            queryName = "Columns";
                            tablename = text.substring(0, text.length() - 1);
                            currentIcon = Util.getImage(Config.imageBase2 + "column.png");
                            c.k(new K.KCharacterVector("cols " + tablename));

                        } else if (tablename != null && !tablename.equals("") && !tablename.equals(" ")) {
                            queryName = "Columns";
                            currentIcon = Util.getImage(Config.imageBase2 + "column.png");
                            c.k(new K.KCharacterVector("cols " + tablename));

                        } else {
                            queryName = "Tables";
                            currentIcon = Util.getImage(Config.imageBase2 + "table.png");
                            c.k(new K.KCharacterVector("tables[]"));
                        }

                        Object res = c.getResponse();
                        if (res instanceof K.KSymbolVector) {
                            K.KSymbolVector items = (K.KSymbolVector) res;

                            List allItems = new ArrayList();
                            List searchItems = new ArrayList();

                            for (int i = 0; i < items.getLength(); i++) {
                                K.KSymbol symbol = (K.KSymbol) items.at(i);
                                allItems.add(new BooleanAttribItem(symbol.s, offset, 0, false));

                                if (prefix != "" && symbol.s.startsWith(prefix)) {
                                    searchItems.add(new BooleanAttribItem(symbol.s.substring(prefix.length()), offset, 0, false));
                                }
                            }

                            List result = new ArrayList();

                            // if items which start with prefix is not zero, use only search items.
                            if (searchItems.isEmpty()) {
                                result = allItems;
                            } else {
                                result = searchItems;
                            }

                            r = new CompletionQuery.DefaultResult(component, queryName, result, offset, 0);
                        }

                    } catch (Throwable th) {
                    } finally {
                        if (c != null)
                            ConnectionPool.getInstance().freeConnection(s, c);
                    }
                }
            }
        } catch (Throwable th) {
        }

        return r;
    }


    private static abstract class QResultItem implements CompletionQuery.ResultItem
    {
        static javax.swing.JLabel rubberStamp = new javax.swing.JLabel();

        static
        {
            rubberStamp.setOpaque(true);
        }

        String baseText;
        int offset;
        int length;

        public QResultItem(String baseText, int offset, int length)
        {
            this.baseText = lowerCase ? baseText.toLowerCase() : baseText;
            this.offset = offset;
            this.length = length;
        }

        boolean replaceText(JTextComponent component, String text)
        {
            BaseDocument doc = (BaseDocument) component.getDocument();
            doc.atomicLock();
            try
            {
                doc.remove(offset, length);
                doc.insertString(offset, text, null);
            }
            catch (BadLocationException exc)
            {
                return false;    //not sucessfull
            }
            finally
            {
                doc.atomicUnlock();
            }
            return true;
        }

        public boolean substituteCommonText(JTextComponent c, int a, int b, int subLen)
        {
            return replaceText(c, getItemText().substring(0, subLen));
        }

        public boolean substituteText(JTextComponent c, int a, int b, boolean shift)
        {
            return replaceText(c, getItemText());
        }

        /** @return Properly colored JLabel with text gotten from <CODE>getPaintText()</CODE>. */
        public java.awt.Component getPaintComponent(javax.swing.JList list, boolean isSelected, boolean cellHasFocus)
        {
            // The space is prepended to avoid interpretation as Q Label
            rubberStamp.setText(" " + getPaintText());  // NOI18N

            if( currentIcon != null)
            {
                rubberStamp.setIcon( currentIcon);
                rubberStamp.setIconTextGap(8);
            }

            if (isSelected)
            {
                rubberStamp.setBackground(list.getSelectionBackground());
                rubberStamp.setForeground(list.getSelectionForeground());
            }
            else
            {
                rubberStamp.setBackground(list.getBackground());
                rubberStamp.setForeground(getPaintColor());
            }
            return rubberStamp;
        }

        String getPaintText()
        {
            return getItemText();
        }

        abstract Color getPaintColor();

        public String getItemText()
        {
            return baseText;
        }
    }

    static class EndTagItem extends QResultItem
    {

        public EndTagItem(String baseText, int offset, int length)
        {
            super(baseText, offset, length);
        }

        Color getPaintColor()
        {
            return Color.blue;
        }

        public String getItemText()
        {
            return "</" + baseText + ">";
        } // NOI18N

        public boolean substituteText(JTextComponent c, int a, int b, boolean shift)
        {
            return super.substituteText(c, a, b, shift);
        }
    }

    private static class CharRefItem extends QResultItem
    {

        public CharRefItem(String name, int offset, int length)
        {
            super(name, offset, length);
        }

        Color getPaintColor()
        {
            return Color.red.darker();
        }

        public String getItemText()
        {
            return "&" + baseText + ";";
        } // NOI18N
    }

    private static class TagItem extends QResultItem
    {

        public TagItem(String name, int offset, int length)
        {
            super(name, offset, length);
        }

        public boolean substituteText(JTextComponent c, int a, int b, boolean shift)
        {
            replaceText(c, "<" + baseText + (shift ? " >" : ">")); // NOI18N
            if (shift)
            {
                Caret caret = c.getCaret();
                caret.setDot(caret.getDot() - 1);
            }
            return !shift; // flag == false;
        }

        Color getPaintColor()
        {
            return Color.blue;
        }

        public String getItemText()
        {
            return "<" + baseText + ">";
        } // NOI18N
    }

    private static class SetAttribItem extends QResultItem
    {
        boolean required;

        public SetAttribItem(String name, int offset, int length, boolean required)
        {
            super(name, offset, length);
            this.required = required;
        }

        Color getPaintColor()
        {
            return required ? Color.red : Color.green.darker();
        }

        String getPaintText()
        {
            return baseText;
        }

        public String getItemText()
        {
            return baseText + "=";
        } //NOI18N

        public boolean substituteText(JTextComponent c, int a, int b, boolean shift)
        {
            super.substituteText(c, 0, 0, shift);
            return false; // always refresh
        }
    }

    private static class BooleanAttribItem extends QResultItem
    {

        boolean required;

        public BooleanAttribItem(String name, int offset, int length, boolean required)
        {
            super(name, offset, length);
            this.required = required;
        }

        Color getPaintColor()
        {
            return required ? Color.red : Color.green.darker();
        }


        public boolean substituteText(JTextComponent c, int a, int b, boolean shift)
        {
            replaceText(c, shift ? baseText + " " : baseText);
            return false; // always refresh
        }
    }

    private static class PlainAttribItem extends QResultItem
    {

        boolean required;

        public PlainAttribItem(String name, int offset, int length, boolean required)
        {
            super(name, offset, length);
            this.required = required;
        }

        Color getPaintColor()
        {
            return required ? Color.red : Color.green.darker();
        }

        public boolean substituteText(JTextComponent c, int a, int b, boolean shift)
        {
            replaceText(c, baseText + "=''"); //NOI18N
            if (shift)
            {
                Caret caret = c.getCaret();
                caret.setDot(caret.getDot() - 1);
            }
            return false; // always refresh
        }
    }

    private static class ValueItem extends QResultItem
    {

        public ValueItem(String name, int offset, int length)
        {
            super(name, offset, length);
        }

        Color getPaintColor()
        {
            return Color.magenta;
        }

        public boolean substituteText(JTextComponent c, int a, int b, boolean shift)
        {
            replaceText(c, shift ? baseText + " " : baseText); // NOI18N
            return !shift;
        }
    }
}
