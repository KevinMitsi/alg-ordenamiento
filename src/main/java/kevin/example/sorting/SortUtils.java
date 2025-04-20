package kevin.example.sorting;

import java.util.Arrays;

public class SortUtils {

    private SortUtils(){}

    public static void bubbleSort(int[] a) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++)
            for (int j = 0; j < n - 1 - i; j++)
                if (a[j] > a[j+1]) {
                    int tmp = a[j];
                    a[j] = a[j+1];
                    a[j+1] = tmp;
                }
    }

    public static void quickSort(int[] a, int low, int high) {
        if (low < high) {
            int p = partition(a, low, high);
            quickSort(a, low, p - 1);
            quickSort(a, p + 1, high);
        }
    }
    private static int partition(int[] a, int low, int high) {
        int pivot = a[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (a[j] <= pivot) {
                i++;
                int tmp = a[i]; a[i] = a[j]; a[j] = tmp;
            }
        }
        int tmp = a[i+1]; a[i+1] = a[high]; a[high] = tmp;
        return i + 1;
    }

    public static void stoogeSort(int[] a, int i, int j) {
        if (a[i] > a[j]) {
            int tmp = a[i]; a[i] = a[j]; a[j] = tmp;
        }
        if (j - i + 1 > 2) {
            int t = (j - i + 1) / 3;
            stoogeSort(a, i, j - t);
            stoogeSort(a, i + t, j);
            stoogeSort(a, i, j - t);
        }
    }

    public static void radixSort(int[] a) {
        int max = Arrays.stream(a).max().orElse(0);
        for (int exp = 1; max/exp > 0; exp *= 10) {
            countingSortByDigit(a, exp);
        }
    }
    private static void countingSortByDigit(int[] a, int exp) {
        int n = a.length;
        int[] output = new int[n];
        int[] count = new int[10];
        for (int num : a) count[(num/exp) % 10]++;
        for (int i = 1; i < 10; i++) count[i] += count[i-1];
        for (int i = n - 1; i >= 0; i--) {
            int d = (a[i]/exp) % 10;
            output[--count[d]] = a[i];
        }
        System.arraycopy(output, 0, a, 0, n);
    }

    public static void mergeSort(int[] a, int l, int r) {
        if (l < r) {
            int m = (l + r) / 2;
            mergeSort(a, l, m);
            mergeSort(a, m+1, r);
            merge(a, l, m, r);
        }
    }
    private static void merge(int[] a, int l, int m, int r) {
        int n1 = m - l + 1;
        int n2 = r - m;
        int[] lArray = new int[n1];
        int[] rArray = new int[n2];
        System.arraycopy(a, l, lArray, 0, n1);
        System.arraycopy(a, m+1, rArray, 0, n2);
        int i=0;
        int j=0;
        int k=l;
        while (i<n1 && j<n2) {
            a[k++] = (lArray[i] <= rArray[j]) ? lArray[i++] : rArray[j++];
        }
        while (i < n1) a[k++] = lArray[i++];
        while (j < n2) a[k++] = rArray[j++];
    }

    // Bitonic sort (secuencial)
    public static void bitonicSort(int[] a, int low, int cnt, int dir) {
        if (cnt > 1) {
            int k = cnt / 2;
            bitonicSort(a, low, k, 1);
            bitonicSort(a, low + k, k, 0);
            bitonicMerge(a, low, cnt, dir);
        }
    }
    private static void bitonicMerge(int[] a, int low, int cnt, int dir) {
        if (cnt > 1) {
            int k = cnt / 2;
            for (int i = low; i < low + k; i++) {
                if ((a[i] > a[i+k]) == (dir == 1)) {
                    int tmp = a[i];
                    a[i] = a[i+k];
                    a[i+k] = tmp;
                }
            }
            bitonicMerge(a, low, k, dir);
            bitonicMerge(a, low + k, k, dir);
        }
    }
}
