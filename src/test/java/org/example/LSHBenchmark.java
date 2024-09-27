package org.example;

import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LSHBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        @Param({"5", "7"})
        int shingleSize;

        @Param({"100", "200"})
        int numHashFunctions;

        @Param({"20", "30"})
        int bands;

        int rows; // Количество строк, вычисляемое в зависимости от количества хеш-функций и полос

        Shingler shingler;
        MinHash minHash;
        LSH lsh;
        Map<String, String> documents;
        List<String> docIds;

        @Setup(Level.Trial)
        public void setup() {
            rows = numHashFunctions / bands; // Вычисляем количество строк на полосу
            shingler = new Shingler(shingleSize); // Инициализируем объект для создания шинглов
            minHash = new MinHash(numHashFunctions, Integer.MAX_VALUE); // Инициализируем MinHash
            lsh = new LSH(bands, rows); // Инициализируем LSH

            try {

                documents = loadDocumentsFromResources("20_newsgroups");
                docIds = new ArrayList<>(documents.keySet()); // Получаем список ID документов


                for (Map.Entry<String, String> entry : documents.entrySet()) {
                    String docId = entry.getKey(); // ID документа
                    String content = entry.getValue(); // Содержимое документа

                    Set<String> shingles = shingler.shingle(content); // Создаем шинглы
                    Set<Integer> shingleHashes = shingles.stream().map(String::hashCode).collect(Collectors.toSet()); // Преобразуем шинглы в хеш-коды

                    int[] signature = minHash.computeMinHashSignature(shingleHashes); // Вычисляем MinHash-подпись
                    lsh.addDocument(docId, signature); // Добавляем документ в LSH
                }
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }


        private static Map<String, String> loadDocumentsFromResources(String directory) throws IOException, URISyntaxException {
            Map<String, String> documents = new HashMap<>();

            // путь к директории
            Path resourcePath = Paths.get(Objects.requireNonNull(LSHBenchmark.class.getClassLoader().getResource(directory)).toURI());

            // Используем Stream API для обхода всех файлов в директории и поддиректориях
            try (Stream<Path> paths = Files.walk(resourcePath)) {
                paths.filter(Files::isRegularFile)
                        .forEach(path -> {
                            String filePath = resourcePath.relativize(path).toString();
                            try {
                                String content = Files.readString(path, StandardCharsets.UTF_8);
                                documents.put(filePath, content);
                            } catch (IOException e) {
                                System.err.println("Ошибка чтения файла " + filePath + ": " + e.getMessage());
                            }
                        });
            }

            return documents;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime) // Измеряем среднее время выполнения
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void testQuerySimilarDocuments(BenchmarkState state) {
        // Генерируем случайный идентификатор документа для поиска похожих
        Random rand = new Random();
        String queryDocId = state.docIds.get(rand.nextInt(state.docIds.size()));

        Set<String> similarDocs = state.lsh.querySimilarDocuments(queryDocId);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput) // Измеряем операции в секунду
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void testThroughputQuerySimilarDocuments(BenchmarkState state) {

        Random rand = new Random();
        String queryDocId = state.docIds.get(rand.nextInt(state.docIds.size()));

        Set<String> similarDocs = state.lsh.querySimilarDocuments(queryDocId);
    }
}
