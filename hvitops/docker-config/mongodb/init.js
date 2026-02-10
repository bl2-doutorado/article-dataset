// Initialize MongoDB databases and collections

// Connect to admin database
db = db.getSiblingDB('admin');

// Create user for MongoDB
db.createUser({
  user: 'hvitops_user',
  pwd: 'hvitops_password',
  roles: [
    { role: 'readWrite', db: 'hvitops_laboratory' },
    { role: 'readWrite', db: 'hvitops_records' }
  ]
});

// Switch to laboratory database
db = db.getSiblingDB('hvitops_laboratory');

// Create lab_tests collection with validation
db.createCollection('lab_tests', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['patientId', 'date', 'status'],
      properties: {
        _id: { bsonType: 'objectId' },
        patientId: { bsonType: 'long' },
        date: { bsonType: 'date' },
        status: { enum: ['scheduled', 'pending_results', 'completed'] },
        items: {
          bsonType: 'array',
          items: {
            bsonType: 'object',
            properties: {
              testType: { bsonType: 'string' },
              result: { bsonType: 'string' },
              unit: { bsonType: 'string' },
              referenceRange: { bsonType: 'string' }
            }
          }
        }
      }
    }
  }
});

// Insert seed data for lab tests
db.lab_tests.insertMany([
  {
    patientId: 1,
    date: new Date('2026-01-20'),
    status: 'completed',
    items: [
      { testType: 'Blood Glucose', result: '95', unit: 'mg/dL', referenceRange: '70-100' },
      { testType: 'Hemoglobin', result: '14.5', unit: 'g/dL', referenceRange: '13.5-17.5' }
    ]
  },
  {
    patientId: 2,
    date: new Date('2026-01-21'),
    status: 'pending_results',
    items: [
      { testType: 'Cholesterol', result: null, unit: 'mg/dL', referenceRange: '<200' }
    ]
  },
  {
    patientId: 3,
    date: new Date('2026-01-22'),
    status: 'scheduled',
    items: [
      { testType: 'Triglycerides', result: null, unit: 'mg/dL', referenceRange: '<150' }
    ]
  }
]);

// Create indexes
db.lab_tests.createIndex({ patientId: 1 });
db.lab_tests.createIndex({ date: 1 });
db.lab_tests.createIndex({ status: 1 });

// Switch to records database
db = db.getSiblingDB('hvitops_records');

// Create medical_records collection
db.createCollection('medical_records', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['patientId', 'date'],
      properties: {
        _id: { bsonType: 'objectId' },
        patientId: { bsonType: 'long' },
        physicianId: { bsonType: 'long' },
        date: { bsonType: 'date' },
        diagnosis: { bsonType: 'string' },
        prescriptions: {
          bsonType: 'array',
          items: { bsonType: 'string' }
        },
        clinicalNotes: { bsonType: 'string' },
        createdAt: { bsonType: 'date' },
        updatedAt: { bsonType: 'date' }
      }
    }
  }
});

// Insert seed data for medical records
db.medical_records.insertMany([
  {
    patientId: 1,
    physicianId: 101,
    date: new Date('2026-01-15'),
    diagnosis: 'Hypertension',
    prescriptions: ['Lisinopril 10mg daily', 'Amlodipine 5mg daily'],
    clinicalNotes: 'Patient presents with elevated blood pressure. Started on antihypertensive therapy.',
    createdAt: new Date('2026-01-15'),
    updatedAt: new Date('2026-01-15')
  },
  {
    patientId: 2,
    physicianId: 102,
    date: new Date('2026-01-18'),
    diagnosis: 'Type 2 Diabetes',
    prescriptions: ['Metformin 500mg twice daily', 'Glipizide 5mg daily'],
    clinicalNotes: 'Patient diagnosed with type 2 diabetes. Referred to nutritionist.',
    createdAt: new Date('2026-01-18'),
    updatedAt: new Date('2026-01-18')
  },
  {
    patientId: 3,
    physicianId: 101,
    date: new Date('2026-01-20'),
    diagnosis: 'Common Cold',
    prescriptions: ['Acetaminophen 500mg as needed', 'Rest and fluids'],
    clinicalNotes: 'Patient has viral upper respiratory infection. Advised symptomatic treatment.',
    createdAt: new Date('2026-01-20'),
    updatedAt: new Date('2026-01-20')
  }
]);

// Create indexes
db.medical_records.createIndex({ patientId: 1 });
db.medical_records.createIndex({ physicianId: 1 });
db.medical_records.createIndex({ date: 1 });
