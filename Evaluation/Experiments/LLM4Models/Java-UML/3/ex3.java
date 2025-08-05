import java.util.*;
class MedicalData {
  int patientID = 430;
}
class VitalSigns extends MedicalData {
  float heartRate = 3.4f;
  float bloodPressure = 9.6f;
}  
class LabResults extends MedicalData {
  float glucoseLevel = 33.44f;
  float cholesterol = 99.68f;
} 
class DetailedLabResults extends LabResults {
  float hemoglobin = 33.44f;
  float platelets = 99.68f;
} 
class MedicalHistory extends MedicalData {
  float previousConditions = 33.44f;
} 
class PatientRecord extends MedicalHistory {
  int recordNumber = 33;
}
