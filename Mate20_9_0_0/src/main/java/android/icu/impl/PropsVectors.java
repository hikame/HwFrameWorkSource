package android.icu.impl;

import android.icu.impl.TrieBuilder.DataManipulate;
import java.util.Arrays;
import java.util.Comparator;

public class PropsVectors {
    public static final int ERROR_VALUE_CP = 1114113;
    public static final int FIRST_SPECIAL_CP = 1114112;
    public static final int INITIAL_ROWS = 4096;
    public static final int INITIAL_VALUE_CP = 1114112;
    public static final int MAX_CP = 1114113;
    public static final int MAX_ROWS = 1114114;
    public static final int MEDIUM_ROWS = 65536;
    private int columns;
    private boolean isCompacted;
    private int maxRows;
    private int prevRow;
    private int rows;
    private int[] v;

    public interface CompactHandler {
        void setRowIndexForErrorValue(int i);

        void setRowIndexForInitialValue(int i);

        void setRowIndexForRange(int i, int i2, int i3);

        void startRealValues(int i);
    }

    private static class DefaultGetFoldedValue implements DataManipulate {
        private IntTrieBuilder builder;

        public DefaultGetFoldedValue(IntTrieBuilder inBuilder) {
            this.builder = inBuilder;
        }

        public int getFoldedValue(int start, int offset) {
            int initialValue = this.builder.m_initialValue_;
            int limit = start + 1024;
            while (start < limit) {
                boolean[] inBlockZero = new boolean[1];
                int value = this.builder.getValue(start, inBlockZero);
                if (inBlockZero[0]) {
                    start += 32;
                } else if (value != initialValue) {
                    return offset;
                } else {
                    start++;
                }
            }
            return 0;
        }
    }

    private static class DefaultGetFoldingOffset implements Trie.DataManipulate {
        private DefaultGetFoldingOffset() {
        }

        /* synthetic */ DefaultGetFoldingOffset(AnonymousClass1 x0) {
            this();
        }

        public int getFoldingOffset(int value) {
            return value;
        }
    }

    private boolean areElementsSame(int index1, int[] target, int index2, int length) {
        for (int i = 0; i < length; i++) {
            if (this.v[index1 + i] != target[index2 + i]) {
                return false;
            }
        }
        return true;
    }

    private int findRow(int rangeStart) {
        int index = this.prevRow * this.columns;
        if (rangeStart >= this.v[index]) {
            if (rangeStart < this.v[index + 1]) {
                return index;
            }
            index += this.columns;
            if (rangeStart < this.v[index + 1]) {
                this.prevRow++;
                return index;
            }
            index += this.columns;
            if (rangeStart < this.v[index + 1]) {
                this.prevRow += 2;
                return index;
            } else if (rangeStart - this.v[index + 1] < 10) {
                this.prevRow += 2;
                do {
                    this.prevRow++;
                    index += this.columns;
                } while (rangeStart >= this.v[index + 1]);
                return index;
            }
        } else if (rangeStart < this.v[1]) {
            this.prevRow = 0;
            return 0;
        }
        int start = 0;
        int limit = this.rows;
        while (start < limit - 1) {
            int mid = (start + limit) / 2;
            index = this.columns * mid;
            if (rangeStart < this.v[index]) {
                limit = mid;
            } else if (rangeStart < this.v[index + 1]) {
                this.prevRow = mid;
                return index;
            } else {
                start = mid;
            }
        }
        this.prevRow = start;
        return this.columns * start;
    }

    public PropsVectors(int numOfColumns) {
        if (numOfColumns >= 1) {
            this.columns = numOfColumns + 2;
            this.v = new int[(this.columns * 4096)];
            this.maxRows = 4096;
            this.rows = 3;
            this.prevRow = 0;
            this.isCompacted = false;
            this.v[0] = 0;
            int cp = 1114112;
            this.v[1] = 1114112;
            int index = this.columns;
            while (true) {
                int cp2 = cp;
                if (cp2 <= 1114113) {
                    this.v[index] = cp2;
                    this.v[index + 1] = cp2 + 1;
                    index += this.columns;
                    cp = cp2 + 1;
                } else {
                    return;
                }
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("numOfColumns need to be no less than 1; but it is ");
        stringBuilder.append(numOfColumns);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public void setValue(int start, int end, int column, int value, int mask) {
        int i = start;
        int i2 = end;
        int i3 = column;
        int i4 = mask;
        if (i < 0 || i > i2 || i2 > 1114113 || i3 < 0 || i3 >= this.columns - 2) {
            throw new IllegalArgumentException();
        } else if (this.isCompacted) {
            throw new IllegalStateException("Shouldn't be called aftercompact()!");
        } else {
            int limit = i2 + 1;
            int column2 = i3 + 2;
            int value2 = value & i4;
            i3 = findRow(start);
            int lastRow = findRow(i2);
            boolean splitFirstRow = (i == this.v[i3] || value2 == (this.v[i3 + column2] & i4)) ? false : true;
            boolean splitLastRow = (limit == this.v[lastRow + 1] || value2 == (this.v[lastRow + column2] & i4)) ? false : true;
            if (splitFirstRow || splitLastRow) {
                int newMaxRows;
                int[] iArr;
                int rowsToExpand = 0;
                if (splitFirstRow) {
                    rowsToExpand = 0 + 1;
                }
                if (splitLastRow) {
                    rowsToExpand++;
                }
                int newMaxRows2 = 0;
                if (this.rows + rowsToExpand > this.maxRows) {
                    if (this.maxRows < 65536) {
                        newMaxRows = 65536;
                    } else if (this.maxRows < MAX_ROWS) {
                        newMaxRows = MAX_ROWS;
                    } else {
                        throw new IndexOutOfBoundsException("MAX_ROWS exceeded! Increase it to a higher valuein the implementation");
                    }
                    newMaxRows2 = newMaxRows;
                    int[] temp = new int[(this.columns * newMaxRows2)];
                    System.arraycopy(this.v, 0, temp, 0, this.rows * this.columns);
                    this.v = temp;
                    this.maxRows = newMaxRows2;
                }
                i2 = (this.rows * this.columns) - (this.columns + lastRow);
                if (i2 > 0) {
                    System.arraycopy(this.v, this.columns + lastRow, this.v, lastRow + ((1 + rowsToExpand) * this.columns), i2);
                }
                this.rows += rowsToExpand;
                if (splitFirstRow) {
                    System.arraycopy(this.v, i3, this.v, this.columns + i3, (lastRow - i3) + this.columns);
                    lastRow += this.columns;
                    iArr = this.v;
                    newMaxRows = i3 + 1;
                    this.v[this.columns + i3] = i;
                    iArr[newMaxRows] = i;
                    i3 += this.columns;
                }
                if (splitLastRow) {
                    System.arraycopy(this.v, lastRow, this.v, this.columns + lastRow, this.columns);
                    iArr = this.v;
                    newMaxRows = lastRow + 1;
                    this.v[this.columns + lastRow] = limit;
                    iArr[newMaxRows] = limit;
                }
            }
            this.prevRow = lastRow / this.columns;
            i3 += column2;
            i2 = lastRow + column2;
            int mask2 = ~i4;
            while (true) {
                this.v[i3] = (this.v[i3] & mask2) | value2;
                if (i3 != i2) {
                    i3 += this.columns;
                } else {
                    return;
                }
            }
        }
    }

    public int getValue(int c, int column) {
        if (this.isCompacted || c < 0 || c > 1114113 || column < 0 || column >= this.columns - 2) {
            return 0;
        }
        return this.v[(findRow(c) + 2) + column];
    }

    public int[] getRow(int rowIndex) {
        if (this.isCompacted) {
            throw new IllegalStateException("Illegal Invocation of the method after compact()");
        } else if (rowIndex < 0 || rowIndex > this.rows) {
            throw new IllegalArgumentException("rowIndex out of bound!");
        } else {
            int[] rowToReturn = new int[(this.columns - 2)];
            System.arraycopy(this.v, (this.columns * rowIndex) + 2, rowToReturn, 0, this.columns - 2);
            return rowToReturn;
        }
    }

    public int getRowStart(int rowIndex) {
        if (this.isCompacted) {
            throw new IllegalStateException("Illegal Invocation of the method after compact()");
        } else if (rowIndex >= 0 && rowIndex <= this.rows) {
            return this.v[this.columns * rowIndex];
        } else {
            throw new IllegalArgumentException("rowIndex out of bound!");
        }
    }

    public int getRowEnd(int rowIndex) {
        if (this.isCompacted) {
            throw new IllegalStateException("Illegal Invocation of the method after compact()");
        } else if (rowIndex >= 0 && rowIndex <= this.rows) {
            return this.v[(this.columns * rowIndex) + 1] - 1;
        } else {
            throw new IllegalArgumentException("rowIndex out of bound!");
        }
    }

    public void compact(CompactHandler compactor) {
        if (!this.isCompacted) {
            int i;
            int start;
            this.isCompacted = true;
            int valueColumns = this.columns - 2;
            Integer[] indexArray = new Integer[this.rows];
            int i2 = 0;
            for (i = 0; i < this.rows; i++) {
                indexArray[i] = Integer.valueOf(this.columns * i);
            }
            Arrays.sort(indexArray, new Comparator<Integer>() {
                public int compare(Integer o1, Integer o2) {
                    int indexOfRow1 = o1.intValue();
                    int indexOfRow2 = o2.intValue();
                    int count = PropsVectors.this.columns;
                    int index = 2;
                    while (PropsVectors.this.v[indexOfRow1 + index] == PropsVectors.this.v[indexOfRow2 + index]) {
                        index++;
                        if (index == PropsVectors.this.columns) {
                            index = 0;
                        }
                        count--;
                        if (count <= 0) {
                            return 0;
                        }
                    }
                    return PropsVectors.this.v[indexOfRow1 + index] < PropsVectors.this.v[indexOfRow2 + index] ? -1 : 1;
                }
            });
            int count = -valueColumns;
            i = 0;
            while (i < this.rows) {
                start = this.v[indexArray[i].intValue()];
                if (count < 0 || !areElementsSame(indexArray[i].intValue() + 2, this.v, indexArray[i - 1].intValue() + 2, valueColumns)) {
                    count += valueColumns;
                }
                if (start == 1114112) {
                    compactor.setRowIndexForInitialValue(count);
                } else if (start == 1114113) {
                    compactor.setRowIndexForErrorValue(count);
                }
                i++;
            }
            count += valueColumns;
            compactor.startRealValues(count);
            int[] temp = new int[count];
            count = -valueColumns;
            while (i2 < this.rows) {
                start = this.v[indexArray[i2].intValue()];
                int limit = this.v[indexArray[i2].intValue() + 1];
                if (count < 0 || !areElementsSame(indexArray[i2].intValue() + 2, temp, count, valueColumns)) {
                    count += valueColumns;
                    System.arraycopy(this.v, indexArray[i2].intValue() + 2, temp, count, valueColumns);
                }
                if (start < 1114112) {
                    compactor.setRowIndexForRange(start, limit - 1, count);
                }
                i2++;
            }
            this.v = temp;
            this.rows = (count / valueColumns) + 1;
        }
    }

    public int[] getCompactedArray() {
        if (this.isCompacted) {
            return this.v;
        }
        throw new IllegalStateException("Illegal Invocation of the method before compact()");
    }

    public int getCompactedRows() {
        if (this.isCompacted) {
            return this.rows;
        }
        throw new IllegalStateException("Illegal Invocation of the method before compact()");
    }

    public int getCompactedColumns() {
        if (this.isCompacted) {
            return this.columns - 2;
        }
        throw new IllegalStateException("Illegal Invocation of the method before compact()");
    }

    public IntTrie compactToTrieWithRowIndexes() {
        PVecToTrieCompactHandler compactor = new PVecToTrieCompactHandler();
        compact(compactor);
        return compactor.builder.serialize(new DefaultGetFoldedValue(compactor.builder), new DefaultGetFoldingOffset());
    }
}