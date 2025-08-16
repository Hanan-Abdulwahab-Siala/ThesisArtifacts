import java.util.ArrayList;
class Person {
    int personID = 430;
    String name = "John Doe";
    int age = 30;
    ArrayList<DataPoint> dataPoints = new ArrayList<>();
    public Person(int personID, String name, int age) {
        this.personID = personID;
        this.name = name;
        this.age = age;
    }
    public void addDataPoint(DataPoint dp) {
        dataPoints.add(dp);
    }
}
class DataPoint {
    float valueOne = 3.4f;
    float valueTwo = 9.6f;
    String type = "Metric Type A";
    String timestamp = "2025-01-29";
    Person owner;
    public DataPoint(float valueOne, float valueTwo, String type, String timestamp, Person owner) {
        this.valueOne = valueOne;
        this.valueTwo = valueTwo;
        this.type = type;
        this.timestamp = timestamp;
        this.owner = owner;
    }
}

