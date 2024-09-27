package org.example;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;

public class MinHash { // Объявление класса MinHash
    private int numHashFunctions; // Количество хеш-функций
    private int[] hashCoefficientsA; // Массив коэффициентов A для хеш-функций
    private int[] hashCoefficientsB; // Массив коэффициентов B для хеш-функций
    private int prime; // Простое число для модуля в хеш-функциях

    public MinHash(int numHashFunctions, int maxShingleId) { // Конструктор класса
        this.numHashFunctions = numHashFunctions; // Инициализируем количество хеш-функций
        this.prime = getNextPrime(maxShingleId); // Находим ближайшее простое число больше maxShingleId
        this.hashCoefficientsA = new int[numHashFunctions]; // Инициализируем массив A
        this.hashCoefficientsB = new int[numHashFunctions]; // Инициализируем массив B
        Random rand = new Random(); // Создаем объект Random для генерации случайных чисел

        for (int i = 0; i < numHashFunctions; i++) { // Генерируем коэффициенты для хеш-функций
            hashCoefficientsA[i] = rand.nextInt(prime - 1) + 1; // Случайное число от 1 до prime - 1
            hashCoefficientsB[i] = rand.nextInt(prime - 1) + 1; // Случайное число от 1 до prime - 1
        }
    }

    private int getNextPrime(int n) { // Метод для нахождения следующего простого числа
        while (true) { // Бесконечный цикл
            if (isPrime(n)) return n; // Если число простое, возвращаем его
            n++; // Иначе увеличиваем n и продолжаем поиск
        }
    }

    private boolean isPrime(int n) { // Метод для проверки, является ли число простым
        if (n <= 1) return false; // Числа меньше или равные 1 не являются простыми
        for (int i = 2; i <= Math.sqrt(n); i++) // Проходим от 2 до квадратного корня из n
            if (n % i == 0) return false; // Если n делится на i без остатка, то n не простое
        return true; // Если делителей не найдено, n простое
    }

    public int[] computeMinHashSignature(Set<Integer> shingles) { // Метод для вычисления MinHash-подписи
        int[] signature = new int[numHashFunctions]; // Инициализируем массив для подписи
        Arrays.fill(signature, Integer.MAX_VALUE); // Заполняем массив максимальными значениями

        for (int shingle : shingles) { // Проходим по каждому шинглу
            for (int i = 0; i < numHashFunctions; i++) { // Проходим по каждой хеш-функции
                int hashCode = (hashCoefficientsA[i] * shingle + hashCoefficientsB[i]) % prime; // Вычисляем хеш-код
                if (hashCode < signature[i]) { // Если хеш-код меньше текущего значения в подписи
                    signature[i] = hashCode; // Обновляем значение в подписи
                }
            }
        }
        return signature; // Возвращаем MinHash-подпись
    }
}
