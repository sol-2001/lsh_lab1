package org.example;

import java.util.*;

public class LSH {
    private int bands; // Количество полос (bands) для разбиения подписи
    private int rows; // Количество строк в каждой полосе
    private Map<String, int[]> documentSignatures; // Карта для хранения MinHash-подписей документов, где ключ - это ID документа, а значение - его MinHash-подпись
    private Map<String, List<String>> buckets; // Карта для хранения корзин, где ключ - это хеш полосы, а значение - список документов, попавших в эту корзину

    public LSH(int bands, int rows) { // Конструктор
        this.bands = bands;
        this.rows = rows;
        this.documentSignatures = new HashMap<>();
        this.buckets = new HashMap<>();
    }

    public void addDocument(String docId, int[] signature) { // Метод для добавления документа в LSH
        documentSignatures.put(docId, signature);

        // Разбиваем подпись на полосы (bands) и сохраняем их в корзины
        for (int i = 0; i < bands; i++) { // Проходим по каждой полосе

            int[] band = Arrays.copyOfRange(signature, i * rows, (i + 1) * rows);
            // Вычисляем хеш-код для этой полосы (подмассива)
            int hash = Arrays.hashCode(band);

            // Создаем ключ для корзины, который включает номер полосы и её хеш-код
            String bucketKey = i + "-" + hash;

            // Добавляем ID документа в соответствующую корзину
            buckets.computeIfAbsent(bucketKey, k -> new ArrayList<>()).add(docId);
        }
    }

    public Set<String> querySimilarDocuments(String docId) {
        Set<String> similarDocs = new HashSet<>(); // Создаем множество для хранения идентификаторов похожих документов
        int[] signature = documentSignatures.get(docId); // Получаем MinHash-подпись запрашиваемого документа

        if (signature == null) {
            System.err.println("Документ с ID " + docId + " не найден.");
            return similarDocs;
        }

        // Проходим по каждой полосе подписи запрашиваемого документа
        for (int i = 0; i < bands; i++) {
            // Извлекаем полосу из подписи
            int[] band = Arrays.copyOfRange(signature, i * rows, (i + 1) * rows);
            // Вычисляем хеш-код для этой полосы
            int hash = Arrays.hashCode(band);

            // Получаем корзину по её ключу (состоящему из номера полосы и хеша)
            String bucketKey = i + "-" + hash;
            List<String> bucket = buckets.get(bucketKey); // Ищем корзину для этой полосы

            // Если корзина найдена, проходим по всем документам в корзине
            if (bucket != null) {
                for (String otherDocId : bucket) {
                    // Если найденный документ не является запрашиваемым (чтобы не добавлять сам себя)
                    if (!otherDocId.equals(docId)) {
                        // Добавляем идентификатор документа в список похожих
                        similarDocs.add(otherDocId);
                    }
                }
            }
        }
        return similarDocs; // Возвращаем множество похожих документов
    }
}