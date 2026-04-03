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