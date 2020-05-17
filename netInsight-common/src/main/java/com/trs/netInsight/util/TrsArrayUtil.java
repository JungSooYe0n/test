package com.trs.netInsight.util;

import java.util.Arrays;
import java.util.List;

import com.trs.netInsight.handler.exception.OperationException;

/**
 * 操作数组工具类
 */
public class TrsArrayUtil {

	/**
	 * 对第一个数组降序排序，后面数组对应变化。
	 *
	 * @param a
	 *            要排序数组
	 * @param s
	 *            被动变化数组
	 */
	public static void sortDesc(long[] a, String[] s) throws OperationException {
		sort(a, 0, a.length - 1, null, 0, 0, s);
		for (int start = 0, end = a.length - 1; start < end; start++, end--) {
			long tempL = a[end];
			String tempS = s[end];
			a[end] = a[start];
			s[end] = s[start];
			a[start] = tempL;
			s[start] = tempS;
		}
	}

	/**
	 * 数组逆序
	 *
	 * @param a
	 *            long[]
	 */
	public static void reverse(long[] a) {
		for (int start = 0, end = a.length - 1; start < end; start++, end--) {
			long tempL = a[end];
			a[end] = a[start];
			a[start] = tempL;
		}
	}

	/**
	 * 数组逆序
	 *
	 * @param a
	 *            long[]
	 */
	public static void reverse(int[] a) {
		for (int start = 0, end = a.length - 1; start < end; start++, end--) {
			int tempL = a[end];
			a[end] = a[start];
			a[start] = tempL;
		}
	}

	/**
	 * 数组逆序
	 *
	 * @param a
	 *            Integer[]
	 */
	public static void reverse(Integer[] a) {
		for (int start = 0, end = a.length - 1; start < end; start++, end--) {
			int tempL = a[end];
			a[end] = a[start];
			a[start] = tempL;
		}
	}

	/**
	 * 数组逆序
	 *
	 * @param a
	 *            String[]
	 */
	public static void reverse(String[] a) {
		for (int start = 0, end = a.length - 1; start < end; start++, end--) {
			String tempL = a[end];
			a[end] = a[start];
			a[start] = tempL;
		}
	}

	/**
	 * 对第一个数组升序排序，后面数组对应变化。
	 *
	 * @param a
	 *            要排序数组
	 * @param s
	 *            被动变化数组
	 */
	public static void sortAsc(long[] a, String[] s) throws OperationException {
		sort(a, 0, a.length - 1, null, 0, 0, s);
	}

	/**
	 * 数组合并
	 *
	 * @return String[]
	 */
	@SafeVarargs
	public static <T> T[] concatAll(T[] first, T[]... rest) {
		int totalLength = first.length;
		for (T[] array : rest) {
			totalLength += array.length;
		}
		T[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (T[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	/**
	 * 从数组指定范围进行升序排序
	 *
	 * @param a,s
	 *            需排序数组
	 * @param left,right
	 *            起止排序位置
	 * @param work
	 *            a workspace array (slice)
	 * @param workBase
	 *            origin of usable space in work array
	 * @param workLen
	 *            workspace 大小
	 */
	public static void sort(long[] a, int left, int right, long[] work, int workBase, int workLen, String[] s)
			throws OperationException {
		if (a.length != s.length) {
			throw new OperationException("sort array error, s[] must as the number of a[]");
		}
		// 数组长度 286 位下直接进行快排
		if (right - left < 286) {
			sort(a, left, right, true, s);
			return;
		}

		/*
		 * Index run[i] is the start of i-th run (ascending or descending
		 * sequence).
		 */
		int[] run = new int[67 + 1];
		int count = 0;
		run[0] = left;

		// Check if the array is nearly sorted
		for (int k = left; k < right; run[count] = k) {
			if (a[k] < a[k + 1]) { // ascending
				while (++k <= right && a[k - 1] <= a[k]) {

				}
			} else if (a[k] > a[k + 1]) { // descending
				while (++k <= right && a[k - 1] >= a[k]) {

				}
				for (int lo = run[count] - 1, hi = k; ++lo < --hi;) {
					long t = a[lo];
					a[lo] = a[hi];
					a[hi] = t;
					String st = s[lo];
					s[lo] = s[hi];
					s[hi] = st;
				}
			} else { // equal
				for (int m = 33; ++k <= right && a[k - 1] == a[k];) {
					if (--m == 0) {
						sort(a, left, right, true, s);
						return;
					}
				}
			}

			/*
			 * The array is not highly structured, use Quicksort instead of
			 * merge sort.
			 */
			if (++count == 67) {
				sort(a, left, right, true, s);
				return;
			}
		}

		// Check special cases
		// Implementation note: variable "right" is increased by 1.
		if (run[count] == right++) { // The last run contains one element
			run[++count] = right;
		} else if (count == 1) { // The array is already sorted
			return;
		}

		// Determine alternation base for merge
		byte odd = 0;
		for (int n = 1; (n <<= 1) < count; odd ^= 1) {

		}
		// Use or create temporary array b for merging
		long[] b; // temp array; alternates with a
		int ao;
		int bo; // array offsets from 'left'
		int blen = right - left; // space needed for b
		if (work == null || workLen < blen || workBase + blen > work.length) {
			work = new long[blen];
			workBase = 0;
		}
		if (odd == 0) {
			System.arraycopy(a, left, work, workBase, blen);
			b = a;
			bo = 0;
			a = work;
			ao = workBase - left;
		} else {
			b = work;
			ao = 0;
			bo = workBase - left;
		}

		// Merging
		for (int last; count > 1; count = last) {
			for (int k = (last = 0) + 2; k <= count; k += 2) {
				int hi = run[k];
				int mi = run[k - 1];
				for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
					if (q >= hi || p < mi && a[p + ao] <= a[q + ao]) {
						b[i + bo] = a[p++ + ao];
					} else {
						b[i + bo] = a[q++ + ao];
					}
				}
				run[++last] = hi;
			}
			if ((count & 1) != 0) {
				for (int i = right, lo = run[count - 1]; --i >= lo; b[i + bo] = a[i + ao]) {

				}
				run[++last] = right;
			}
			long[] t = a;
			a = b;
			b = t;
			int o = ao;
			ao = bo;
			bo = o;
		}
	}

	/**
	 * Dual-Pivot 快排.
	 */
	private static void sort(long[] a, int left, int right, boolean leftmost, String[] s) {
		int length = right - left + 1;

		// 插入排序
		if (length < 47) {
			if (leftmost) {
				for (int i = left, j = i; i < right; j = ++i) {
					long ai = a[i + 1];
					String si = s[i + 1];
					while (ai < a[j]) {
						a[j + 1] = a[j];
						s[j + 1] = s[j];
						if (j-- == left) {
							break;
						}
					}
					s[j + 1] = si;
					a[j + 1] = ai;
				}
			} else {
				do {
					if (left >= right) {
						return;
					}
				} while (a[++left] >= a[left - 1]);

				for (int k = left; ++left <= right; k = ++left) {
					long a1 = a[k];
					long a2 = a[left];
					String s1 = s[k];
					String s2 = s[left];

					if (a1 < a2) {
						s2 = s1;
						s1 = s[left];
						a2 = a1;
						a1 = a[left];
					}
					while (a1 < a[--k]) {
						s[k + 2] = s[k];
						a[k + 2] = a[k];
					}

					a[++k + 1] = a1;
					s[k + 1] = s1;

					while (a2 < a[--k]) {
						a[k + 1] = a[k];
						s[k + 1] = s[k];
					}
					a[k + 1] = a2;
					s[k + 1] = s2;
				}
				long last = a[right];
				String la = s[right];

				while (last < a[--right]) {
					a[right + 1] = a[right];
					s[right + 1] = s[right];
				}
				a[right + 1] = last;
				s[right + 1] = la;
			}
			return;
		}

		// 长度 7
		int seventh = (length >> 3) + (length >> 6) + 1;

		int e3 = (left + right) >>> 1; // 中点
		int e2 = e3 - seventh;
		int e1 = e2 - seventh;
		int e4 = e3 + seventh;
		int e5 = e4 + seventh;

		// 插入排序
		if (a[e2] < a[e1]) {
			String st = s[e2];
			s[e2] = s[e1];
			s[e1] = st;
			long t = a[e2];
			a[e2] = a[e1];
			a[e1] = t;
		}

		if (a[e3] < a[e2]) {
			String st = s[e3];
			s[e3] = s[e2];
			s[e2] = st;
			long t = a[e3];
			a[e3] = a[e2];
			a[e2] = t;
			if (t < a[e1]) {
				a[e2] = a[e1];
				a[e1] = t;
				s[e2] = s[e1];
				s[e1] = st;
			}
		}
		if (a[e4] < a[e3]) {
			String st = s[e4];
			s[e4] = s[e3];
			s[e3] = st;
			long t = a[e4];
			a[e4] = a[e3];
			a[e3] = t;
			if (t < a[e2]) {
				a[e3] = a[e2];
				a[e2] = t;
				s[e3] = s[e2];
				s[e2] = st;
				if (t < a[e1]) {
					a[e2] = a[e1];
					a[e1] = t;
					s[e2] = s[e1];
					s[e1] = st;
				}
			}
		}
		if (a[e5] < a[e4]) {
			String st = s[e5];
			s[e5] = s[e4];
			s[e4] = st;
			long t = a[e5];
			a[e5] = a[e4];
			a[e4] = t;
			if (t < a[e3]) {
				s[e4] = s[e3];
				s[e3] = st;
				a[e4] = a[e3];
				a[e3] = t;
				if (t < a[e2]) {
					s[e3] = s[e2];
					s[e2] = st;
					a[e3] = a[e2];
					a[e2] = t;
					if (t < a[e1]) {
						a[e2] = a[e1];
						a[e1] = t;
						s[e2] = s[e1];
						s[e1] = st;
					}
				}
			}
		}

		// 指针
		int less = left;
		int great = right;

		if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {

			long pivot1 = a[e2];
			long pivot2 = a[e4];
			String sp1 = s[e2];
			String sp2 = s[e4];

			s[e2] = s[left];
			s[e4] = s[right];
			a[e2] = a[left];
			a[e4] = a[right];

			// 跳过比主值小和大的
			while (a[++less] < pivot1) {

			}
			while (a[--great] > pivot2) {

			}

			/*
			 * 分区:
			 *
			 * left part center part right part
			 * +--------------------------------------------------------------+
			 * | < pivot1 | pivot1 <= && <= pivot2 | ? | > pivot2 |
			 * +--------------------------------------------------------------+
			 * ^ ^ ^ | | | less k great
			 *
			 * 不变量:
			 *
			 * all in (left, less) < pivot1 pivot1 <= all in [less, k) <= pivot2
			 * all in (great, right) > pivot2
			 *
			 */
			outer: for (int k = less - 1; ++k <= great;) {
				long ak = a[k];
				String ark = s[k];
				if (ak < pivot1) {
					// 考虑性能，不使用 ：a[i++] = b
					a[k] = a[less];
					s[k] = s[less];
					a[less] = ak;
					s[less] = ark;
					++less;
				} else if (ak > pivot2) {
					while (a[great] > pivot2) {
						if (great-- == k) {
							break outer;
						}
					}
					if (a[great] < pivot1) {
						a[k] = a[less];
						a[less] = a[great];
						s[k] = s[less];
						s[less] = s[great];
						++less;
					} else { // pivot1 <= a[great] <= pivot2
						a[k] = a[great];
						s[k] = s[great];
					}
					a[great] = ak;
					s[great] = ark;
					--great;
				}
			}

			// 交换轴心到最后的位置
			a[left] = a[less - 1];
			a[less - 1] = pivot1;
			a[right] = a[great + 1];
			a[great + 1] = pivot2;
			s[left] = s[less - 1];
			s[less - 1] = sp1;
			s[right] = s[great + 1];
			s[great + 1] = sp2;

			// 左和右部分递归,排除已知的轴心
			sort(a, left, less - 2, leftmost, s);
			sort(a, great + 2, right, false, s);

			/*
			 * 如果中心部分太大(包括> 4/7的数组),交换内部主值.
			 */
			if (less < e1 && e5 < great) {

				while (a[less] == pivot1) {
					++less;
				}

				while (a[great] == pivot2) {
					--great;
				}

				outer: for (int k = less - 1; ++k <= great;) {
					String sk = s[k];
					long ak = a[k];
					if (ak == pivot1) {
						a[k] = a[less];
						a[less] = ak;
						s[k] = s[less];
						s[less] = sk;
						++less;
					} else if (ak == pivot2) {
						while (a[great] == pivot2) {
							if (great-- == k) {
								break outer;
							}
						}
						if (a[great] == pivot1) {
							a[k] = a[less];
							s[k] = s[less];
							a[less] = pivot1;
							s[less] = sp1;
							++less;
						} else { // pivot1 < a[great] < pivot2
							a[k] = a[great];
							s[k] = s[great];
						}
						a[great] = ak;
						s[great] = sk;
						--great;
					}
				}
			}

			// 类中心部分递归
			sort(a, less, great, false, s);

		} else {

			long pivot = a[e3];
			String pt = s[e3];

			for (int k = less; k <= great; ++k) {
				if (a[k] == pivot) {
					continue;
				}
				long ak = a[k];
				String sk = s[k];
				if (ak < pivot) {
					a[k] = a[less];
					a[less] = ak;
					s[k] = s[less];
					s[less] = sk;
					++less;
				} else { // a[k] > pivot - Move a[k] to right part
					while (a[great] > pivot) {
						--great;
					}
					if (a[great] < pivot) {
						a[k] = a[less];
						a[less] = a[great];
						s[k] = s[less];
						s[less] = s[great];
						++less;
					} else { // a[great] == pivot

						a[k] = pivot;
						s[k] = pt;
					}
					a[great] = ak;
					s[great] = sk;
					--great;
				}
			}

			sort(a, left, less - 1, leftmost, s);
			sort(a, great + 1, right, false, s);
		}
	}

	/**
	 * 判别两个数组中是否存在任意相同元素
	 * 
	 * @param arr
	 * @param toCompile
	 * @return
	 */
	public static boolean compileArray(Object[] arr, Object[] toCompile) {
		if (arr == null || arr.length <= 0 || toCompile == null || toCompile.length <= 0) {
			return false;
		}
		if (arr.length > toCompile.length) {
			for (Object str : toCompile) {
				List<Object> compile = Arrays.asList(arr);
				if (compile.contains(str)) {
					return true;
				}
			}
		} else {
			for (Object str : arr) {
				List<Object> compile = Arrays.asList(toCompile);
				if (compile.contains(str)) {
					return true;
				}
			}
		}
		return false;
	}

}
