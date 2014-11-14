package studio.kdb;

import studio.kdb.K.KBaseVector;

public class DictionaryModel extends KTableModel {

    private K.Dict dict;
    
    public DictionaryModel(K.Dict obj) {
        setData(obj);
    }

    public void setData(K.Dict obj) {
        dict = obj;
    }
    
    public int getColumnCount() {
        return 2;
    }

    public int getRowCount() {
        return ((K.KBaseVector) dict.x).getLength();
    }

    public Object getValueAt(int arg0, int arg1) {
        if (arg1 == 0) {
            return ((K.KBaseVector) dict.x).at(arg0);
        } else if (arg1 ==1) {
            return ((K.KBaseVector) dict.y).at(arg0);
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
            return (K.KBaseVector) dict.x;
        } else if (col ==1) {
            return (K.KBaseVector) dict.y;
        } else {
            return null;
        }
    }

    @Override
    public void asc(int col) {
        sortIndex = null;
        K.KBaseVector v = null;
        if (col == 0) {
            v = (K.KBaseVector) dict.x;
        } else {
            v = (K.KBaseVector) dict.y;
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
            v = (K.KBaseVector) dict.x;
        } else {
            v = (K.KBaseVector) dict.y;
        }
        sortIndex = v.gradeDown();
        sorted = -1;
        sortedByColumn = col;
    }

    @Override
    public String getColumnName(int col) {
        if (col == 0) {
            return "key";
        } else {
            return "value";
        }
    }
    
    public static boolean isDictionary(Object obj) {
        if (obj instanceof K.Dict) {
            return true;
        } else {
            return false;
        }
    }
    
}
