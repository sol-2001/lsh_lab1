package org.example;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LSHExample {
    public static void main(String[] args) throws IOException, URISyntaxException {
        int shingleSize = 5;
        int numHashFunctions = 100;
        int bands = 20;
        int rows = numHashFunctions / bands;

        Shingler shingler = new Shingler(shingleSize);
        MinHash minHash = new MinHash(numHashFunctions, Integer.MAX_VALUE);
        LSH lsh = new LSH(bands, rows);

        // Загружаем документы из папки ресурсов
        Map<String, String> documents = loadDocumentsFromResources("20_newsgroups");

        for (Map.Entry<String, String> entry : documents.entrySet()) {
            String docId = entry.getKey();
            String content = entry.getValue();

            Set<String> shingles = shingler.shingle(content);
            Set<Integer> shingleHashes = shingles.stream().map(String::hashCode).collect(Collectors.toSet());

            int[] signature = minHash.computeMinHashSignature(shingleHashes);
            lsh.addDocument(docId, signature);
        }

        String queryDocId = ("comp.graphics\\37261.txt");

        Set<String> similarDocs = lsh.querySimilarDocuments(queryDocId);

        System.out.println("Похожие документы для " + queryDocId + ":");
        for (String docId : similarDocs) {
            System.out.println(docId);
        }
    }

    // Метод для загрузки всех документов из папки ресурсов
    private static Map<String, String> loadDocumentsFromResources(String directory) throws IOException, URISyntaxException {
        Map<String, String> documents = new HashMap<>();

        // Получаем путь к директории из ресурсов через ClassLoader
        Path resourcePath = Paths.get(Objects.requireNonNull(LSHExample.class.getClassLoader().getResource(directory)).toURI());

        // Используем Stream API для обхода всех файлов в директории и поддиректориях
        try (Stream<Path> paths = Files.walk(resourcePath)) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        String filePath = resourcePath.relativize(path).toString(); // Получаем относительный путь файла
                        try {
                            // Читаем содержимое файла как строку с использованием кодировки UTF-8
                            String content = Files.readString(path, StandardCharsets.UTF_8);
                            documents.put(filePath, content); // Добавляем содержимое в карту
                        } catch (IOException e) {
                            System.err.println("Ошибка чтения файла " + filePath + ": " + e.getMessage());
                        }
                    });
        }

        return documents;
    }
}

