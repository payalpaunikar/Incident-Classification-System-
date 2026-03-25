Incident Classification System
A production-ready Spring Boot backend that processes PDF/text documents, classifies each chunk against predefined incident topics using keyword (mandatory keyword + optional) +  scoring, and exposes results via REST APIs.
________________________________________
📑 Table of Contents
1.	Tech Stack
2.	Project Structure
3.	Classification Logic
4.	Setup Instructions
5.	API Reference
6.	Sample Requests & Responses
________________________________________
🛠 Tech Stack
Layer	Technology	Version
Framework	Spring Boot	3.X+
Language	Java	17 LTS
Persistence	Spring Data JPA + MySQL	MySQL 8.2
PDF Parsing	Apache PDFBox	3.0.1
API Docs	SpringDoc OpenAPI / Swagger UI	2.8.5

________________________________________
📁 Project Structure
src/main/java/com/incident/classification/
├── MainApplication.java
├── config/
│   ├── SwaggerConfig.java          # OpenAPI metadata
├── controller/
│   ├── TopicController.java        # CRUD  /api/topics
│   ├── DocumentController.java     # Upload + results  /api/documents
│   └── DashboardController.java    # Stats  /api/dashboard
├── dto/
├── entity/                         # Topic, Document, ClassifiedChunk
├── exception/                      # GlobalExceptionHandler + custom exceptions
├── repository/                     # JPA repositories
├── service                  # Business logic implementations
________________________________________
🧠 Classification Logic
- **Split the text** — the document is broken into small pieces (sentences) so each piece can be checked separately.
 - **Check every piece against every topic** — each sentence is compared to all the topics you created to find the best match.
 - **Full point for exact match** — if a word in the sentence exactly matches a topic keyword, it gets a score of 1.0.
 - **Partial point for close match** — if a word is slightly misspelled (like `"Dilli"` instead of `"Delhi"`), it still gets a score if it is at least 75% similar.
 - **Calculate a confidence score** — the total score is divided by the number of keywords to get a final confidence value between 0 and 1. 
- **Pick the best topic** — whichever topic scores the highest is assigned to that sentence. If nothing matches, it is marked as `UNCLASSIFIED`.
 - **Save everything** — the result, topic name, confidence score, and position are saved to the database and the document is marked as `COMPLETED`.
________________________________________
Setup Instructions
Prerequisites: Java 17+, Maven 3.X+, MySQL 8
Step 1 — Create the MySQL database:
sql
mysql -u root -p
Step 2 — Configure application.properties : 
spring.application.name=incident-classification-system

server.port=8090

server.servlet.context-path=/api

#Database
spring.datasource.url=jdbc:mysql://localhost:3306/incident_classification
spring.datasource.username=root
spring.datasource.password=root@123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# --- JPA Configuration ---
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.open-in-view=false

Step 3 — Run the app with local profile:
bash
./mvnw spring-boot:run 

Swagger UI: http://localhost:8090/api/swagger-ui.html
________________________________________
📡 API Reference
Topics
Method	Endpoint	Description
POST	/api/topics	Create a new incident topic
GET	/api/topics	List all topics (paginated)
GET	/api/topics/{id}	Get topic by ID
PUT	/api/topics/{id}	Update topic title or keywords
DELETE	/api/topics/{id}	Delete topic by ID
Documents
Method	Endpoint	Description
POST	/api/documents/upload/pdf	Upload a PDF file (multipart/form-data)
POST	/api/documents/upload/text	Upload raw text (request body)
GET	/api/documents/{id}/results	Get classified chunks (paginated)
Pagination: ?page=0&size=20&sort=chunkIndex,asc
Dashboard
Method	Endpoint	Description
GET	/api/dashboard	Aggregated system-wide statistics
________________________________________
🧪 Sample Requests & Responses
1. Create a Topic
Request
http
POST /api/topics
Content-Type: application/json

{
  "title": "Nagpur Bomb Blast",
  "mandatoryKeywords": ["Nagpur"],
  "optionalKeywords": ["blast", "explosion", "bomb", "market"]
}
Response 201 Created
json
{
    "success": true,
    "message": "Topic created successfully",
    "data": {
        "id": 9,
        "title": "Nagpur Bomb Blast",
        "mandatoryKeywords": [
            "Nagpur"
        ],
        "optionalKeywords": [
            "blast",
            "explosion",
            "bomb",
            "market"
        ],
        "createdAt": "2026-03-25T16:35:04.5691984"
    },
    "timestamp": "2026-03-25T16:35:04.6048542"
}________________________________________
2. Upload Raw Text
Request
http
POST /api/documents/upload/text
Content-Type: application/json
Response 201 Created
json
{
    "success": true,
    "message": "Text classified successfully",
    "data": {
        "id": 8,
        "fileName": "text-input.txt",
        "sourceType": "TEXT",
        "status": "COMPLETED",
        "createdAt": "2026-03-25T16:36:59.3398499",
        "totalChunks": 1
    },
    "timestamp": "2026-03-25T16:36:59.4314699"
}
________________________________________
4. Get Classification Results
Request
http
GET /api/documents/1/results
Response 200 OK
Json{
    "success": true,
    "data": {
        "documentId": 1,
        "fileName": "text-input.txt",
        "totalChunks": 1,
        "results": [
            {
                "id": 8,
                "text": "A massive explosion occurred in Nagpur causing panic among people",
                "assignedTopic": "Nagpur Bomb Blast",
                "confidence": 0.5,
                "chunkIndex": 0
            }
        ]
    },
    "timestamp": "2026-03-25T16:39:57.2406906"
}

________________________________________
5. Dashboard
Request
http
GET /api/dashboard
Response 200 OK
json
  
{
    "success": true,
    "data": {
        "totalDocuments": 8,
        "totalChunks": 8,
        "classifiedChunks": 8,
        "unclassifiedChunks": 0,
        "topicDistribution": {
            "Delhi Bomb Blas": 2,
            "Building Fire Accident": 2,
            "Highway Road Accident": 1,
            "Flood Disaster": 1,
            "Server Down Issue": 1,
            "Nagpur Bomb Blast": 1
        }
    },
    "timestamp": "2026-03-25T16:41:28.5636099"
}
________________________________________

