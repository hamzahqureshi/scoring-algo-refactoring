// Initialize the scoring_algo database
db = db.getSiblingDB('scoring_algo');

// Create collections
db.createCollection('surveys');
db.createCollection('survey_responses');
db.createCollection('candidates');
db.createCollection('applications');
db.createCollection('companies');

// Create indexes for better performance
db.surveys.createIndex({ "id": 1 }, { unique: true });
db.survey_responses.createIndex({ "id": 1 }, { unique: true });
db.candidates.createIndex({ "id": 1 }, { unique: true });
db.applications.createIndex({ "id": 1 }, { unique: true });
db.companies.createIndex({ "id": 1 }, { unique: true });

print('Database scoring_algo initialized successfully!'); 