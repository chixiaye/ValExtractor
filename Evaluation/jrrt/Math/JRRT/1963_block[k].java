package org.apache.commons.math3.linear;
import java.io.Serializable;
import org.apache.commons.math3.Field;
import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.MathUtils;
public class BlockFieldMatrix<T extends org.apache.commons.math3.FieldElement<org.apache.commons.math3.linear.BlockFieldMatrix@T>> extends AbstractFieldMatrix<T> implements Serializable  {
  final public static int BLOCK_SIZE = 36;
  final private static long serialVersionUID = -4602336630143123183L;
  final private T[][] blocks;
  final private int rows;
  final private int columns;
  final private int blockRows;
  final private int blockColumns;
  public BlockFieldMatrix(final Field<T> field, final int rows, final int columns) throws NotStrictlyPositiveException {
    super(field, rows, columns);
    this.rows = rows;
    this.columns = columns;
    blockRows = (rows + BLOCK_SIZE - 1) / BLOCK_SIZE;
    blockColumns = (columns + BLOCK_SIZE - 1) / BLOCK_SIZE;
    blocks = createBlocksLayout(field, rows, columns);
  }
  public BlockFieldMatrix(final T[][] rawData) throws DimensionMismatchException {
    this(rawData.length, rawData[0].length, toBlocksLayout(rawData), false);
  }
  public BlockFieldMatrix(final int rows, final int columns, final T[][] blockData, final boolean copyArray) throws DimensionMismatchException, NotStrictlyPositiveException {
    super(extractField(blockData), rows, columns);
    this.rows = rows;
    this.columns = columns;
    blockRows = (rows + BLOCK_SIZE - 1) / BLOCK_SIZE;
    blockColumns = (columns + BLOCK_SIZE - 1) / BLOCK_SIZE;
    if(copyArray) {
      blocks = MathArrays.buildArray(getField(), blockRows * blockColumns, -1);
    }
    else {
      blocks = blockData;
    }
    int index = 0;
    for(int iBlock = 0; iBlock < blockRows; ++iBlock) {
      final int iHeight = blockHeight(iBlock);
      for(int jBlock = 0; jBlock < blockColumns; ++jBlock, ++index) {
        if(blockData[index].length != iHeight * blockWidth(jBlock)) {
          throw new DimensionMismatchException(blockData[index].length, iHeight * blockWidth(jBlock));
        }
        if(copyArray) {
          blocks[index] = blockData[index].clone();
        }
      }
    }
  }
  public BlockFieldMatrix<T> add(final BlockFieldMatrix<T> m) throws MatrixDimensionMismatchException {
    checkAdditionCompatible(m);
    final BlockFieldMatrix<T> out = new BlockFieldMatrix<T>(getField(), rows, columns);
    for(int blockIndex = 0; blockIndex < out.blocks.length; ++blockIndex) {
      final T[] outBlock = out.blocks[blockIndex];
      final T[] tBlock = blocks[blockIndex];
      final T[] mBlock = m.blocks[blockIndex];
      for(int k = 0; k < outBlock.length; ++k) {
        outBlock[k] = tBlock[k].add(mBlock[k]);
      }
    }
    return out;
  }
  public BlockFieldMatrix<T> multiply(BlockFieldMatrix<T> m) throws DimensionMismatchException {
    checkMultiplicationCompatible(m);
    final BlockFieldMatrix<T> out = new BlockFieldMatrix<T>(getField(), rows, m.columns);
    final T zero = getField().getZero();
    int blockIndex = 0;
    for(int iBlock = 0; iBlock < out.blockRows; ++iBlock) {
      final int pStart = iBlock * BLOCK_SIZE;
      final int pEnd = FastMath.min(pStart + BLOCK_SIZE, rows);
      for(int jBlock = 0; jBlock < out.blockColumns; ++jBlock) {
        final int jWidth = out.blockWidth(jBlock);
        final int jWidth2 = jWidth + jWidth;
        final int jWidth3 = jWidth2 + jWidth;
        final int jWidth4 = jWidth3 + jWidth;
        final T[] outBlock = out.blocks[blockIndex];
        for(int kBlock = 0; kBlock < blockColumns; ++kBlock) {
          final int kWidth = blockWidth(kBlock);
          final T[] tBlock = blocks[iBlock * blockColumns + kBlock];
          final T[] mBlock = m.blocks[kBlock * m.blockColumns + jBlock];
          int k = 0;
          for(int p = pStart; p < pEnd; ++p) {
            final int lStart = (p - pStart) * kWidth;
            final int lEnd = lStart + kWidth;
            for(int nStart = 0; nStart < jWidth; ++nStart) {
              T sum = zero;
              int l = lStart;
              int n = nStart;
              while(l < lEnd - 3){
                sum = sum.add(tBlock[l].multiply(mBlock[n])).add(tBlock[l + 1].multiply(mBlock[n + jWidth])).add(tBlock[l + 2].multiply(mBlock[n + jWidth2])).add(tBlock[l + 3].multiply(mBlock[n + jWidth3]));
                l += 4;
                n += jWidth4;
              }
              while(l < lEnd){
                sum = sum.add(tBlock[l++].multiply(mBlock[n]));
                n += jWidth;
              }
              outBlock[k] = outBlock[k].add(sum);
              ++k;
            }
          }
        }
        ++blockIndex;
      }
    }
    return out;
  }
  public BlockFieldMatrix<T> subtract(final BlockFieldMatrix<T> m) throws MatrixDimensionMismatchException {
    checkSubtractionCompatible(m);
    final BlockFieldMatrix<T> out = new BlockFieldMatrix<T>(getField(), rows, columns);
    for(int blockIndex = 0; blockIndex < out.blocks.length; ++blockIndex) {
      final T[] outBlock = out.blocks[blockIndex];
      final T[] tBlock = blocks[blockIndex];
      final T[] mBlock = m.blocks[blockIndex];
      for(int k = 0; k < outBlock.length; ++k) {
        outBlock[k] = tBlock[k].subtract(mBlock[k]);
      }
    }
    return out;
  }
  @Override() public FieldMatrix<T> add(final FieldMatrix<T> m) throws MatrixDimensionMismatchException {
    try {
      return add((BlockFieldMatrix<T>)m);
    }
    catch (ClassCastException cce) {
      checkAdditionCompatible(m);
      final BlockFieldMatrix<T> out = new BlockFieldMatrix<T>(getField(), rows, columns);
      int blockIndex = 0;
      for(int iBlock = 0; iBlock < out.blockRows; ++iBlock) {
        for(int jBlock = 0; jBlock < out.blockColumns; ++jBlock) {
          final T[] outBlock = out.blocks[blockIndex];
          final T[] tBlock = blocks[blockIndex];
          final int pStart = iBlock * BLOCK_SIZE;
          final int pEnd = FastMath.min(pStart + BLOCK_SIZE, rows);
          final int qStart = jBlock * BLOCK_SIZE;
          final int qEnd = FastMath.min(qStart + BLOCK_SIZE, columns);
          int k = 0;
          for(int p = pStart; p < pEnd; ++p) {
            for(int q = qStart; q < qEnd; ++q) {
              outBlock[k] = tBlock[k].add(m.getEntry(p, q));
              ++k;
            }
          }
          ++blockIndex;
        }
      }
      return out;
    }
  }
  @Override() public FieldMatrix<T> copy() {
    BlockFieldMatrix<T> copied = new BlockFieldMatrix<T>(getField(), rows, columns);
    for(int i = 0; i < blocks.length; ++i) {
      System.arraycopy(blocks[i], 0, copied.blocks[i], 0, blocks[i].length);
    }
    return copied;
  }
  @Override() public FieldMatrix<T> createMatrix(final int rowDimension, final int columnDimension) throws NotStrictlyPositiveException {
    return new BlockFieldMatrix<T>(getField(), rowDimension, columnDimension);
  }
  @Override() public FieldMatrix<T> getColumnMatrix(final int column) throws OutOfRangeException {
    checkColumnIndex(column);
    final BlockFieldMatrix<T> out = new BlockFieldMatrix<T>(getField(), rows, 1);
    final int jBlock = column / BLOCK_SIZE;
    final int jColumn = column - jBlock * BLOCK_SIZE;
    final int jWidth = blockWidth(jBlock);
    int outBlockIndex = 0;
    int outIndex = 0;
    T[] outBlock = out.blocks[outBlockIndex];
    for(int iBlock = 0; iBlock < blockRows; ++iBlock) {
      final int iHeight = blockHeight(iBlock);
      final T[] block = blocks[iBlock * blockColumns + jBlock];
      for(int i = 0; i < iHeight; ++i) {
        if(outIndex >= outBlock.length) {
          outBlock = out.blocks[++outBlockIndex];
          outIndex = 0;
        }
        outBlock[outIndex++] = block[i * jWidth + jColumn];
      }
    }
    return out;
  }
  @Override() public FieldMatrix<T> getRowMatrix(final int row) throws OutOfRangeException {
    checkRowIndex(row);
    final BlockFieldMatrix<T> out = new BlockFieldMatrix<T>(getField(), 1, columns);
    final int iBlock = row / BLOCK_SIZE;
    final int iRow = row - iBlock * BLOCK_SIZE;
    int outBlockIndex = 0;
    int outIndex = 0;
    T[] outBlock = out.blocks[outBlockIndex];
    for(int jBlock = 0; jBlock < blockColumns; ++jBlock) {
      final int jWidth = blockWidth(jBlock);
      final T[] block = blocks[iBlock * blockColumns + jBlock];
      final int available = outBlock.length - outIndex;
      if(jWidth > available) {
        System.arraycopy(block, iRow * jWidth, outBlock, outIndex, available);
        outBlock = out.blocks[++outBlockIndex];
        System.arraycopy(block, iRow * jWidth, outBlock, 0, jWidth - available);
        outIndex = jWidth - available;
      }
      else {
        System.arraycopy(block, iRow * jWidth, outBlock, outIndex, jWidth);
        outIndex += jWidth;
      }
    }
    return out;
  }
  @Override() public FieldMatrix<T> getSubMatrix(final int startRow, final int endRow, final int startColumn, final int endColumn) throws OutOfRangeException, NumberIsTooSmallException {
    checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
    final BlockFieldMatrix<T> out = new BlockFieldMatrix<T>(getField(), endRow - startRow + 1, endColumn - startColumn + 1);
    final int blockStartRow = startRow / BLOCK_SIZE;
    final int rowsShift = startRow % BLOCK_SIZE;
    final int blockStartColumn = startColumn / BLOCK_SIZE;
    final int columnsShift = startColumn % BLOCK_SIZE;
    int pBlock = blockStartRow;
    for(int iBlock = 0; iBlock < out.blockRows; ++iBlock) {
      final int iHeight = out.blockHeight(iBlock);
      int qBlock = blockStartColumn;
      for(int jBlock = 0; jBlock < out.blockColumns; ++jBlock) {
        final int jWidth = out.blockWidth(jBlock);
        final int outIndex = iBlock * out.blockColumns + jBlock;
        final T[] outBlock = out.blocks[outIndex];
        final int index = pBlock * blockColumns + qBlock;
        final int width = blockWidth(qBlock);
        final int heightExcess = iHeight + rowsShift - BLOCK_SIZE;
        final int widthExcess = jWidth + columnsShift - BLOCK_SIZE;
        if(heightExcess > 0) {
          if(widthExcess > 0) {
            final int width2 = blockWidth(qBlock + 1);
            copyBlockPart(blocks[index], width, rowsShift, BLOCK_SIZE, columnsShift, BLOCK_SIZE, outBlock, jWidth, 0, 0);
            copyBlockPart(blocks[index + 1], width2, rowsShift, BLOCK_SIZE, 0, widthExcess, outBlock, jWidth, 0, jWidth - widthExcess);
            copyBlockPart(blocks[index + blockColumns], width, 0, heightExcess, columnsShift, BLOCK_SIZE, outBlock, jWidth, iHeight - heightExcess, 0);
            copyBlockPart(blocks[index + blockColumns + 1], width2, 0, heightExcess, 0, widthExcess, outBlock, jWidth, iHeight - heightExcess, jWidth - widthExcess);
          }
          else {
            copyBlockPart(blocks[index], width, rowsShift, BLOCK_SIZE, columnsShift, jWidth + columnsShift, outBlock, jWidth, 0, 0);
            copyBlockPart(blocks[index + blockColumns], width, 0, heightExcess, columnsShift, jWidth + columnsShift, outBlock, jWidth, iHeight - heightExcess, 0);
          }
        }
        else {
          if(widthExcess > 0) {
            final int width2 = blockWidth(qBlock + 1);
            copyBlockPart(blocks[index], width, rowsShift, iHeight + rowsShift, columnsShift, BLOCK_SIZE, outBlock, jWidth, 0, 0);
            copyBlockPart(blocks[index + 1], width2, rowsShift, iHeight + rowsShift, 0, widthExcess, outBlock, jWidth, 0, jWidth - widthExcess);
          }
          else {
            copyBlockPart(blocks[index], width, rowsShift, iHeight + rowsShift, columnsShift, jWidth + columnsShift, outBlock, jWidth, 0, 0);
          }
        }
        ++qBlock;
      }
      ++pBlock;
    }
    return out;
  }
  @Override() public FieldMatrix<T> multiply(final FieldMatrix<T> m) throws DimensionMismatchException {
    try {
      return multiply((BlockFieldMatrix<T>)m);
    }
    catch (ClassCastException cce) {
      checkMultiplicationCompatible(m);
      final BlockFieldMatrix<T> out = new BlockFieldMatrix<T>(getField(), rows, m.getColumnDimension());
      final T zero = getField().getZero();
      int blockIndex = 0;
      for(int iBlock = 0; iBlock < out.blockRows; ++iBlock) {
        final int pStart = iBlock * BLOCK_SIZE;
        final int pEnd = FastMath.min(pStart + BLOCK_SIZE, rows);
        for(int jBlock = 0; jBlock < out.blockColumns; ++jBlock) {
          final int qStart = jBlock * BLOCK_SIZE;
          final int qEnd = FastMath.min(qStart + BLOCK_SIZE, m.getColumnDimension());
          final T[] outBlock = out.blocks[blockIndex];
          for(int kBlock = 0; kBlock < blockColumns; ++kBlock) {
            final int kWidth = blockWidth(kBlock);
            final T[] tBlock = blocks[iBlock * blockColumns + kBlock];
            final int rStart = kBlock * BLOCK_SIZE;
            int k = 0;
            for(int p = pStart; p < pEnd; ++p) {
              final int lStart = (p - pStart) * kWidth;
              final int lEnd = lStart + kWidth;
              for(int q = qStart; q < qEnd; ++q) {
                T sum = zero;
                int r = rStart;
                for(int l = lStart; l < lEnd; ++l) {
                  sum = sum.add(tBlock[l].multiply(m.getEntry(r, q)));
                  ++r;
                }
                outBlock[k] = outBlock[k].add(sum);
                ++k;
              }
            }
          }
          ++blockIndex;
        }
      }
      return out;
    }
  }
  @Override() public FieldMatrix<T> scalarAdd(final T d) {
    final BlockFieldMatrix<T> out = new BlockFieldMatrix<T>(getField(), rows, columns);
    for(int blockIndex = 0; blockIndex < out.blocks.length; ++blockIndex) {
      final T[] outBlock = out.blocks[blockIndex];
      final T[] tBlock = blocks[blockIndex];
      for(int k = 0; k < outBlock.length; ++k) {
        outBlock[k] = tBlock[k].add(d);
      }
    }
    return out;
  }
  @Override() public FieldMatrix<T> scalarMultiply(final T d) {
    final BlockFieldMatrix<T> out = new BlockFieldMatrix<T>(getField(), rows, columns);
    for(int blockIndex = 0; blockIndex < out.blocks.length; ++blockIndex) {
      final T[] outBlock = out.blocks[blockIndex];
      final T[] tBlock = blocks[blockIndex];
      for(int k = 0; k < outBlock.length; ++k) {
        outBlock[k] = tBlock[k].multiply(d);
      }
    }
    return out;
  }
  @Override() public FieldMatrix<T> subtract(final FieldMatrix<T> m) throws MatrixDimensionMismatchException {
    try {
      return subtract((BlockFieldMatrix<T>)m);
    }
    catch (ClassCastException cce) {
      checkSubtractionCompatible(m);
      final BlockFieldMatrix<T> out = new BlockFieldMatrix<T>(getField(), rows, columns);
      int blockIndex = 0;
      for(int iBlock = 0; iBlock < out.blockRows; ++iBlock) {
        for(int jBlock = 0; jBlock < out.blockColumns; ++jBlock) {
          final T[] outBlock = out.blocks[blockIndex];
          final T[] tBlock = blocks[blockIndex];
          final int pStart = iBlock * BLOCK_SIZE;
          final int pEnd = FastMath.min(pStart + BLOCK_SIZE, rows);
          final int qStart = jBlock * BLOCK_SIZE;
          final int qEnd = FastMath.min(qStart + BLOCK_SIZE, columns);
          int k = 0;
          for(int p = pStart; p < pEnd; ++p) {
            for(int q = qStart; q < qEnd; ++q) {
              outBlock[k] = tBlock[k].subtract(m.getEntry(p, q));
              ++k;
            }
          }
          ++blockIndex;
        }
      }
      return out;
    }
  }
  @Override() public FieldMatrix<T> transpose() {
    final int nRows = getRowDimension();
    final int nCols = getColumnDimension();
    final BlockFieldMatrix<T> out = new BlockFieldMatrix<T>(getField(), nCols, nRows);
    int blockIndex = 0;
    for(int iBlock = 0; iBlock < blockColumns; ++iBlock) {
      for(int jBlock = 0; jBlock < blockRows; ++jBlock) {
        final T[] outBlock = out.blocks[blockIndex];
        final T[] tBlock = blocks[jBlock * blockColumns + iBlock];
        final int pStart = iBlock * BLOCK_SIZE;
        final int pEnd = FastMath.min(pStart + BLOCK_SIZE, columns);
        final int qStart = jBlock * BLOCK_SIZE;
        final int qEnd = FastMath.min(qStart + BLOCK_SIZE, rows);
        int k = 0;
        for(int p = pStart; p < pEnd; ++p) {
          final int lInc = pEnd - pStart;
          int l = p - pStart;
          for(int q = qStart; q < qEnd; ++q) {
            outBlock[k] = tBlock[l];
            ++k;
            l += lInc;
          }
        }
        ++blockIndex;
      }
    }
    return out;
  }
  @Override() public FieldVector<T> getColumnVector(final int column) throws OutOfRangeException {
    checkColumnIndex(column);
    final T[] outData = MathArrays.buildArray(getField(), rows);
    final int jBlock = column / BLOCK_SIZE;
    final int jColumn = column - jBlock * BLOCK_SIZE;
    final int jWidth = blockWidth(jBlock);
    int outIndex = 0;
    for(int iBlock = 0; iBlock < blockRows; ++iBlock) {
      final int iHeight = blockHeight(iBlock);
      final T[] block = blocks[iBlock * blockColumns + jBlock];
      for(int i = 0; i < iHeight; ++i) {
        outData[outIndex++] = block[i * jWidth + jColumn];
      }
    }
    return new ArrayFieldVector<T>(getField(), outData, false);
  }
  @Override() public FieldVector<T> getRowVector(final int row) throws OutOfRangeException {
    checkRowIndex(row);
    final T[] outData = MathArrays.buildArray(getField(), columns);
    final int iBlock = row / BLOCK_SIZE;
    final int iRow = row - iBlock * BLOCK_SIZE;
    int outIndex = 0;
    for(int jBlock = 0; jBlock < blockColumns; ++jBlock) {
      final int jWidth = blockWidth(jBlock);
      final T[] block = blocks[iBlock * blockColumns + jBlock];
      System.arraycopy(block, iRow * jWidth, outData, outIndex, jWidth);
      outIndex += jWidth;
    }
    return new ArrayFieldVector<T>(getField(), outData, false);
  }
  @Override() public T getEntry(final int row, final int column) throws OutOfRangeException {
    checkRowIndex(row);
    checkColumnIndex(column);
    final int iBlock = row / BLOCK_SIZE;
    final int jBlock = column / BLOCK_SIZE;
    final int k = (row - iBlock * BLOCK_SIZE) * blockWidth(jBlock) + (column - jBlock * BLOCK_SIZE);
    return blocks[iBlock * blockColumns + jBlock][k];
  }
  @Override() public T walkInOptimizedOrder(final FieldMatrixChangingVisitor<T> visitor) {
    visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
    int blockIndex = 0;
    for(int iBlock = 0; iBlock < blockRows; ++iBlock) {
      final int pStart = iBlock * BLOCK_SIZE;
      final int pEnd = FastMath.min(pStart + BLOCK_SIZE, rows);
      for(int jBlock = 0; jBlock < blockColumns; ++jBlock) {
        final int qStart = jBlock * BLOCK_SIZE;
        final int qEnd = FastMath.min(qStart + BLOCK_SIZE, columns);
        final T[] block = blocks[blockIndex];
        int k = 0;
        for(int p = pStart; p < pEnd; ++p) {
          for(int q = qStart; q < qEnd; ++q) {
            block[k] = visitor.visit(p, q, block[k]);
            ++k;
          }
        }
        ++blockIndex;
      }
    }
    return visitor.end();
  }
  @Override() public T walkInOptimizedOrder(final FieldMatrixChangingVisitor<T> visitor, final int startRow, final int endRow, final int startColumn, final int endColumn) throws OutOfRangeException, NumberIsTooSmallException {
    checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
    visitor.start(rows, columns, startRow, endRow, startColumn, endColumn);
    for(int iBlock = startRow / BLOCK_SIZE; iBlock < 1 + endRow / BLOCK_SIZE; ++iBlock) {
      final int p0 = iBlock * BLOCK_SIZE;
      final int pStart = FastMath.max(startRow, p0);
      final int pEnd = FastMath.min((iBlock + 1) * BLOCK_SIZE, 1 + endRow);
      for(int jBlock = startColumn / BLOCK_SIZE; jBlock < 1 + endColumn / BLOCK_SIZE; ++jBlock) {
        final int jWidth = blockWidth(jBlock);
        final int q0 = jBlock * BLOCK_SIZE;
        final int qStart = FastMath.max(startColumn, q0);
        final int qEnd = FastMath.min((jBlock + 1) * BLOCK_SIZE, 1 + endColumn);
        final T[] block = blocks[iBlock * blockColumns + jBlock];
        for(int p = pStart; p < pEnd; ++p) {
          int k = (p - p0) * jWidth + qStart - q0;
          for(int q = qStart; q < qEnd; ++q) {
            block[k] = visitor.visit(p, q, block[k]);
            ++k;
          }
        }
      }
    }
    return visitor.end();
  }
  @Override() public T walkInOptimizedOrder(final FieldMatrixPreservingVisitor<T> visitor) {
    visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
    int blockIndex = 0;
    for(int iBlock = 0; iBlock < blockRows; ++iBlock) {
      final int pStart = iBlock * BLOCK_SIZE;
      final int pEnd = FastMath.min(pStart + BLOCK_SIZE, rows);
      for(int jBlock = 0; jBlock < blockColumns; ++jBlock) {
        final int qStart = jBlock * BLOCK_SIZE;
        final int qEnd = FastMath.min(qStart + BLOCK_SIZE, columns);
        final T[] block = blocks[blockIndex];
        int k = 0;
        for(int p = pStart; p < pEnd; ++p) {
          for(int q = qStart; q < qEnd; ++q) {
            visitor.visit(p, q, block[k]);
            ++k;
          }
        }
        ++blockIndex;
      }
    }
    return visitor.end();
  }
  @Override() public T walkInOptimizedOrder(final FieldMatrixPreservingVisitor<T> visitor, final int startRow, final int endRow, final int startColumn, final int endColumn) throws OutOfRangeException, NumberIsTooSmallException {
    checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
    visitor.start(rows, columns, startRow, endRow, startColumn, endColumn);
    for(int iBlock = startRow / BLOCK_SIZE; iBlock < 1 + endRow / BLOCK_SIZE; ++iBlock) {
      final int p0 = iBlock * BLOCK_SIZE;
      final int pStart = FastMath.max(startRow, p0);
      final int pEnd = FastMath.min((iBlock + 1) * BLOCK_SIZE, 1 + endRow);
      for(int jBlock = startColumn / BLOCK_SIZE; jBlock < 1 + endColumn / BLOCK_SIZE; ++jBlock) {
        final int jWidth = blockWidth(jBlock);
        final int q0 = jBlock * BLOCK_SIZE;
        final int qStart = FastMath.max(startColumn, q0);
        final int qEnd = FastMath.min((jBlock + 1) * BLOCK_SIZE, 1 + endColumn);
        final T[] block = blocks[iBlock * blockColumns + jBlock];
        for(int p = pStart; p < pEnd; ++p) {
          int k = (p - p0) * jWidth + qStart - q0;
          for(int q = qStart; q < qEnd; ++q) {
            visitor.visit(p, q, block[k]);
            ++k;
          }
        }
      }
    }
    return visitor.end();
  }
  @Override() public T walkInRowOrder(final FieldMatrixChangingVisitor<T> visitor) {
    visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
    for(int iBlock = 0; iBlock < blockRows; ++iBlock) {
      final int pStart = iBlock * BLOCK_SIZE;
      final int pEnd = FastMath.min(pStart + BLOCK_SIZE, rows);
      for(int p = pStart; p < pEnd; ++p) {
        for(int jBlock = 0; jBlock < blockColumns; ++jBlock) {
          final int jWidth = blockWidth(jBlock);
          final int qStart = jBlock * BLOCK_SIZE;
          final int qEnd = FastMath.min(qStart + BLOCK_SIZE, columns);
          final T[] block = blocks[iBlock * blockColumns + jBlock];
          int k = (p - pStart) * jWidth;
          for(int q = qStart; q < qEnd; ++q) {
            T var_1963 = block[k];
            block[k] = visitor.visit(p, q, var_1963);
            ++k;
          }
        }
      }
    }
    return visitor.end();
  }
  @Override() public T walkInRowOrder(final FieldMatrixChangingVisitor<T> visitor, final int startRow, final int endRow, final int startColumn, final int endColumn) throws OutOfRangeException, NumberIsTooSmallException {
    checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
    visitor.start(rows, columns, startRow, endRow, startColumn, endColumn);
    for(int iBlock = startRow / BLOCK_SIZE; iBlock < 1 + endRow / BLOCK_SIZE; ++iBlock) {
      final int p0 = iBlock * BLOCK_SIZE;
      final int pStart = FastMath.max(startRow, p0);
      final int pEnd = FastMath.min((iBlock + 1) * BLOCK_SIZE, 1 + endRow);
      for(int p = pStart; p < pEnd; ++p) {
        for(int jBlock = startColumn / BLOCK_SIZE; jBlock < 1 + endColumn / BLOCK_SIZE; ++jBlock) {
          final int jWidth = blockWidth(jBlock);
          final int q0 = jBlock * BLOCK_SIZE;
          final int qStart = FastMath.max(startColumn, q0);
          final int qEnd = FastMath.min((jBlock + 1) * BLOCK_SIZE, 1 + endColumn);
          final T[] block = blocks[iBlock * blockColumns + jBlock];
          int k = (p - p0) * jWidth + qStart - q0;
          for(int q = qStart; q < qEnd; ++q) {
            block[k] = visitor.visit(p, q, block[k]);
            ++k;
          }
        }
      }
    }
    return visitor.end();
  }
  @Override() public T walkInRowOrder(final FieldMatrixPreservingVisitor<T> visitor) {
    visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
    for(int iBlock = 0; iBlock < blockRows; ++iBlock) {
      final int pStart = iBlock * BLOCK_SIZE;
      final int pEnd = FastMath.min(pStart + BLOCK_SIZE, rows);
      for(int p = pStart; p < pEnd; ++p) {
        for(int jBlock = 0; jBlock < blockColumns; ++jBlock) {
          final int jWidth = blockWidth(jBlock);
          final int qStart = jBlock * BLOCK_SIZE;
          final int qEnd = FastMath.min(qStart + BLOCK_SIZE, columns);
          final T[] block = blocks[iBlock * blockColumns + jBlock];
          int k = (p - pStart) * jWidth;
          for(int q = qStart; q < qEnd; ++q) {
            visitor.visit(p, q, block[k]);
            ++k;
          }
        }
      }
    }
    return visitor.end();
  }
  @Override() public T walkInRowOrder(final FieldMatrixPreservingVisitor<T> visitor, final int startRow, final int endRow, final int startColumn, final int endColumn) throws OutOfRangeException, NumberIsTooSmallException {
    checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
    visitor.start(rows, columns, startRow, endRow, startColumn, endColumn);
    for(int iBlock = startRow / BLOCK_SIZE; iBlock < 1 + endRow / BLOCK_SIZE; ++iBlock) {
      final int p0 = iBlock * BLOCK_SIZE;
      final int pStart = FastMath.max(startRow, p0);
      final int pEnd = FastMath.min((iBlock + 1) * BLOCK_SIZE, 1 + endRow);
      for(int p = pStart; p < pEnd; ++p) {
        for(int jBlock = startColumn / BLOCK_SIZE; jBlock < 1 + endColumn / BLOCK_SIZE; ++jBlock) {
          final int jWidth = blockWidth(jBlock);
          final int q0 = jBlock * BLOCK_SIZE;
          final int qStart = FastMath.max(startColumn, q0);
          final int qEnd = FastMath.min((jBlock + 1) * BLOCK_SIZE, 1 + endColumn);
          final T[] block = blocks[iBlock * blockColumns + jBlock];
          int k = (p - p0) * jWidth + qStart - q0;
          for(int q = qStart; q < qEnd; ++q) {
            visitor.visit(p, q, block[k]);
            ++k;
          }
        }
      }
    }
    return visitor.end();
  }
  @Override() public T[] getColumn(final int column) throws OutOfRangeException {
    checkColumnIndex(column);
    final T[] out = MathArrays.buildArray(getField(), rows);
    final int jBlock = column / BLOCK_SIZE;
    final int jColumn = column - jBlock * BLOCK_SIZE;
    final int jWidth = blockWidth(jBlock);
    int outIndex = 0;
    for(int iBlock = 0; iBlock < blockRows; ++iBlock) {
      final int iHeight = blockHeight(iBlock);
      final T[] block = blocks[iBlock * blockColumns + jBlock];
      for(int i = 0; i < iHeight; ++i) {
        out[outIndex++] = block[i * jWidth + jColumn];
      }
    }
    return out;
  }
  @Override() public T[] getRow(final int row) throws OutOfRangeException {
    checkRowIndex(row);
    final T[] out = MathArrays.buildArray(getField(), columns);
    final int iBlock = row / BLOCK_SIZE;
    final int iRow = row - iBlock * BLOCK_SIZE;
    int outIndex = 0;
    for(int jBlock = 0; jBlock < blockColumns; ++jBlock) {
      final int jWidth = blockWidth(jBlock);
      final T[] block = blocks[iBlock * blockColumns + jBlock];
      System.arraycopy(block, iRow * jWidth, out, outIndex, jWidth);
      outIndex += jWidth;
    }
    return out;
  }
  @Override() public T[] operate(final T[] v) throws DimensionMismatchException {
    if(v.length != columns) {
      throw new DimensionMismatchException(v.length, columns);
    }
    final T[] out = MathArrays.buildArray(getField(), rows);
    final T zero = getField().getZero();
    for(int iBlock = 0; iBlock < blockRows; ++iBlock) {
      final int pStart = iBlock * BLOCK_SIZE;
      final int pEnd = FastMath.min(pStart + BLOCK_SIZE, rows);
      for(int jBlock = 0; jBlock < blockColumns; ++jBlock) {
        final T[] block = blocks[iBlock * blockColumns + jBlock];
        final int qStart = jBlock * BLOCK_SIZE;
        final int qEnd = FastMath.min(qStart + BLOCK_SIZE, columns);
        int k = 0;
        for(int p = pStart; p < pEnd; ++p) {
          T sum = zero;
          int q = qStart;
          while(q < qEnd - 3){
            sum = sum.add(block[k].multiply(v[q])).add(block[k + 1].multiply(v[q + 1])).add(block[k + 2].multiply(v[q + 2])).add(block[k + 3].multiply(v[q + 3]));
            k += 4;
            q += 4;
          }
          while(q < qEnd){
            sum = sum.add(block[k++].multiply(v[q++]));
          }
          out[p] = out[p].add(sum);
        }
      }
    }
    return out;
  }
  @Override() public T[] preMultiply(final T[] v) throws DimensionMismatchException {
    if(v.length != rows) {
      throw new DimensionMismatchException(v.length, rows);
    }
    final T[] out = MathArrays.buildArray(getField(), columns);
    final T zero = getField().getZero();
    for(int jBlock = 0; jBlock < blockColumns; ++jBlock) {
      final int jWidth = blockWidth(jBlock);
      final int jWidth2 = jWidth + jWidth;
      final int jWidth3 = jWidth2 + jWidth;
      final int jWidth4 = jWidth3 + jWidth;
      final int qStart = jBlock * BLOCK_SIZE;
      final int qEnd = FastMath.min(qStart + BLOCK_SIZE, columns);
      for(int iBlock = 0; iBlock < blockRows; ++iBlock) {
        final T[] block = blocks[iBlock * blockColumns + jBlock];
        final int pStart = iBlock * BLOCK_SIZE;
        final int pEnd = FastMath.min(pStart + BLOCK_SIZE, rows);
        for(int q = qStart; q < qEnd; ++q) {
          int k = q - qStart;
          T sum = zero;
          int p = pStart;
          while(p < pEnd - 3){
            sum = sum.add(block[k].multiply(v[p])).add(block[k + jWidth].multiply(v[p + 1])).add(block[k + jWidth2].multiply(v[p + 2])).add(block[k + jWidth3].multiply(v[p + 3]));
            k += jWidth4;
            p += 4;
          }
          while(p < pEnd){
            sum = sum.add(block[k].multiply(v[p++]));
            k += jWidth;
          }
          out[q] = out[q].add(sum);
        }
      }
    }
    return out;
  }
  public static  <T extends org.apache.commons.math3.FieldElement<org.apache.commons.math3.linear.T>> T[][] createBlocksLayout(final Field<T> field, final int rows, final int columns) {
    final int blockRows = (rows + BLOCK_SIZE - 1) / BLOCK_SIZE;
    final int blockColumns = (columns + BLOCK_SIZE - 1) / BLOCK_SIZE;
    final T[][] blocks = MathArrays.buildArray(field, blockRows * blockColumns, -1);
    int blockIndex = 0;
    for(int iBlock = 0; iBlock < blockRows; ++iBlock) {
      final int pStart = iBlock * BLOCK_SIZE;
      final int pEnd = FastMath.min(pStart + BLOCK_SIZE, rows);
      final int iHeight = pEnd - pStart;
      for(int jBlock = 0; jBlock < blockColumns; ++jBlock) {
        final int qStart = jBlock * BLOCK_SIZE;
        final int qEnd = FastMath.min(qStart + BLOCK_SIZE, columns);
        final int jWidth = qEnd - qStart;
        blocks[blockIndex] = MathArrays.buildArray(field, iHeight * jWidth);
        ++blockIndex;
      }
    }
    return blocks;
  }
  @Override() public T[][] getData() {
    final T[][] data = MathArrays.buildArray(getField(), getRowDimension(), getColumnDimension());
    final int lastColumns = columns - (blockColumns - 1) * BLOCK_SIZE;
    for(int iBlock = 0; iBlock < blockRows; ++iBlock) {
      final int pStart = iBlock * BLOCK_SIZE;
      final int pEnd = FastMath.min(pStart + BLOCK_SIZE, rows);
      int regularPos = 0;
      int lastPos = 0;
      for(int p = pStart; p < pEnd; ++p) {
        final T[] dataP = data[p];
        int blockIndex = iBlock * blockColumns;
        int dataPos = 0;
        for(int jBlock = 0; jBlock < blockColumns - 1; ++jBlock) {
          System.arraycopy(blocks[blockIndex++], regularPos, dataP, dataPos, BLOCK_SIZE);
          dataPos += BLOCK_SIZE;
        }
        System.arraycopy(blocks[blockIndex], lastPos, dataP, dataPos, lastColumns);
        regularPos += BLOCK_SIZE;
        lastPos += lastColumns;
      }
    }
    return data;
  }
  public static  <T extends org.apache.commons.math3.FieldElement<org.apache.commons.math3.linear.T>> T[][] toBlocksLayout(final T[][] rawData) throws DimensionMismatchException {
    final int rows = rawData.length;
    final int columns = rawData[0].length;
    final int blockRows = (rows + BLOCK_SIZE - 1) / BLOCK_SIZE;
    final int blockColumns = (columns + BLOCK_SIZE - 1) / BLOCK_SIZE;
    for(int i = 0; i < rawData.length; ++i) {
      final int length = rawData[i].length;
      if(length != columns) {
        throw new DimensionMismatchException(columns, length);
      }
    }
    final Field<T> field = extractField(rawData);
    final T[][] blocks = MathArrays.buildArray(field, blockRows * blockColumns, -1);
    int blockIndex = 0;
    for(int iBlock = 0; iBlock < blockRows; ++iBlock) {
      final int pStart = iBlock * BLOCK_SIZE;
      final int pEnd = FastMath.min(pStart + BLOCK_SIZE, rows);
      final int iHeight = pEnd - pStart;
      for(int jBlock = 0; jBlock < blockColumns; ++jBlock) {
        final int qStart = jBlock * BLOCK_SIZE;
        final int qEnd = FastMath.min(qStart + BLOCK_SIZE, columns);
        final int jWidth = qEnd - qStart;
        final T[] block = MathArrays.buildArray(field, iHeight * jWidth);
        blocks[blockIndex] = block;
        int index = 0;
        for(int p = pStart; p < pEnd; ++p) {
          System.arraycopy(rawData[p], qStart, block, index, jWidth);
          index += jWidth;
        }
        ++blockIndex;
      }
    }
    return blocks;
  }
  private int blockHeight(final int blockRow) {
    return (blockRow == blockRows - 1) ? rows - blockRow * BLOCK_SIZE : BLOCK_SIZE;
  }
  private int blockWidth(final int blockColumn) {
    return (blockColumn == blockColumns - 1) ? columns - blockColumn * BLOCK_SIZE : BLOCK_SIZE;
  }
  @Override() public int getColumnDimension() {
    return columns;
  }
  @Override() public int getRowDimension() {
    return rows;
  }
  @Override() public void addToEntry(final int row, final int column, final T increment) throws OutOfRangeException {
    checkRowIndex(row);
    checkColumnIndex(column);
    final int iBlock = row / BLOCK_SIZE;
    final int jBlock = column / BLOCK_SIZE;
    final int k = (row - iBlock * BLOCK_SIZE) * blockWidth(jBlock) + (column - jBlock * BLOCK_SIZE);
    final T[] blockIJ = blocks[iBlock * blockColumns + jBlock];
    blockIJ[k] = blockIJ[k].add(increment);
  }
  private void copyBlockPart(final T[] srcBlock, final int srcWidth, final int srcStartRow, final int srcEndRow, final int srcStartColumn, final int srcEndColumn, final T[] dstBlock, final int dstWidth, final int dstStartRow, final int dstStartColumn) {
    final int length = srcEndColumn - srcStartColumn;
    int srcPos = srcStartRow * srcWidth + srcStartColumn;
    int dstPos = dstStartRow * dstWidth + dstStartColumn;
    for(int srcRow = srcStartRow; srcRow < srcEndRow; ++srcRow) {
      System.arraycopy(srcBlock, srcPos, dstBlock, dstPos, length);
      srcPos += srcWidth;
      dstPos += dstWidth;
    }
  }
  @Override() public void multiplyEntry(final int row, final int column, final T factor) throws OutOfRangeException {
    checkRowIndex(row);
    checkColumnIndex(column);
    final int iBlock = row / BLOCK_SIZE;
    final int jBlock = column / BLOCK_SIZE;
    final int k = (row - iBlock * BLOCK_SIZE) * blockWidth(jBlock) + (column - jBlock * BLOCK_SIZE);
    final T[] blockIJ = blocks[iBlock * blockColumns + jBlock];
    blockIJ[k] = blockIJ[k].multiply(factor);
  }
  @Override() public void setColumn(final int column, final T[] array) throws MatrixDimensionMismatchException, OutOfRangeException {
    checkColumnIndex(column);
    final int nRows = getRowDimension();
    if(array.length != nRows) {
      throw new MatrixDimensionMismatchException(array.length, 1, nRows, 1);
    }
    final int jBlock = column / BLOCK_SIZE;
    final int jColumn = column - jBlock * BLOCK_SIZE;
    final int jWidth = blockWidth(jBlock);
    int outIndex = 0;
    for(int iBlock = 0; iBlock < blockRows; ++iBlock) {
      final int iHeight = blockHeight(iBlock);
      final T[] block = blocks[iBlock * blockColumns + jBlock];
      for(int i = 0; i < iHeight; ++i) {
        block[i * jWidth + jColumn] = array[outIndex++];
      }
    }
  }
  void setColumnMatrix(final int column, final BlockFieldMatrix<T> matrix) throws MatrixDimensionMismatchException, OutOfRangeException {
    checkColumnIndex(column);
    final int nRows = getRowDimension();
    if((matrix.getRowDimension() != nRows) || (matrix.getColumnDimension() != 1)) {
      throw new MatrixDimensionMismatchException(matrix.getRowDimension(), matrix.getColumnDimension(), nRows, 1);
    }
    final int jBlock = column / BLOCK_SIZE;
    final int jColumn = column - jBlock * BLOCK_SIZE;
    final int jWidth = blockWidth(jBlock);
    int mBlockIndex = 0;
    int mIndex = 0;
    T[] mBlock = matrix.blocks[mBlockIndex];
    for(int iBlock = 0; iBlock < blockRows; ++iBlock) {
      final int iHeight = blockHeight(iBlock);
      final T[] block = blocks[iBlock * blockColumns + jBlock];
      for(int i = 0; i < iHeight; ++i) {
        if(mIndex >= mBlock.length) {
          mBlock = matrix.blocks[++mBlockIndex];
          mIndex = 0;
        }
        block[i * jWidth + jColumn] = mBlock[mIndex++];
      }
    }
  }
  @Override() public void setColumnMatrix(final int column, final FieldMatrix<T> matrix) throws MatrixDimensionMismatchException, OutOfRangeException {
    try {
      setColumnMatrix(column, (BlockFieldMatrix<T>)matrix);
    }
    catch (ClassCastException cce) {
      super.setColumnMatrix(column, matrix);
    }
  }
  @Override() public void setColumnVector(final int column, final FieldVector<T> vector) throws OutOfRangeException, MatrixDimensionMismatchException {
    try {
      setColumn(column, ((ArrayFieldVector<T>)vector).getDataRef());
    }
    catch (ClassCastException cce) {
      super.setColumnVector(column, vector);
    }
  }
  @Override() public void setEntry(final int row, final int column, final T value) throws OutOfRangeException {
    checkRowIndex(row);
    checkColumnIndex(column);
    final int iBlock = row / BLOCK_SIZE;
    final int jBlock = column / BLOCK_SIZE;
    final int k = (row - iBlock * BLOCK_SIZE) * blockWidth(jBlock) + (column - jBlock * BLOCK_SIZE);
    blocks[iBlock * blockColumns + jBlock][k] = value;
  }
  @Override() public void setRow(final int row, final T[] array) throws OutOfRangeException, MatrixDimensionMismatchException {
    checkRowIndex(row);
    final int nCols = getColumnDimension();
    if(array.length != nCols) {
      throw new MatrixDimensionMismatchException(1, array.length, 1, nCols);
    }
    final int iBlock = row / BLOCK_SIZE;
    final int iRow = row - iBlock * BLOCK_SIZE;
    int outIndex = 0;
    for(int jBlock = 0; jBlock < blockColumns; ++jBlock) {
      final int jWidth = blockWidth(jBlock);
      final T[] block = blocks[iBlock * blockColumns + jBlock];
      System.arraycopy(array, outIndex, block, iRow * jWidth, jWidth);
      outIndex += jWidth;
    }
  }
  public void setRowMatrix(final int row, final BlockFieldMatrix<T> matrix) throws MatrixDimensionMismatchException, OutOfRangeException {
    checkRowIndex(row);
    final int nCols = getColumnDimension();
    if((matrix.getRowDimension() != 1) || (matrix.getColumnDimension() != nCols)) {
      throw new MatrixDimensionMismatchException(matrix.getRowDimension(), matrix.getColumnDimension(), 1, nCols);
    }
    final int iBlock = row / BLOCK_SIZE;
    final int iRow = row - iBlock * BLOCK_SIZE;
    int mBlockIndex = 0;
    int mIndex = 0;
    T[] mBlock = matrix.blocks[mBlockIndex];
    for(int jBlock = 0; jBlock < blockColumns; ++jBlock) {
      final int jWidth = blockWidth(jBlock);
      final T[] block = blocks[iBlock * blockColumns + jBlock];
      final int available = mBlock.length - mIndex;
      if(jWidth > available) {
        System.arraycopy(mBlock, mIndex, block, iRow * jWidth, available);
        mBlock = matrix.blocks[++mBlockIndex];
        System.arraycopy(mBlock, 0, block, iRow * jWidth, jWidth - available);
        mIndex = jWidth - available;
      }
      else {
        System.arraycopy(mBlock, mIndex, block, iRow * jWidth, jWidth);
        mIndex += jWidth;
      }
    }
  }
  @Override() public void setRowMatrix(final int row, final FieldMatrix<T> matrix) throws MatrixDimensionMismatchException, OutOfRangeException {
    try {
      setRowMatrix(row, (BlockFieldMatrix<T>)matrix);
    }
    catch (ClassCastException cce) {
      super.setRowMatrix(row, matrix);
    }
  }
  @Override() public void setRowVector(final int row, final FieldVector<T> vector) throws MatrixDimensionMismatchException, OutOfRangeException {
    try {
      setRow(row, ((ArrayFieldVector<T>)vector).getDataRef());
    }
    catch (ClassCastException cce) {
      super.setRowVector(row, vector);
    }
  }
  @Override() public void setSubMatrix(final T[][] subMatrix, final int row, final int column) throws DimensionMismatchException, OutOfRangeException, NoDataException, NullArgumentException {
    MathUtils.checkNotNull(subMatrix);
    final int refLength = subMatrix[0].length;
    if(refLength == 0) {
      throw new NoDataException(LocalizedFormats.AT_LEAST_ONE_COLUMN);
    }
    final int endRow = row + subMatrix.length - 1;
    final int endColumn = column + refLength - 1;
    checkSubMatrixIndex(row, endRow, column, endColumn);
    for (final T[] subRow : subMatrix) {
      if(subRow.length != refLength) {
        throw new DimensionMismatchException(refLength, subRow.length);
      }
    }
    final int blockStartRow = row / BLOCK_SIZE;
    final int blockEndRow = (endRow + BLOCK_SIZE) / BLOCK_SIZE;
    final int blockStartColumn = column / BLOCK_SIZE;
    final int blockEndColumn = (endColumn + BLOCK_SIZE) / BLOCK_SIZE;
    for(int iBlock = blockStartRow; iBlock < blockEndRow; ++iBlock) {
      final int iHeight = blockHeight(iBlock);
      final int firstRow = iBlock * BLOCK_SIZE;
      final int iStart = FastMath.max(row, firstRow);
      final int iEnd = FastMath.min(endRow + 1, firstRow + iHeight);
      for(int jBlock = blockStartColumn; jBlock < blockEndColumn; ++jBlock) {
        final int jWidth = blockWidth(jBlock);
        final int firstColumn = jBlock * BLOCK_SIZE;
        final int jStart = FastMath.max(column, firstColumn);
        final int jEnd = FastMath.min(endColumn + 1, firstColumn + jWidth);
        final int jLength = jEnd - jStart;
        final T[] block = blocks[iBlock * blockColumns + jBlock];
        for(int i = iStart; i < iEnd; ++i) {
          System.arraycopy(subMatrix[i - row], jStart - column, block, (i - firstRow) * jWidth + (jStart - firstColumn), jLength);
        }
      }
    }
  }
}