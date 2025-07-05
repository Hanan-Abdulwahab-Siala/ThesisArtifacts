public @interface Bean {
    public static final String ASDF = "ASDF";
}
class AnnoName<T> {
    static <T> org.host.test.@N Bar<T> fn1(org.host.test.@N Bar<T> p) {
        return null;
    }
    static <T> org.test.@N Bar<T> fn2(org.test.@N Bar<T> p) {
        return null;
    }
    static <T> org.@N Bar<T> fn3(org.@N Bar<T> p) {
        return null;
    }
}