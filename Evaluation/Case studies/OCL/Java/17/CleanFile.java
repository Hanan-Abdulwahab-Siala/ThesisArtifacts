class Mapper {
    <T, V> Mapper(T array, V item) {
    }
    static <T, V extends T> boolean contains(T item, V[] arr) {
        for (T currentItem : arr) {
            if (item.equals(currentItem)) {
                return true;
            }
        }
        return false;
    }
}
interface Expandable<T extends Number> {
    void addItem(T item);
}
class Array<T extends Number> implements Expandable<T> {
    void addItem(T item) {
    }
}
class IntegerArray implements Expandable<Integer> {
    void addItem(Integer item) {
    }
}