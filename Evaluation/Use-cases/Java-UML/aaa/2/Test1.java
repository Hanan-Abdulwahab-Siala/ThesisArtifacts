public interface ArticleScoreBehaviour {
    float articleScore();
}
public interface ProjectScoreBehaviour {
    float projectScore();
}
public interface ConferenceScoreBehaviour {
    float conferenceScore();
}
public abstract class Student {
    int id;
    float mid;
    float fin;
    Student(int id, float mid, float fin) {
        this.id = id;
        this.mid = mid;
        this.fin = fin;
    }
    abstract float computeTotalScore();
    float computeBaseScore() {
        return mid*0.4f+fin*0.6f;
    }
}
public class BachelourStudent extends Student implements ProjectScoreBehaviour {
    BachelourStudent(int id, float mid, float fin) {
        super(id, mid, fin);
    }
    float computeTotalScore() {
        return computeBaseScore()+projectScore();
    }
    @Override
    public float projectScore() {
        return 20f;
    }
    @Override
    public String toString() {
        return "\n" + "This is a Bachelour Student with the following academic achievements" + "\n" +
                "ID: " + id + "\n" +
                "Midterm Score: " + mid + "\n" +
                "Final Score: " + fin + "\n" +
                "Project Score: " + projectScore() + "\n" +
                "Total Score: " + computeTotalScore() + "\n";
    }
}
public class MasterStudent extends Student implements ConferenceScoreBehaviour {
    int numberOfConf;
    MasterStudent(int id, float mid, float fin, int numberOfConf) {
        super(id, mid, fin);
        this.numberOfConf = numberOfConf;
    }
    float computeTotalScore() {
        return computeBaseScore()+conferenceScore();
    }
    @Override
    public float conferenceScore() {
        return numberOfConf*5;
    }
    @Override
    public String toString() {
        return "This is a Master Student with the following academic achievements" + "\n" +
                "ID: " + id + "\n" +
                "Midterm Score: " + mid + "\n" +
                "Final Score: " + fin + "\n" +
                "Number of Conferences: " + numberOfConf + "\n" +
                "Conference Score: " + conferenceScore() + "\n" +
                "Total Score: " + computeTotalScore() + "\n";
    }
}
public class PhDStudent extends MasterStudent implements ArticleScoreBehaviour {
    int numberOfArticles;
    PhDStudent(int id, float mid, float fin, int numberOfConf, int numberOfArticles) {
        super(id, mid, fin, numberOfConf);
        this.numberOfArticles = numberOfArticles;
    }
    float computeTotalScore() {
        return computeBaseScore()+articleScore();
    }
    @Override
    public float articleScore() {
        return numberOfArticles*8;
    }
    @Override
    public String toString() {
        return "This is a PhD Student with the following academic achievements" + "\n" +
                "ID: " + id + "\n" +
                "Midterm Score: " + mid + "\n" +
                "Final Score: " + fin + "\n" +
                "Number of Conferences: " + numberOfConf + "\n" +
                "Conference Score: " + conferenceScore() + "\n" +
                "Number of Articles: " + numberOfArticles + "\n" +
                "Article Score: " + articleScore() + "\n" +
                "Total Score: " + computeTotalScore();
    }
}
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