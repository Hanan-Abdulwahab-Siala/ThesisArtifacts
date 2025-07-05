
// Generic methods and constructors -------------------
class Mapper {
    // The class itself is not generic, the constructor is
    <T, V> Mapper(T array, V item) {
    }
    
    /* This method will accept only arrays of the same type as
    the searched item type or its subtype*/
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

// This class is parameterized
class Array<T extends Number> implements Expandable<T> {
    void addItem(T item) {
    }
}

// And this is not and uses an explicit type instead
class IntegerArray implements Expandable<Integer> {
    void addItem(Integer item) {
    }
}

