package studio.kdb;

import java.util.ArrayList;
import java.util.List;

import studio.kdb.K.KBaseVector;
import studio.kdb.K.KSymbol;

public class DictionaryModel extends KTableModel {
    
    private static final String KEY_NAME = "key";
    private static final String VALUE_NAME = "value";

    private K.Dict dict;
    
    private List<String> columnNames = new ArrayList<>();
    private List<K.KBaseVector> columnValues = new ArrayList<>();
    
    public DictionaryModel(K.Dict obj) {
        setData(obj);
    }

    public void setData(K.Dict obj) {
        dict = obj;
        
        if (dict.x instanceof K.KBaseVector) {
            columnNames.add(KEY_NAME);
            columnNames.add(VALUE_NAME);
            columnValues.add((K.KBaseVector) dict.x);
            columnValues.add((K.KBaseVector) dict.y);
            
        } else if (dict.x instanceof K.Flip){
            K.Flip flip = (K.Flip) dict.x;
            // keys
            for (int i = 0; i < flip.y.getLength(); i++) {
                columnNames.add(KEY_NAME + "=" + ((KSymbol)flip.x.at(i)).toString(false));
                columnValues.add((K.KBaseVector) flip.y.at(i));
            }
            // value
            columnNames.add(VALUE_NAME);
            columnValues.add((K.KBaseVector) dict.y);
        } else {
            throw new RuntimeException("Unexpected class of dict.x : " + dict.x.getClass().toGenericString());
        }
    }
    
    public int getColumnCount() {
        return columnValues.size();
    }

    public int getRowCount() {
        return columnValues.get(0).getLength();
    }

    public Object getValueAt(int row, int col) {
        row = (sortIndex == null) ? row : sortIndex[row];
        return columnValues.get(col).at(row);
    }
    
    @Override
    public boolean isKey(int column) {
        if (column < columnValues.size() - 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public KBaseVector getColumn(int col) {
        return columnValues.get(col);
    }

    @Override
    public void asc(int col) {
        sortIndex = null;
        K.KBaseVector v = columnValues.get(col);
        
        sortIndex = v.gradeUp();
        sorted = 1;
        sortedByColumn = col;
    }

    @Override
    public void desc(int col) {
        sortIndex = null;
        K.KBaseVector v = columnValues.get(col);
        
        sortIndex = v.gradeDown();
        sorted = -1;
        sortedByColumn = col;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames.get(col);
    }
    
    public static boolean isDictionary(Object obj) {
        if (!(obj instanceof K.Dict)) {
            return false;
        }
        // check dictionary
        K.Dict dict = (K.Dict) obj;
        if (dict.x instanceof K.KBaseVector && dict.y instanceof K.KBaseVector) {
            return true;
        } else if (dict.x instanceof K.Flip){
            return true;
        }
        // Otherwise it cannot be display as table.
        return false;
    }
}
