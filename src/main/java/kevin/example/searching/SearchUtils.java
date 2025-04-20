package kevin.example.searching;

public class SearchUtils {

    /**
     * Búsqueda lineal: recorre el arreglo hasta encontrar key o terminar.
     * @param a arreglo ordenado o desordenado
     * @param key valor a buscar
     * @return índice de key o -1 si no existe
     */
    public static int linearSearch(int[] a, int key) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == key) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Búsqueda binaria: requiere arreglo ordenado.
     * @param a arreglo ordenado
     * @param key valor a buscar
     * @param lo índice inferior
     * @param hi índice superior
     * @return índice de key o -1 si no existe
     */
    public static int binarySearch(int[] a, int key, int lo, int hi) {
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] == key) {
                return mid;
            } else if (a[mid] < key) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return -1;
    }

    /**
     * Búsqueda ternaria: divide el rango en tres secciones.
     * @param a arreglo ordenado
     * @param key valor a buscar
     * @param lo índice inferior
     * @param hi índice superior
     * @return índice de key o -1 si no existe
     */
    public static int ternarySearch(int[] a, int key, int lo, int hi) {
        if (lo > hi) {
            return -1;
        }
        int third = (hi - lo) / 3;
        int mid1 = lo + third;
        int mid2 = hi - third;

        if (a[mid1] == key) {
            return mid1;
        }
        if (a[mid2] == key) {
            return mid2;
        }

        if (key < a[mid1]) {
            return ternarySearch(a, key, lo, mid1 - 1);
        } else if (key > a[mid2]) {
            return ternarySearch(a, key, mid2 + 1, hi);
        } else {
            return ternarySearch(a, key, mid1 + 1, mid2 - 1);
        }
    }

    /**
     * Búsqueda por saltos (Jump Search): requiere arreglo ordenado.
     * @param a arreglo ordenado
     * @param key valor a buscar
     * @return índice de key o -1 si no existe
     */
    public static int jumpSearch(int[] a, int key) {
        int n = a.length;
        int step = (int) Math.sqrt(n);
        int prev = 0;

        // Encontrar el bloque donde puede estar key
        while (prev < n && a[Math.min(prev + step, n) - 1] < key) {
            prev += step;
        }

        // Buscar linealmente dentro del bloque
        int limit = Math.min(prev + step, n);
        for (int i = prev; i < limit; i++) {
            if (a[i] == key) {
                return i;
            }
        }
        return -1;
    }
}

