package org.example;

import java.util.HashSet;
import java.util.Set;

public class Shingler {
    private int k; // Размер шингла

    public Shingler(int k) { // Конструктор класса, принимающий размер шнгла
        this.k = k; // Инициализация поля k
    }

    public Set<String> shingle(String document) { // создания шинглов из документа
        Set<String> shingles = new HashSet<>(); // Создаем пустое множество для хранения шинглов
        for (int i = 0; i <= document.length() - k; i++) { // Проходим по документу от 0 до длины документа минус размер шингла
            shingles.add(document.substring(i, i + k)); // Добавляем подстроку длиной k в множество шинглов
        }
        return shingles; // Возвращаем множество шинглов
    }
}
