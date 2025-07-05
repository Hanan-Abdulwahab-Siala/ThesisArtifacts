public class Mapper<T extends ArrayList & Formattable, V> {
    public void add(T array, V item) {
        array.add(item);
        Mapper<CustomList, Integer> mapper = new Mapper<CustomList, Integer>();
        Mapper<CustomList, ?> mapper;
        mapper = new Mapper<CustomList, Boolean>();
        mapper = new Mapper<CustomList, Integer>();
    }
}