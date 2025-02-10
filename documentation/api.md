# API Documentation

## Authentication

All endpoints except `/auth/login` and `/auth/register` require JWT bearer authentication.

### Register New User
```http
POST /auth/register
```

**Request Body:**
```json
{
  "username": "string (3-50 chars, alphanumeric + ._-)",
  "name": "string (max 100 chars)",
  "email": "valid email address",
  "password": "string (min 8 chars)",
  "inviteCode": "string (optional)"
}
```

### Login
```http
POST /auth/login
```

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```

Both authentication endpoints return:
```json
{
  "token": "JWT token",
  "user": {
    "id": "string",
    "username": "string",
    "name": "string",
    "email": "string"
  }
}
```

## User Management

### Get Current User Profile
```http
GET /users/me
```

### Update User Profile
```http
PATCH /users/me
```

**Request Body:**
```json
{
  "name": "string (max 100 chars)",
  "email": "string"
}
```

### Change Password
```http
PUT /users/me/password
```

**Request Body:**
```json
{
  "currentPassword": "string",
  "newPassword": "string (min 8 chars)"
}
```

## Tasks

### List Tasks
```http
GET /api/tasks
```

Returns array of TaskDTO objects.

### Create New Task

Instantiate a new task but don't call the Agent yet. This allows
for also adding documents to the task workspace prior to execution.

```http
POST /api/tasks
```

**Request Body:**
```json
{
  "message": "string"
}
```

### Get Task Details
```http
GET /api/tasks/{task_id}
```

### Progress Task

Progress the task for one step. This call is synchronous and will
return when the agent has progressed a single step. Check the boolean `action.final` to see
if the agent expects the task to be completed or not.

You can keep progressing a task for as long as you need. You can provide additional
instructions in between or can leave the message empty to just instruct the agent to keep progressing.

```http
POST /api/tasks/{task_id}
```

**Request Body:**
```json
{
  "message": "string" (optional)
}
```

### List Task Steps
```http
GET /api/tasks/{task_id}/steps
```

### Upload Document to Task
```http
POST /api/tasks/{task_id}/documents
```

**Request Body:** Multipart form data with file

### Download Task Document
```http
GET /api/tasks/{task_id}/documents/{document_id}
```

## Document Management

### List Documents
```http
GET /api/documents
```

Returns array of RagDocumentDTO objects.

### Upload New Document
```http
POST /api/documents
```

**Query Parameters:**
- `title`: string (required)
- `description`: string (required)

**Request Body:** Multipart form data with file

### Get Document Details
```http
GET /api/documents/{documentId}
```

### Delete Document
```http
DELETE /api/documents/{documentId}
```

### Get Document Chunks
```http
GET /api/documents/{documentId}/chunks
```

### Get Specific Chunk
```http
GET /api/chunks/{chunkId}
```

## Data Models

### TaskDTO
```json
{
  "created": "datetime",
  "completed": "datetime",
  "id": "string",
  "title": "string",
  "description": "string",
  "request": "string",
  "context": {
    "key": "value"
  }
}
```

### StepDTO
```json
{
  "input": "string",
  "action": {
    "name": "string",
    "parameters": {
      "key": "value"
    },
    "reasoning": "string",
    "final": "boolean"
  },
  "result": {
    "status": "string",
    "details": "object",
    "summary": "string",
    "error": "string",
    "message": "string"
  },
  "documents": [
    {
      "taskId": "string",
      "filename": "string",
      "metadata": {
        "key": "value"
      }
    }
  ]
}
```

### RagDocumentDTO
```json
{
  "id": "string",
  "filename": "string",
  "title": "string",
  "description": "string",
  "contentType": "string",
  "progress": "float"
}
```

### RagChunkDTO
```json
{
  "id": "string",
  "documentId": "string",
  "documentTitle": "string",
  "documentDescription": "string",
  "content": "string"
}
```