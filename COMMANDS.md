# API Endpoint Reference

All endpoints are relative to `http://localhost:8080`.

---

## Mod Endpoints

### GET /api/mods

**Description:** Returns all mods. Supports optional query parameters for filtering.  
**Query parameters:**
- `minecraftVersion` – filter by exact Minecraft version (e.g. `?minecraftVersion=1.20.1`)
- `search` – filter by partial name match, case-insensitive (e.g. `?search=opti`)

**Example:**
```
GET /api/mods?minecraftVersion=1.20.1
```

---

### GET /api/mods/{id}

**Description:** Returns the mod with the given ID.  
**Path parameter:** `id` – mod ID  
**Errors:** `404` if the mod does not exist.

**Example:**
```
GET /api/mods/3
```

---

### POST /api/mods

**Description:** Creates a new mod.  
**Request body (JSON):**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | yes | Mod name |
| `version` | string | yes | Mod version |
| `author` | string | yes | Author name |
| `description` | string | no | Short description |
| `fileUrl` | string | no | Download URL for the mod JAR |
| `minecraftVersion` | string | yes | Target Minecraft version |

**Example:**
```json
{
  "name": "OptiFine",
  "version": "HD_U_I5",
  "author": "sp614x",
  "description": "Performance and graphics improvements",
  "fileUrl": "https://example.com/optifine.jar",
  "minecraftVersion": "1.20.1"
}
```

---

### PUT /api/mods/{id}

**Description:** Updates all fields of an existing mod.  
**Path parameter:** `id` – mod ID  
**Request body:** Same fields as `POST /api/mods`.  
**Errors:** `404` if the mod does not exist.

---

### DELETE /api/mods/{id}

**Description:** Deletes the mod with the given ID.  
**Path parameter:** `id` – mod ID  
**Errors:** `404` if the mod does not exist.

---

## Modpack Endpoints

### GET /api/modpacks

**Description:** Returns all modpacks. Supports the same `minecraftVersion` and `search` query parameters as `GET /api/mods`.

---

### GET /api/modpacks/{id}

**Description:** Returns the modpack with the given ID.  
**Errors:** `404` if the modpack does not exist.

---

### POST /api/modpacks

**Description:** Creates a new modpack.  
**Request body (JSON):**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | yes | Modpack name |
| `description` | string | no | Short description |
| `minecraftVersion` | string | yes | Target Minecraft version |

**Example:**
```json
{
  "name": "My Pack",
  "description": "A performance-focused modpack",
  "minecraftVersion": "1.20.1"
}
```

---

### PUT /api/modpacks/{id}

**Description:** Updates the name, description, and Minecraft version of a modpack.  
**Path parameter:** `id` – modpack ID  
**Request body:** Same fields as `POST /api/modpacks`.  
**Errors:** `404` if the modpack does not exist.

---

### DELETE /api/modpacks/{id}

**Description:** Deletes the modpack with the given ID.  
**Errors:** `404` if the modpack does not exist.

---

### POST /api/modpacks/{id}/mods/{modId}

**Description:** Adds a mod to a modpack.  
**Path parameters:** `id` – modpack ID, `modId` – mod ID  
**Errors:**
- `404` if the modpack or mod does not exist.
- `409` if the mod is already in the modpack.

---

### DELETE /api/modpacks/{id}/mods/{modId}

**Description:** Removes a mod from a modpack.  
**Path parameters:** `id` – modpack ID, `modId` – mod ID  
**Errors:**
- `404` if the modpack does not exist, the mod does not exist, or the mod is not in the modpack.

---

### GET /api/modpacks/{id}/build

**Description:** Assembles and returns the modpack including all of its mods.  
**Path parameter:** `id` – modpack ID  
**Errors:** `404` if the modpack does not exist.

**Example response:**
```json
{
  "id": 1,
  "name": "My Pack",
  "description": "A performance-focused modpack",
  "minecraftVersion": "1.20.1",
  "mods": [
    {
      "id": 3,
      "name": "OptiFine",
      "version": "HD_U_I5",
      "author": "sp614x",
      "description": "Performance and graphics improvements",
      "fileUrl": "https://example.com/optifine.jar",
      "minecraftVersion": "1.20.1"
    }
  ]
}
```
