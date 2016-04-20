/*
 * ChLongHashSet.java
 *
 * Created on 20. Oktober 2001, 17:50
 */

package chesspresso.position.map;

/**
 *
 * @author  BerniMan
 * @version 
 */
public class ChLongHashSet
{
    
    private long[][] m_keyTable;
    
    /*================================================================================*/

    public ChLongHashSet()
    {
        this(8192);
    }
    
    public ChLongHashSet(int size)
    {
        m_keyTable = new long[size][];
    }

    /*================================================================================*/

    public void add(long key)
    {
        int index = (int)(key % m_keyTable.length);
        long[] keys = m_keyTable[index];
        if (keys == null) {keys = new long[2]; m_keyTable[index] = keys;}
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == 0) {
                keys[i] = key;
        if (!contains(key))new Exception(key + " not contained").printStackTrace();
                return;  // =====>
            }
        }
        m_keyTable[index] = new long[2 * keys.length];
        System.arraycopy(keys, 0, m_keyTable[index], 0, keys.length);
        m_keyTable[index][keys.length] = key;
        if (!contains(key)) new Exception(key + " not contained").printStackTrace();
    }

    public boolean contains(long key)
    {
        int index = (int)(key % m_keyTable.length);
        long[] keys = m_keyTable[index];
        if (keys == null) return false;  // =====>
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == key) return true;  // =====>
        }
        return false;  // =====>
    }

}