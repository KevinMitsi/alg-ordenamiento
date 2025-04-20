package kevin.example.searching;


import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class SearchingBenchmark {

    // Timeout máximo en segundos
    private static final long TIMEOUT_SECONDS = 120;

    // 1. Generar datos desordenados
    public static void generarDatos(String archivo, int n) throws IOException {
        Random rnd = new Random();
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get("src/main/java/kevin/example/searching/numbers/"+archivo))) {
            for (int i = 0; i < n; i++) {
                int valor = 10_000_000 + rnd.nextInt(90_000_000);
                bw.write(Integer.toString(valor));
                bw.newLine();
            }
        }
    }

    // 2. Leer datos al arreglo
    public static int[] leerDatos(String archivo) throws IOException {
        List<String> lineas = Files.readAllLines(Paths.get("src/main/java/kevin/example/searching/numbers/"+archivo));
        int[] arr = new int[lineas.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = Integer.parseInt(lineas.get(i));
        }
        return arr;
    }

    // 3. Medir tiempo de ejecución (ms) de una búsqueda
    public static long medirTiempo(Runnable searchRoutine) {
        long t0 = System.nanoTime();
        searchRoutine.run();
        long t1 = System.nanoTime();
        return (t1 - t0) / 1_000_000;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int[] tamanos  = {10_000, 100_000, 1_000_000};
        String[] archivos = {"datos_10000.txt", "datos_100000.txt", "datos_1000000.txt"};

        // Generar archivos (solo la primera vez)
        for (int i = 0; i < tamanos.length; i++)
            generarDatos(archivos[i], tamanos[i]);

        // Nombre de algoritmos de búsqueda
        String[] algoritmos = {
                "LinearSearch",
                "BinarySearch",
                "TernarySearch",
                "JumpSearch"
        };

        // Map para guardar los tiempos
        Map<String, List<Long>> resultados = new LinkedHashMap<>();
        for (String alg : algoritmos)
            resultados.put(alg, new ArrayList<>());

        // Para cada tamaño de entrada
        for (int i = 0; i < tamanos.length; i++) {
            int n = tamanos[i];
            int[] original = leerDatos(archivos[i]);
            Arrays.sort(original);         // ordenamos para binaria, ternaria y salto

            // Seleccionar un key aleatorio del arreglo
            Random random = new Random();
            int keyIndex = random.nextInt(original.length);
            int key = original[keyIndex];

            System.out.printf("\nPruebas para tamaño %,d con key=%d (índice %d):%n", n, key, keyIndex);

            for (String alg : algoritmos) {
                int[] copia = original.clone();

                ExecutorService exec = Executors.newSingleThreadExecutor();
                Future<Long> future = exec.submit(() -> {
                    return switch (alg) {
                        case "LinearSearch"  -> medirTiempo(() -> SearchUtils.linearSearch(copia, key));
                        case "BinarySearch"  -> medirTiempo(() -> SearchUtils.binarySearch(copia, key, 0, copia.length-1));
                        case "TernarySearch" -> medirTiempo(() -> SearchUtils.ternarySearch(copia, key, 0, copia.length-1));
                        case "JumpSearch"    -> medirTiempo(() -> SearchUtils.jumpSearch(copia, key));
                        default               -> 0L;
                    };
                });

                try {
                    long tiempo = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    resultados.get(alg).add(tiempo);
                    System.out.printf("%s – %,d: %d ms%n", alg, n, tiempo);
                } catch (TimeoutException te) {
                    future.cancel(true);
                    System.out.printf("%s – %,d: TIMEOUT%n", alg, n);
                    resultados.get(alg).add(Long.MAX_VALUE);
                } catch (ExecutionException e) {
                    System.err.printf("Error en %s – %,d: %s%n", alg, n, e.getMessage());
                    resultados.get(alg).add(Long.MAX_VALUE);
                } finally {
                    exec.shutdownNow();
                    exec.awaitTermination(1, TimeUnit.SECONDS);
                }
            }
        }

        // Orden final por tiempo total
        List<Map.Entry<String,List<Long>>> orden =
                new ArrayList<>(resultados.entrySet());

        // sort descendiente por la suma de tiempos
        orden.sort(
                Comparator.<Map.Entry<String,List<Long>>>comparingLong(e ->
                        // desambiguamos longValue() con un lambda
                        e.getValue().stream()
                                .mapToLong(x -> x)
                                .sum()
                ).reversed()
        );

        System.out.println("\nRanking (mayor → menor tiempo total):");
        for (var e : orden) {
            long suma = e.getValue().stream().mapToLong(Long::longValue).sum();
            System.out.printf("%s: %d ms%n", e.getKey(), suma);
        }
    }
}