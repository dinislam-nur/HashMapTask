package map.impl;

import lombok.AllArgsConstructor;
import map.api.Map;

import java.util.Objects;
import java.util.Optional;

/**
 * Реализация интерфейса Map. Пары ключ-значение лежат в определенных
 * корзинах. Корзины представлины в виде массива и каждая корзина
 * соответсвуют определенному диапозону хэша. При добавлении новой пары
 * вычисляется хэш ключа, которая будет удовлетворять только одной корзине
 * по диапозону хэша корзины. В эту корзину и кладется новая пара.
 * Если пар ключ-значение больше единицы в одной корзине, они выстраиваются
 * в односвязный список.
 */
public class HashMap implements Map {

    /**
     * Количество корзин по умолчанию, если не введен начальный
     * размер.
     */
    private final static int DEFAULT_CAPACITY = 16;

    /**
     * Максимально допустимое количество корзин.
     */
    private final static int MAX_CAPACITY = 1 << 30;

    /**
     * Коэффициент загрузки по умолчанию.
     */
    private final static double DEFAULT_LOAD_FACTOR = 0.75;

    /**
     * Массив корзин
     */
    private Node[] table;

    /**
     * Количество корзин
     */
    private int capacity;

    /**
     * Порог, пройдя который, массив корзин увеличивается вдвое.
     * Порог вычисляется как threshold = capacity * loadFactor;
     */
    private int threshold;

    /**
     * Размер HashMap.
     */
    private int size;

    /**
     * Вложенный класс Node - внутреннее хранение пары
     * ключ-значение. Содержит поле next, которые ссылает
     * на следующую пару, за счет чего обеспечивает построение
     * связанного списка в корзине, каждый элемент которого будем
     * называть узлом.
     */
    @AllArgsConstructor
    private static class Node {

        /**
         * Поле хранит хэш ключа key.
         */
        final int hash;

        /**
         * Хранит значение ключа key.
         */
        final Object key;

        /**
         * Храниет значение value.
         */
        Object value;

        /**
         * Хранит ссылку на следующий узел.
         */
        Node next;

        /**
         * Метод equals, сравнивает два узла по ключу и значению
         *
         * @param o - объект, который будет сравниваться с this
         * @return - true, если объекты схожие, false - если разные.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node)) return false;

            Node node = (Node) o;

            if (!Objects.equals(key, node.key)) return false;
            return Objects.equals(value, node.value);
        }

        /**
         * Вычисляет хэш код узла по композиции хэша значения и ключа.
         *
         * @return - возвращает результирующий хэш код.
         */
        @Override
        public int hashCode() {
            return Objects.hashCode(value) ^ hash;
        }
    }

    /**
     * Конструктор. Инициализирует поля capacity и threshold
     * по входным аргументам.
     *
     * @param capacity   - начальное значение количества корзин.
     * @param loadFactor - начальный коэффициент загрузки.
     * @throws IllegalArgumentException -
     */
    public HashMap(int capacity, double loadFactor) {
        if (capacity < 0) {
            throw new IllegalArgumentException("initial CAPACITY should be positive");
        }
        if (Double.compare(loadFactor, 1) > 0 || Double.compare(loadFactor, 0) < 0) {
            throw new IllegalArgumentException("load factory should be between 0 and 1");
        }
        this.capacity = tableSizeFromCapacity(capacity);
        threshold = (int) (capacity * loadFactor);
    }

    /**
     * Конструктор с начальным размером корзин и коэффициентом
     * загрузки по умолчанию.
     *
     * @param capacity - начальное значение количества корзин.
     */
    public HashMap(int capacity) {
        this(capacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Конструктор с размером корзин по умолчанию и коэффициентом
     * загрузки по умолчанию.
     */
    public HashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Переопределение начального значение capacity в ближайшее
     * верхнее число кратное 2.
     *
     * @param capacity - начальное значение capacity.
     * @return - переопределенное значение.
     */
    private int tableSizeFromCapacity(int capacity) {
        capacity -= 1;
        capacity |= capacity >>> 1;
        capacity |= capacity >>> 2;
        capacity |= capacity >>> 4;
        capacity |= capacity >>> 8;
        capacity |= capacity >>> 16;
        return capacity < 0 ? 1 : (capacity > MAX_CAPACITY) ? MAX_CAPACITY : (capacity + 1);
    }

    /**
     * Реализация метода Map.put. Метод put добавляет пару ключ-значение в Map.
     * Если HashMap пустая, вызывается метод resize() и инициализируется массив
     * корзин начальным размером. При добавлении узла ключ-значение вычисляется
     * индекс корзины, в которую кладется узел, методом findIndex(). В корзине узел
     * кладется в конец односвязного списка, либо, если корзина была пустой, образует
     * новый односвязный список.
     * Если в Map уже имеется значение по ключу, старое значение заменяется новым
     * и возвращается методом.
     * Когда добавляется новый узел в HashMap, размер size увеличивается на единицу.
     * Как только размер больше порога threshold, вызывается метод resize().
     *
     * @param key   - ключ, который собираемся добавить.
     * @param value - значение, которое ассоциируем с ключом.
     * @return - либо null, если добавляемый ключ не хранится в Map, либо,
     * ассоциируемое с ключом, старое значение, если в Map уже имеется добавляемый
     * ключ.
     */
    public Object put(Object key, Object value) {
        int hash = hash(key);
        int index = findIndex(hash, capacity);
        if (table == null) {
            resize();
        }
        Node node = table[index];
        if (node == null) {
            table[index] = new Node(hash, key, value, null);
        } else {
            boolean flag = true;
            Node temp = node;
            while (temp != null) {
                node = temp;
                temp = temp.next;
                if (node.hash == hash &&
                        ((key == null) || (key.equals(node.key)))) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                node.next = new Node(hash, key, value, null);
            } else {
                Object oldValue = node.value;
                node.value = value;
                return oldValue;
            }
        }
        size++;
        if (size > threshold) {
            resize();
        }
        return null;
    }

    /**
     * Метод resize() либо инициализирует массив корзин с начальным размером
     * capacity, если HashMap была пустой, либо увеличивает массив корзин в
     * два раза перестраивая все узлы вызывом метода transform().
     */
    private void resize() {
        if (table == null) {
            table = new Node[capacity];
        } else {
            Node[] newTable;
            int newCapacity = capacity << 1;
            threshold = threshold << 1;
            if (newCapacity >= MAX_CAPACITY) {
                newCapacity = MAX_CAPACITY;
                threshold = Integer.MAX_VALUE;
            }
            newTable = new Node[newCapacity];
            transform(newTable);
            capacity = newCapacity;
            table = newTable;
        }
    }

    /**
     * Метод transform() переводит узлы из текущего массива корзин в
     * новый массив newTable, перестраивая эти узлы по хэшам в новые
     * корзины.
     *
     * @param newTable - новый массив корзин.
     */
    private void transform(Node[] newTable) {
        int newCapacity = newTable.length;
        for (Node bucket : table) {
            if (bucket != null) {
                Node node = bucket;
                do {
                    Node temp = node.next;
                    int index = findIndex(node.hash, newCapacity);
                    node.next = newTable[index];
                    newTable[index] = node;
                    node = temp;
                } while (node != null);
            }
        }
    }

    /**
     * Метод get возвращает значение ассоциируемое с ключом key. Обращается
     * к методу getNode(key), чтобы получить узел с значение ключа key.
     *
     * @param key - ключ, по которому хотим получить значение
     * @return - значение, если ключ хранится в Map, или null, если в Map не
     * имеется такого ключа.
     */
    public Object get(Object key) {
        return Optional.ofNullable(getNode(key)).flatMap((node) -> Optional.ofNullable(node.value)).orElse(null);
    }

    /**
     * Метод contains проверяет содержание в Map ключа key. Если
     * getNode(key) возвращает узел по ключу key, возвращается true.
     * Если getNode(key) возвращает null - false.
     *
     * @param key - ключ, который хотим проверить.
     * @return - возвращает true, если в Map хранится ключ key, false -
     * если не хранится.
     */
    public boolean contains(Object key) {
        return getNode(key) != null;
    }

    /**
     * Метод getNode(key) возвращает узел по ключу.
     * Вычисляется хэш ключа, после ищется соответствующая хэшу
     * корзина. В корзине ключ каджого узла сравнивается с входным ключом
     * по hash() и equals(). Если найден совпадающий ключ, возвращается
     * соответсвующий узел, в обратном случае null.
     *
     * @param key - ключ, по которому ведется поиск узла.
     * @return - узел, если найдено совпадение по ключам, или null
     * в обратном случае.
     */
    private Node getNode(Object key) {
        if (table != null) {
            int hash = hash(key);
            Node node = table[findIndex(hash, capacity)];
            while (node != null) {
                if (hash == node.hash &&
                        ((key == null) || key.equals(node.key))) {
                    return node;
                }
                node = node.next;
            }
        }
        return null;
    }


    /**
     * Метод remove(key) удаляет узел по ключу key.
     * Вычисляется хэш ключа key и, соотвествующая ей, корзина.
     * Если находится совпадающий по ключу узел в корзине, этот узел
     * извлекается из односвязанного списка.
     * @param key - ключ, по которому собираемся удалить пару
     *            ключ-значение.
     * @return возвращает значение из пары ключ-значение, которое было
     * удалено из Map, или null, если в Map не хранится пары ключ-значение,
     * соответствующему ключу key.
     */
    public Object remove(Object key) {
        if (table != null) {
            int hash = hash(key);
            int index = findIndex(hash, capacity);
            Node node = table[index];
            if (node != null) {
                if (hash == node.hash &&
                        ((key == null) || key.equals(node.key))) {
                    table[index] = node.next;
                    node.next = null;
                    size--;
                    return node.value;
                }
                while (node.next != null) {
                    Node removingNode = node.next;
                    if (hash == removingNode.hash &&
                            ((key == null) || key.equals(removingNode.key))) {
                        node.next = removingNode.next;
                        removingNode.next = null;
                        size--;
                        return removingNode.value;
                    }
                    node = removingNode;
                }
            }
        }
        return null;
    }

    /**
     * Возвращает размер HashMap.
     * @return размер HashMap.
     */
    public int size() {
        return size;
    }

    /**
     * Вычислает хэш ключа key.
     * @param key - ключ, у которого вычисляется хэш.
     * @return - возвращает хэш ключа.
     */
    private int hash(Object key) {
        int h;
        return key == null ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * Вычисляет соответсвующий хэшу hash индекс корзины.
     * @param hash - хэш, по которому вычисляется индекс
     * @param capacity - размер массива корзин.
     * @return - индекс корзины.
     */
    private int findIndex(int hash, int capacity) {
        return hash & (capacity - 1);
    }

    /**
     * Метод возвращает строковое представление объекта.
     * В каждой строчке выводится "key =" + key + "value =" + value.
     * @return строковое представление объекта.
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("{").append(System.lineSeparator());
        for (Node node : table) {
            while (node != null) {
                stringBuilder.append("\t")
                        .append("key = ")
                        .append(node.key)
                        .append("\t\tvalue = ")
                        .append(node.value)
                        .append(System.lineSeparator());
                node = node.next;
            }
        }
        return stringBuilder.append("}").toString();
    }

    /**
     * Метод сравнения на одинаковость двух HashMap.
     * Сравнивает по размерам Map и по каждому узлу.
     * @param o - объект для сравнение с this
     * @return true, если объекты схожие, false - в обратном случае
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashMap)) return false;

        HashMap otherHashMap = (HashMap) o;

        if (size != otherHashMap.size) return false;

        for (Node node : table) {
            while (node != null) {
                Object key = node.key;
                if (!node.equals(otherHashMap.getNode(key))) {
                    return false;
                }
                node = node.next;
            }
        }
        return true;
    }

    /**
     * Вычисляет хэш код HashMap, суммируя хэш каждого узла
     * @return хэш код HashMap.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        for (Node node : table) {
            while (node != null) {
                hash += node.hashCode();
                node = node.next;
            }
        }
        return hash;
    }
}
