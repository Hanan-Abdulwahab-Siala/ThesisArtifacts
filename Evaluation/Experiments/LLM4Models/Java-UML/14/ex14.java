public class Employee {
    public String name = "John Doe";
    private double salary = 50000.0;
    protected int age = 30;
    String department = "HR";
    public void displayDetails() {
        System.out.println("Name: " + name);
        System.out.println("Salary: " + salary);
        System.out.println("Age: " + age);
        System.out.println("Department: " + department);
    }
}