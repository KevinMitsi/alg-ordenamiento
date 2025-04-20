package kevin.example.sorting;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class SortingBenchmark {

    // Timeout máximo en segundos para evitar que se demore el programa
    private static final long TIMEOUT_SECONDS = 120;

    // 1. Generar datos de ejemplo
    public static void generarDatos(String archivo, int n) throws IOException {
        Random rnd = new Random();
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get("src/main/java/kevin/example/sorting/numbers/"+archivo))) {
            for (int i = 0; i < n; i++) {
                int valor = 10_000_000 + rnd.nextInt(90_000_000);
                bw.write(Integer.toString(valor));
                bw.newLine();
            }
        }
    }

    // 2. Leer datos desde archivo a un int[]
    public static int[] leerDatos(String archivo) throws IOException {
        List<String> lineas = Files.readAllLines(Paths.get("src/main/java/kevin/example/sorting/numbers/"+archivo));
        int[] arr = new int[lineas.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = Integer.parseInt(lineas.get(i));
        }
        return arr;
    }

    // 3. Timing helper
    public static long medirTiempo(Runnable sortRoutine) {
        long t0 = System.nanoTime();
        sortRoutine.run();
        long t1 = System.nanoTime();
        return (t1 - t0) / 1_000_000;  // milisegundos
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // Definimos tamaños y archivos
        int[] tamanos = {10_000, 100_000, 1_000_000};
        String[] archivos = {"datos_10000.txt", "datos_100000.txt", "datos_1000000.txt"};

        // Generar archivos (solo la primera vez)
        for (int i = 0; i < tamanos.length; i++) {
            generarDatos(archivos[i], tamanos[i]);
        }

        // Map para guardar resultados
        Map<String, List<Long>> resultados = new LinkedHashMap<>();
        String[] algoritmos = {
                "BubbleSort", "QuickSort", "StoogeSort",
                "RadixSort", "MergeSort", "BitonicSort"
        };
        for (String alg : algoritmos) {
            resultados.put(alg, new ArrayList<>());
        }

        // Benchmark secuencial con timeout por algoritmo
        for (int i = 0; i < tamanos.length; i++) {
            int size = tamanos[i];
            int[] original = leerDatos(archivos[i]);

            for (String alg : algoritmos) {
                int[] copia = original.clone();

                // Executor de un solo hilo para aplicar timeout
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<Long> future = executor.submit(() -> {
                    return switch (alg) {
                        case "BubbleSort"  -> medirTiempo(() -> SortUtils.bubbleSort(copia));
                        case "QuickSort"   -> medirTiempo(() -> SortUtils.quickSort(copia, 0, copia.length - 1));
                        case "StoogeSort"  -> medirTiempo(() -> SortUtils.stoogeSort(copia, 0, copia.length - 1));
                        case "RadixSort"   -> medirTiempo(() -> SortUtils.radixSort(copia));
                        case "MergeSort"   -> medirTiempo(() -> SortUtils.mergeSort(copia, 0, copia.length - 1));
                        case "BitonicSort" -> medirTiempo(() -> SortUtils.bitonicSort(copia, 0, copia.length, 1));
                        default            -> 0L;
                    };
                });

                try {
                    // Espera hasta TIMEOUT_SECONDS
                    long tiempo = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    resultados.get(alg).add(tiempo);
                    System.out.printf("%s – %d: %d ms%n", alg, size, tiempo);
                } catch (TimeoutException te) {
                    // Si excede el timeout, se cancela la tarea
                    future.cancel(true);
                    System.out.printf("%s – %d: TIMEOUT tras %d s%n", alg, size, TIMEOUT_SECONDS);
                    resultados.get(alg).add(10000000000L);
                } catch (InterruptedException | ExecutionException e) {
                    System.err.printf("Error en %s – %d: %s%n", alg, size, e.getMessage());
                    resultados.get(alg).add(100000000000L);
                } finally {
                    // Apagamos el executor antes de continuar
                    executor.shutdownNow();
                    executor.awaitTermination(1, TimeUnit.SECONDS);
                }
            }
        }

        // 4. Ordenar algoritmos por tiempo total
        List<Map.Entry<String, List<Long>>> orden = new ArrayList<>(resultados.entrySet());
        orden.sort((a, b) -> {
            long sumaA = a.getValue().stream().mapToLong(Long::longValue).sum();
            long sumaB = b.getValue().stream().mapToLong(Long::longValue).sum();
            return Long.compare(sumaB, sumaA);
        });

        // Mostrar orden final
        System.out.println("\nOrden de algoritmos (mayor → menor tiempo total):");
        for (var e : orden) {
            long suma = e.getValue().stream().mapToLong(Long::longValue).sum();
            System.out.printf("%s: %d ms%n", e.getKey(), suma);
        }


        // 5. Graficar con JFreeChart (pseudocódigo)
        // BarChart chart = ChartFactory.createBarChart(...);
        // ChartFrame frame = new ChartFrame("Benchmark", chart);
        // frame.pack(); frame.setVisible(true);
    }
}