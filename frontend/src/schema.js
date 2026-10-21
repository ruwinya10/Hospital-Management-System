export const fields={
  "patients": [
    "patientNumber",
    "departmentId",
    "dateOfBirth",
    "gender",
    "phone",
    "address",
    "emergencyContact",
    "bloodGroup",
    "allergies"
  ],
  "doctors": [
    "specialization",
    "departmentId",
    "phone",
    "licenseNumber",
    "schedule"
  ],
  "departments": [
    "description",
    "location"
  ],
  "appointments": [
    "patientId",
    "doctorId",
    "date",
    "time",
    "reason",
    "notes"
  ],
  "admissions": [
    "patientId",
    "ward",
    "bed",
    "admissionDate",
    "dischargeDate",
    "type",
    "notes"
  ],
  "medical-records": [
    "patientId",
    "doctorId",
    "visitDate",
    "visitType",
    "diagnosis",
    "treatment",
    "prescription",
    "report"
  ],
  "laboratory-tests": [
    "patientId",
    "doctorId",
    "testName",
    "requestedAt",
    "completedAt",
    "sampleDate",
    "sampleType",
    "result",
    "referenceRange",
    "report"
  ],
  "pharmacy": [
    "medicineCode",
    "category",
    "quantity",
    "reorderLevel",
    "unitPrice",
    "expiryDate",
    "supplier"
  ],
  "bills": [
    "patientId",
    "appointmentId",
    "billDate",
    "consultationCharge",
    "laboratoryCharge",
    "pharmacyCharge",
    "admissionCharge",
    "total",
    "dueDate"
  ],
  "payments": [
    "billId",
    "amount",
    "method",
    "paymentDate",
    "reference"
  ],
  "employees": [
    "staffNumber",
    "departmentId",
    "jobTitle",
    "phone",
    "email",
    "hireDate"
  ],
  "attendance": [
    "employeeId",
    "date",
    "checkIn",
    "checkOut"
  ],
  "leave-records": [
    "employeeId",
    "startDate",
    "endDate",
    "reason",
    "approvalStatus"
  ],
  "documents": [
    "patientId",
    "fileReference",
    "description"
  ]
};
export const names={
  "patients": "Patient",
  "doctors": "Doctor",
  "departments": "Department",
  "appointments": "Appointment",
  "admissions": "Admission",
  "medical-records": "Medical Record",
  "laboratory-tests": "Laboratory Test",
  "pharmacy": "Pharmacy Item",
  "bills": "Bill",
  "payments": "Payment",
  "employees": "Employee",
  "attendance": "Attendance",
  "leave-records": "Leave Record",
  "documents": "Document"
};