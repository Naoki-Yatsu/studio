package studio.kdb;

import studio.kdb.K.KBaseVector;
import studio.kdb.K.KSymbol;

public class DictionaryModel extends KTableModel {
    
    private static final String KEY_NAME = "key";
    private static final String VALUE_NAME = "value";

    private K.Dict dict;
    private K.KBaseVector x;
    private K.KBaseVector y;

    private String keyName = KEY_NAME;
    private String valueName = VALUE_NAME;
    
    public DictionaryModel(K.Dict obj) {
        setData(obj);
    }

    public void setData(K.Dict obj) {
        dict = obj;
        
        if (dict.x instanceof K.KBaseVector) {
            x = (K.KBaseVector) dict.x;
            y = (K.KBaseVector) dict.y;
        } else if (isKeyFlipPrintableDictionary(dict)) {
            K.Flip flip = (K.Flip) dict.x;
            keyName = ((KSymbol)flip.x.at(0)).toString(false);
            valueName = "";
            x = (K.KBaseVector) flip.y.at(0);
            y = (K.KBaseVector) dict.y;
        }
    }
    
    public int getColumnCount() {
        return 2;
    }

    public int getRowCount() {
        return x.getLength();
    }

    public Object getValueAt(int arg0, int arg1) {
        if (arg1 == 0) {
            return x.at(arg0);
        } else if (arg1 ==1) {
            return y.at(arg0);
        } else {
            return null;
        }
    }

    @Override
    public boolean isKey(int column) {
        if (column == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public KBaseVector getColumn(int col) {
        if (col == 0) {
            return x;
        } else if (col ==1) {
            return y;
        } else {
            return null;
        }
    }

    @Override
    public void asc(int col) {
        sortIndex = null;
        K.KBaseVector v = null;
        if (col == 0) {
            v = x;
        } else {
            v = y;
        }
        sortIndex = v.gradeUp();
        sorted = 1;
        sortedByColumn = col;
    }

    @Override
    public void desc(int col) {
        sortIndex = null;
        K.KBaseVector v = null;
        if (col == 0) {
            v = x;
        } else {
            v = y;
        }
        sortIndex = v.gradeDown();
        sorted = -1;
        sortedByColumn = col;
    }

    @Override
    public String getColumnName(int col) {
        if (col == 0) {
            return keyName;
        } else {
            return valueName;
        }
    }
    
    public static boolean isDictionary(Object obj) {
        if (!(obj instanceof K.Dict)) {
            return false;
        }
        // check dictionary
        K.Dict dict = (K.Dict) obj;
        if (dict.x instanceof K.KBaseVector && dict.y instanceof K.KBaseVector) {
            return true;
        } else {
            // special dictionary
            if (isKeyFlipPrintableDictionary(dict)) {
                return true;
            }
        }
        // Otherwise it cannot be display as table.
        return false;
    }
    
    public static boolean isKeyFlipPrintableDictionary(K.Dict dict) {
        if (dict.x instanceof K.Flip) {
            K.Flip flip = (K.Flip) dict.x;
            // Only when lenth == 1, it is printable
            if (flip.x instanceof K.KBaseVector
                    && ((K.KBaseVector)flip.x).getLength() == 1) {
                return true;
            }
        }
        return false;
    }
}
