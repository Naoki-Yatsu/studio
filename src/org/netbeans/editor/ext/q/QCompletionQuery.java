/* Studio for kdb+ by Charles Skelton
   is licensed under a Creative Commons Attribution-Noncommercial-Share Alike 3.0 Germany License
   http://creativecommons.org/licenses/by-nc-sa/3.0
   except for the netbeans components which retain their original copyright notice
*/
package org.netbeans.editor.ext.q;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private static final String QUERY_DELIMITER = "QUERY_DELIMITER";

    // keywords ref. QSyntax class
    private static final String[] RESERVED_KEYWORDS = new String[] {
            // plus datatypes : "boolean","guid","byte","short","int","long","real","float","char","symbol","timestamp","month","date","datetime","timespan","minute","second","time"
            //    "abs","acos","aj","aj0","all","and","any","asc","asin","asof","atan","attr","avg","avgs","bin","by","ceiling","cols","cor","cos","count","cov","cross","csv","cut","delete","deltas","desc","dev","differ","distinct","div","do","each","ej","enlist","eval","except","exec","exit","exp","fby","fills","first","fkeys","flip","floor","from","get","getenv","group","gtime","hclose","hcount","hdel","hopen","hsym","iasc","idesc","if","ij","in","insert","inter","inv","key","keys","last","like","list","lj","load","log","lower","lsq","ltime","ltrim","mavg","max","maxs","mcount","md5","mdev","med","meta","min","mins","mmax","mmin","mmu","mod","msum","neg","next","not","null","or","over","parse","peach","pj","plist","prd","prds","prev","prior","rand","rank","ratios","raze","read0","read1","reciprocal","reverse","rload","rotate","rsave","rtrim","save","scan","select","set","setenv","show","signum","sin","sqrt","ss","ssr","string","sublist","sum","sums","sv","system","tables","tan","til","trim","txf","type","uj","ungroup","union","update","upper","upsert","value","var","view","views","vs","wavg","where","while","within","wj","wj1","wsum","xasc","xbar","xcol","xcols","xdesc","xexp","xgroup","xkey","xlog","xprev","xrank"
            "abs","acos","aj","aj0","all","and","any","asc","asin","asof","atan","attr","avg","avgs","bin","boolean","by","byte","ceiling","char","cols","cor","cos","count","cov","cross","csv","cut","date","datetime","delete","deltas","desc","dev","differ","distinct","div","do","each","ej","enlist","eval","except","exec","exit","exp","fby","fills","first","fkeys","flip","float","floor","from","get","getenv","group","gtime","guid","hclose","hcount","hdel","hopen","hsym","iasc","idesc","if","ij","in","insert","int","inter","inv","key","keys","last","like","list","lj","load","log","long","lower","lsq","ltime","ltrim","mavg","max","maxs","mcount","md5","mdev","med","meta","min","mins","minute","mmax","mmin","mmu","mod","month","msum","neg","next","not","null","or","over","parse","peach","pj","plist","prd","prds","prev","prior","rand","rank","ratios","raze","read0","read1","real","reciprocal","reverse","rload","rotate","rsave","rtrim","save","scan","scov","sdev","second","select","set","setenv","short","show","signum","sin","sqrt","ss","ssr","string","sublist","sum","sums","sv","svar","symbol","system","tables","tan","til","time","timespan","timestamp","trim","txf","type","uj","ungroup","union","update","upper","upsert","value","var","view","views","vs","wavg","where","while","within","wj","wj1","wsum","xasc","xbar","xcol","xcols","xdesc","xexp","xgroup","xkey","xlog","xprev","xrank"
        };

    private static final Set<String> variableSet = new HashSet<>(100);

    private static final String QUERY_TITLE_COLUMN = "Columns";
    private static final String QUERY_TITLE_TABLE = "Tables";

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

                    // get text of this line(before cursor, after cursor)
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

                    StringTokenizer t = new StringTokenizer(text, " %$!&()=~#;:><?,+-'\"/*\n`[]{}");
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
                    // If end with words[a-zA-Z_0-9] , set last words as prefix
                    String prefix = "";
                    if (foreThisLine.matches(".*\\w$")) {
                        StringTokenizer token = new StringTokenizer(foreThisLine, " %$!&()=~#;:><?,+-'\"/*\n");
                        while (token.hasMoreTokens()) {
                            prefix = token.nextToken();
                        }
                    }

                    // if there is "where" in front of cursor, use the word before where as tablename.
                    if (foreThisLine.contains(" where ")) {
                        String[] strArray = foreThisLine.split("[ %$!&()=~#;:><?,'\"/\n]");
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
                        String[] strArray = latterThisLine.split("[ %$!&()=~#;:><?,'\"/\n]");
                        for (int i = 0; i < strArray.length; i++) {
                            if (strArray[i].equals("from") && i + 1 < strArray.length) {
                                tablename = strArray[i + 1];
                                break;
                            }
                        }
                    }
                    
                    // delete "`" in the top of prefix
                    if (prefix.startsWith("`")) {
                        prefix = prefix.substring(1);
                    }

                    kx.c c = null;
                    String queryTitle = null;

                    try {
                        c = ConnectionPool.getInstance().leaseConnection(s);
                        ConnectionPool.getInstance().checkConnected(c);

                        if (text.endsWith(".")) {
                            queryTitle = QUERY_TITLE_COLUMN + ": " + tablename + "  ";
                            tablename = text.substring(0, text.length() - 1);
                            currentIcon = Util.getImage(Config.imageBase2 + "column.png");
                            c.k(new K.KCharacterVector("cols " + tablename));

                        } else if (tablename != null && !tablename.equals("") && !tablename.equals(" ")) {
                            if (prefix.length() > 1) {
                                queryTitle = QUERY_TITLE_COLUMN + ": " + tablename + " - " + prefix + "  ";
                            } else {
                                queryTitle = QUERY_TITLE_COLUMN + ": " + tablename;
                            }
                            currentIcon = Util.getImage(Config.imageBase2 + "column.png");
//                            c.k(new K.KCharacterVector("cols " + tablename));
                            c.k(new K.KCharacterVector("(cols " + tablename + "),(`" +QUERY_DELIMITER + "),tables[],views[],(system \"f\"),(system \"v\")"));

                        } else {
                            if (prefix.length() > 1) {
                                queryTitle = QUERY_TITLE_TABLE + " - " + prefix + "  ";
                            } else {
                                queryTitle = QUERY_TITLE_TABLE;
                            }
                            currentIcon = Util.getImage(Config.imageBase2 + "table.png");
                            //c.k(new K.KCharacterVector("tables[]"));
                            c.k(new K.KCharacterVector("tables[]" + ",(`" +QUERY_DELIMITER + "),views[],(system \"f\"),(system \"v\")"));
                        }

                        Object res = c.getResponse();
                        if (res instanceof K.KSymbolVector) {
                            // create result list
                            List result = createResultList((K.KSymbolVector) res, prefix, offset, queryTitle.startsWith(QUERY_TITLE_COLUMN));
                            r = new CompletionQuery.DefaultResult(component, queryTitle, result, offset, 0);
                        }

                    } catch (Throwable th) {
                        List<BooleanAttribItem> result = new ArrayList<>();
                        result.add(new BooleanAttribItem("", offset, 0, false));
                        result.add(new BooleanAttribItem("[Query Error]", offset, 0, false));
                        // error reason
                        String errorReason = th.toString();
                        if (th.toString().length() > 100) {
                            errorReason.substring(0, 100);
                        }
                        result.add(new BooleanAttribItem(errorReason, offset, 0, false));

                        if (queryTitle != null) {
                            queryTitle = "[ERROR] " + queryTitle;
                        } else {
                            queryTitle = "[ERROR]";
                        }
                        r = new CompletionQuery.DefaultResult(component, queryTitle, result, offset, 0);
                    } finally {
                        if (c != null) {
                            ConnectionPool.getInstance().freeConnection(s, c);
                        }
                    }
                }
            }
        } catch (Throwable th) {
        }

        return r;
    }

    private static List createResultList(K.KSymbolVector items, String prefix, int offset, boolean isColumnQuery) {

        // prepare both of List
        List<BooleanAttribItem> allItems = new ArrayList<>();
        List<BooleanAttribItem> searchItems = new ArrayList<>();

        // update variable set
        variableSet.clear();

        // check delimiter
        boolean isBeforeDelimiter = true;
        boolean existSearchItem = false;

        for (int i = 0; i < items.getLength(); i++) {
            K.KSymbol symbol = (K.KSymbol) items.at(i);
            String word = symbol.s;

            // blank check
            if (word.trim().equals("")) {
                continue;
            }

            // check delimiter
            if (word.equals(QUERY_DELIMITER)) {
                // for delimiter add "" instead
                word = "";
                isBeforeDelimiter = false;
                allItems.add(new BooleanAttribItem(word, offset, 0, false));
                // if it is better to show blank line is search list, comment out.
//                searchItems.add(new BooleanAttribItem(word, offset, 0, false));
                continue;
            }

            // Prevent duplication of table
            if (variableSet.contains(word)) {
                continue;
            } else {
                // Don't add columns
                if (!(isColumnQuery && isBeforeDelimiter)) {
                    variableSet.add(word);
                }
            }

            allItems.add(new BooleanAttribItem(word, offset, 0, false));
            if (prefix != "" && word.startsWith(prefix)) {
                existSearchItem = true;
                searchItems.add(new BooleanAttribItem(word.substring(prefix.length()), offset, 0, false));
            }
        }

        // add reverved keywords only for searchItems
        // if last item is not "", add delimiter
        if (searchItems.size() > 0 && ! searchItems.get(searchItems.size() -1 ).getItemText().equals("")) {
            searchItems.add(new BooleanAttribItem("", offset, 0, false));
        }
        for (int i = 0; i < RESERVED_KEYWORDS.length; i++) {
            String word = RESERVED_KEYWORDS[i];
            if (prefix != "" && word.startsWith(prefix)) {
                existSearchItem = true;
                searchItems.add(new BooleanAttribItem(word.substring(prefix.length()), offset, 0, false));
            }
        }

        // if items which start with prefix is not zero, use only search items.
        List<BooleanAttribItem> result;
        if (existSearchItem) {
            result = searchItems;
        } else {
            result = allItems;
        }

        // If result is only one, add more item in order to prevent auto fill.
        if (result.size() == 1) {
            result.add(new BooleanAttribItem("", offset, 0, false));
        }

        return result;
    }

    public static Set<String> getVariableSet() {
        return variableSet;
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
