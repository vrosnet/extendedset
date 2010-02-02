/* (c) 2010 Alessandro Colantonio
 * <mailto:colanton@mat.uniroma3.it>
 * <http://ricerca.mat.uniroma3.it/users/colanton>
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */ 

package it.uniroma3.mat.extendedset;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * Two-dimensional matrix managed through indices.
 * <p>
 * The matrix is scanned by rows, namely in a <code>n*m</code> matrix the
 * indices from <code>0</code> to <code>n-1</code> indicates the columns of
 * the first row, the indices from <code>n</code> to <code>2*n-1</code>
 * indicates the columns of the second row, etc.
 * 
 * @author Alessandro Colantonio
 * @version $Id$
 * 
 * @param <R>
 *            the type of elements represented by rows
 * @param <C>
 *            the type of elements represented by columns
 */
public class MatrixSet<R, C> extends AbstractExtendedSet<Integer> {
	/** all possible row elements */
	private final IndexedSet<R> rows;

	/** all possible column elements */
	private final IndexedSet<C> cols;
	
	/** matrix cell indices */
	private final ExtendedSet<Integer> indices;

	/**
	 * Matrix cell
	 */
	public class Cell {
		/** element representing the row */
		public final R row; 

		/** element representing the column */
		public final C col;
		
		/** cell index */
		public final int index;
		
		/**
		 * Constructs a matrix cell
		 * 
		 * @param row
		 *            element representing the row
		 * @param col
		 *            element representing the column
		 * @param index         
		 *            cell index  
		 */
		private Cell(R row, C col, int index) {
			this.row = row;
			this.col = col;
			this.index = index;
		}
		
		/**
		 * Gives the {@link MatrixSet} instance that contains this {@link Cell}
		 * instance.
		 * 
		 * @return the {@link MatrixSet} instance that contains this
		 *         {@link Cell} instance
		 */
		public MatrixSet<R, C> getMatrix() {
			return MatrixSet.this;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (this == obj)
				return true;
			if (getClass() != obj.getClass())
				return false;

			final Cell other = (Cell) obj;
			return this.getMatrix() == other.getMatrix() && this.index == other.index;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return index;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return "(" + row + ", " + col + ")";
		}
	}

	/**
	 * Initializes the matrix
	 * 
	 * @param rows
	 *            collection of <i>all</i> possible items of rows. Order
	 *            will be preserved.
	 * @param cols
	 *            collection of <i>all</i> possible items of columns. Order
	 *            will be preserved.
	 * @param compressed
	 *            <code>true</code> if a compressed internal representation
	 *            should be used
	 */
	public MatrixSet(Collection<R> rows, Collection<C> cols, boolean compressed) {
		this.rows = new IndexedSet<R>(rows, compressed).universe().unmodifiable();
		this.cols = new IndexedSet<C>(cols, compressed).universe().unmodifiable();
		if (compressed)
			indices = new ConciseSet();
		else
			indices = new FastSet();
	}

	/**
	 * Shallow-copy constructor
	 * 
	 * @param rows
	 *            row elements
	 * @param cols
	 *            column elements
	 * @param indices
	 *            matrix cell indices
	 */
	private MatrixSet(IndexedSet<R> rows, IndexedSet<C> cols, ExtendedSet<Integer> indices) {
		this.rows = rows;
		this.cols = cols;
		this.indices = indices;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public MatrixSet<R, C> clone() {
		return new MatrixSet<R, C>(rows, cols, indices.clone());
	}
	
	/**
	 * Returns the set of all row elements
	 * 
	 * @return the set of all row elements
	 */
	public IndexedSet<R> rows() {
		return rows;
	}

	/**
	 * Returns the set of all column elements
	 * 
	 * @return the set of all column elements
	 */
	public IndexedSet<C> cols() {
		return cols;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MatrixSet))
			return false;
		MatrixSet<?, ?> other = (MatrixSet<?, ?>) obj;
		return this.rows == other.rows
				&& this.cols == other.cols
				&& super.equals(other);
	}

	/**
	 * Returns the index of the given cell
	 * 
	 * @param row
	 *            row element
	 * @param col
	 *            column element
	 * @return the index of the given cell
	 */
	public int indexOf(R row, C col) {
		return rows.indexOf(row) * cols.size() + cols.indexOf(col);
	}
	
	/**
	 * Returns the cell corresponding to the given index
	 * 
	 * @param index
	 *            index calculated as <code>col * cols + row</code>
	 * @return the cell corresponding to the given index
	 */
	public Cell get(int index) {
		return new Cell(rows.get(index / cols.size()), cols.get(index % cols.size()), index);
	}

	/**
	 * Adds a cell
	 * 
	 * @param row
	 *            row element
	 * @param col
	 *            column element
	 * @return <code>true</code> if this matrix did not already contain the
	 *         specified cell
	 */
	public boolean add(R row, C col) {
		return add(indexOf(row, col));
	}
	
	/**
	 * Removes a cell
	 * 
	 * @param row
	 *            row element
	 * @param col
	 *            column element
	 * @return <code>true</code> if this matrix contained the specified cell
	 */
	public boolean remove(R row, C col) {
		return remove(indexOf(row, col));
	}

	/**
	 * Identifies all the indices that represent the specified sub-matrix
	 * 
	 * @param fromRow
	 *            first row element (if <code>null</code> it represents the
	 *            first one)
	 * @param toRow
	 *            last row element (if <code>null</code> it represents the
	 *            last one)
	 * @param fromCol
	 *            first column element (if <code>null</code> it represents the
	 *            first one)
	 * @param toCol
	 *            last column element (if <code>null</code> it represents the
	 *            last one)
	 * @return all the indices that represent the specified sub-matrix
	 */
	public MatrixSet<R, C> extractSubMatrix(R fromRow, R toRow, C fromCol, C toCol) {
		// final set of indices
		MatrixSet<R, C> res = emptySet();
		
		// row range
		int firstRow = fromRow == null ? 0 : rows.indexOf(fromRow);
		int lastRow = toRow == null ? rows.size() - 1 : rows.indexOf(toRow);
		
		// column range
		int firstCol = fromCol == null ? 0 : cols.indexOf(fromCol);
		int lastCol = toCol == null ? cols.size() - 1 : cols.indexOf(toCol);

		// identify indices
		for (int r = firstRow; r <= lastRow; r++) {
			int firstCellInRow = r * cols.size();
			res.fill(firstCellInRow + firstCol, firstCellInRow + lastCol);
		}
		return res;
	}
	
	/**
	 * Identifies all the indices that represent the specified sub-matrix
	 * 
	 * @param selectedRows
	 *            row elements (if <code>null</code> it represents all rows)
	 * @param selectedCols
	 *            column elements (if <code>null</code> it represents all
	 *            columns)
	 * @return all the indices that represent the specified sub-matrix
	 */
	public MatrixSet<R, C> extractSubMatrix(Collection<R> selectedRows, Collection<C> selectedCols) {
		// final set of indices
		MatrixSet<R, C> res = emptySet();

		// identify indices
		if (selectedRows == null && selectedCols == null) {
			res.fill(0, rows.size() * cols.size() - 1);
		} else if (selectedRows == null) {
			for (R r : rows) 
				for (C c : selectedCols) 
					res.add(rows.indexOf(r) * cols.size() + cols.indexOf(c));
		} else if (selectedCols == null) {
			for (R r : selectedRows) {
				int first = rows.indexOf(r) * cols.size();
				res.fill(first, first + cols.size() - 1);
			}
		} else {
			for (R r : selectedRows) 
				for (C c : selectedCols) 
					res.add(rows.indexOf(r) * cols.size() + cols.indexOf(c));
		}
		return res;
	}

	/**
	 * Gets all the elements contained within the specified row
	 * 
	 * @param row
	 *            row element
	 * @return all the elements (of type <code>C</code>) contained within the
	 *         specified row
	 */
	public IndexedSet<C> row(R row) {
		// involved indices
		MatrixSet<R, C> matrixIndices = extractSubMatrix(row, row, null, null);
		int first = rows.indexOf(row) * cols.size();
		
		// shift indices
		ExtendedSet<Integer> rowIndices = cols.indices().emptySet();
		for (Integer i : getIntersection(matrixIndices)) 
			rowIndices.add(i - first);
		
		// get elements
		IndexedSet<C> res = cols.clone();
		res.indices().retainAll(rowIndices);
		return res;
	}
	
	/**
	 * Gets all the elements contained within the specified column
	 * 
	 * @param col
	 *            column element
	 * @return all the elements (of type <code>R</code>) contained within the
	 *         specified column
	 */
	public IndexedSet<R> col(C col) {
		// involved indices
		MatrixSet<R, C> matrixIndices = extractSubMatrix(null, null, col, col);
		int colCount = cols.size();
		
		// shift indices
		ExtendedSet<Integer> colIndices = rows.indices().emptySet();
		for (Integer i : getIntersection(matrixIndices)) 
			colIndices.add(i / colCount);
		
		// get elements
		IndexedSet<R> res = rows.clone();
		res.indices().retainAll(colIndices);
		return res;
	}

	/**
	 * Iterates over filled cells, from first row &amp; column to last row &amp;
	 * column
	 * 
	 * @return the cell {@link Iterator} instance
	 */
	public Iterator<Cell> cellIterator() {
		return new Iterator<Cell>() {
			private final Iterator<Integer> itr = iterator();
			/** {@inheritDoc} */ @Override public boolean hasNext() {return itr.hasNext();}
			/** {@inheritDoc} */ @Override public Cell next() {return get(itr.next());}
			/** {@inheritDoc} */ @Override public void remove() {itr.remove();}
		};
	}
	
	/**
	 * Iterates over filled cells in reverse order, namely from last row &amp;
	 * column to first row &amp; column
	 * 
	 * @return the cell {@link Iterator} instance
	 */
	public Iterator<Cell> descendingCellIterator() {
		return new Iterator<Cell>() {
			private final Iterator<Integer> itr = descendingIterator();
			/** {@inheritDoc} */ @Override public boolean hasNext() {return itr.hasNext();}
			/** {@inheritDoc} */ @Override public Cell next() {return get(itr.next());}
			/** {@inheritDoc} */ @Override public void remove() {itr.remove();}
		};
	}
	
	/**
	 * Gets an {@link Iterable} interface to iterate over filled cells, from
	 * first row &amp; column to last row &amp; column
	 * 
	 * @return the {@link Iterable} interface
	 */
	public Iterable<Cell> cells() {
		return new Iterable<Cell>() {
			/** {@inheritDoc} */ 
			@Override 
			public Iterator<Cell> iterator() {
				return cellIterator();
			}
		};
	}

	/**
	 * Gets an {@link Iterable} interface to iterate over filled cells in
	 * reverse order, namely from last row &amp; column to first row &amp;
	 * column
	 * 
	 * @return the {@link Iterable} interface
	 */
	public Iterable<Cell> descendingCells() {
		return new Iterable<Cell>() {
			/** {@inheritDoc} */ 
			@Override 
			public Iterator<Cell> iterator() {
				return descendingCellIterator();
			}
		};
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String debugInfo() {
		StringBuilder s = new StringBuilder();
		
		s.append("rows: ");
		s.append(rows);
		s.append('\n');
		s.append("cols: ");
		s.append(cols);
		s.append('\n');

		s.append("matrix:\n");
		s.append('+');
		for (int c = 0; c < cols.size(); c++) 
			s.append('-');
		s.append("+\n");

		for (int r = 0; r < rows.size(); r++) {
			s.append('|');
			for (int c = 0; c < cols.size(); c++) {
				if (contains(r * cols.size() + c))
					s.append('*');
				else
					s.append(' ');
			}
			s.append("|\n");
		}
		
		s.append('+');
		for (int c = 0; c < cols.size(); c++) 
			s.append('-');
		s.append('+');

		return s.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return indices.toString();
	}

	
	/*
	 * Generic ExtendedSet<Integer> methods  
	 */
	
	/** {@inheritDoc} */ @Override public boolean addFirstOf(SortedSet<Integer> set) {return indices.addFirstOf(set);}
	/** {@inheritDoc} */ @Override public boolean addLastOf(SortedSet<Integer> set) {return indices.addLastOf(set);}
	/** {@inheritDoc} */ @Override public double bitmapCompressionRatio() {return indices.bitmapCompressionRatio();}
	/** {@inheritDoc} */ @Override public void clear(Integer from, Integer to) {indices.clear(from, to);}
	/** {@inheritDoc} */ @Override public double collectionCompressionRatio() {return indices.collectionCompressionRatio();}
	/** {@inheritDoc} */ @Override public void complement() {indices.complement();}
	/** {@inheritDoc} */ @Override public int complementSize() {return indices.complementSize();}
	/** {@inheritDoc} */ @Override public boolean containsAny(Collection<? extends Integer> other) {return indices.containsAny(other);}
	/** {@inheritDoc} */ @Override public boolean containsAtLeast(Collection<? extends Integer> other, int minElements) {return containsAtLeast(other, minElements);}
	/** {@inheritDoc} */ @Override public Iterable<Integer> descending() {return indices.descending();}
	/** {@inheritDoc} */ @Override public Iterator<Integer> descendingIterator() {return indices.descendingIterator();}
	/** {@inheritDoc} */ @Override public int differenceSize(Collection<? extends Integer> other) {return indices.differenceSize(other);}
	/** {@inheritDoc} */ @Override public void fill(Integer from, Integer to) {indices.fill(from, to);}
	/** {@inheritDoc} */ @Override public int intersectionSize(Collection<? extends Integer> other) {return indices.intersectionSize(other);}
	/** {@inheritDoc} */ @Override public boolean removeFirstOf(SortedSet<Integer> set) {return indices.removeFirstOf(set);}
	/** {@inheritDoc} */ @Override public boolean removeLastOf(SortedSet<Integer> set) {return indices.removeLastOf(set);}
	/** {@inheritDoc} */ @Override public int symmetricDifferenceSize(Collection<? extends Integer> other) {return indices.symmetricDifferenceSize(other);}
	/** {@inheritDoc} */ @Override public int unionSize(Collection<? extends Integer> other) {return indices.unionSize(other);}
	/** {@inheritDoc} */ @Override public Comparator<? super Integer> comparator() {return indices.comparator();}
	/** {@inheritDoc} */ @Override public Integer first() {return indices.first();}
	/** {@inheritDoc} */ @Override public Integer last() {return indices.last();}
	/** {@inheritDoc} */ @Override public boolean add(Integer e) {return indices.add(e);}
	/** {@inheritDoc} */ @Override public boolean addAll(Collection<? extends Integer> c) {return indices.addAll(c);}
	/** {@inheritDoc} */ @Override public void clear() {indices.clear();}
	/** {@inheritDoc} */ @Override public boolean contains(Object o) {return indices.contains(o);}
	/** {@inheritDoc} */ @Override public boolean containsAll(Collection<?> c) {return indices.containsAll(c);}
	/** {@inheritDoc} */ @Override public boolean isEmpty() {return indices.isEmpty();}
	/** {@inheritDoc} */ @Override public Iterator<Integer> iterator() {return indices.iterator();}
	/** {@inheritDoc} */ @Override public boolean remove(Object o) {return indices.remove(o);}
	/** {@inheritDoc} */ @Override public boolean removeAll(Collection<?> c) {return indices.removeAll(c);}
	/** {@inheritDoc} */ @Override public boolean retainAll(Collection<?> c) {return indices.retainAll(c);}
	/** {@inheritDoc} */ @Override public int size() {return indices.size();}
	/** {@inheritDoc} */ @Override public Object[] toArray() {return indices.toArray();}
	/** {@inheritDoc} */ @Override public <T> T[] toArray(T[] a) {return indices.toArray(a);}
	/** {@inheritDoc} */ @Override public int compareTo(ExtendedSet<Integer> o) {return indices.compareTo(o);}
	
	/*
	 * Methods that return a MatrixSet instance 
	 */
	
	/** {@inheritDoc} */ @Override public MatrixSet<R, C> getComplement() {return new MatrixSet<R, C>(rows, cols, indices.getComplement());}
	/** {@inheritDoc} */ @Override public MatrixSet<R, C> getDifference(Collection<? extends Integer> other) {return new MatrixSet<R, C>(rows, cols, indices.getDifference(other));}
	/** {@inheritDoc} */ @Override public MatrixSet<R, C> getIntersection(Collection<? extends Integer> other) {return new MatrixSet<R, C>(rows, cols, indices.getIntersection(other));}
	/** {@inheritDoc} */ @Override public MatrixSet<R, C> getSymmetricDifference(Collection<? extends Integer> other) {return new MatrixSet<R, C>(rows, cols, indices.getSymmetricDifference(other));}
	/** {@inheritDoc} */ @Override public MatrixSet<R, C> getUnion(Collection<? extends Integer> other) {return new MatrixSet<R, C>(rows, cols, indices.getUnion(other));}
	/** {@inheritDoc} */ @Override public MatrixSet<R, C> unmodifiable() {return new MatrixSet<R, C>(rows, cols, indices.unmodifiable());}
	/** {@inheritDoc} */ @Override public MatrixSet<R, C> emptySet() {return new MatrixSet<R, C>(rows, cols, indices.emptySet());}


	/**
	 * Test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		MatrixSet<String, Double> m = new MatrixSet<String, Double>(
				Arrays.asList("A", "B", "C", "D"), Arrays.asList(1D, 2D, 3D), false);
		m.add("A", 1D);
		m.add("B", 1D);
		m.add("B", 2D);
		m.add("C", 2D);
		System.out.println(m.debugInfo());
		System.out.println();
		
		for (MatrixSet<String, Double>.Cell c : m.cells()) {
			System.out.println(c);
		}
		System.out.println();
		
		for (MatrixSet<String, Double>.Cell c : m.descendingCells()) {
			System.out.println(c);
		}
		System.out.println();
		
		for (String r : m.rows()) 
			System.out.println(r + ": " + m.row(r));
		System.out.println();

		for (Double c : m.cols()) 
			System.out.println(c + ": " + m.col(c));
		System.out.println();
	}
}