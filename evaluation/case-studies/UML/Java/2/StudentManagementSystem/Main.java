class Main {
    public static void main(String[] args) {
        BachelourStudent bs = new BachelourStudent(1025727, 70, 80);
        MasterStudent ms = new MasterStudent(5683429, 90, 80, 2);
        PhDStudent phd = new PhDStudent(8976541, 80, 70, 6, 3);
        Student[] myStudents = {bs, ms, phd};
        for(Student s : myStudents) {
            System.out.println(s);
        }
    }
}