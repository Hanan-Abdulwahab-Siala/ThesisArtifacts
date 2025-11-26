class MedicalRecord {
    String recordType = "General Record";
}
class PatientRecord extends MedicalRecord {
    int patientID = 4;
}
class DoctorRecord extends PatientRecord {
    float consultationFee;
    void processRecord() {
        System.out.println(recordType + " is being processed for patient ID " + patientID + ".");
    }
}
class TreatmentRecord extends PatientRecord {
    int treatmentCode;
}
