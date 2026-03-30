# NumberGuessingGame-BE

Backend (Spring Boot) for the “Number Guessing Game”.

## 1. Prerequisites
- Java 21+
- Gradle wrapper (included). First run may download Gradle dependencies.
- MySQL (change DB connection in `application.properties` if needed)

## 2. Configure environment
Edit `src/main/resources/application.properties`:

### Database
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

### JWT
- `jwt.secret` must be **at least 32 bytes**

### VNPay (optional)
- `vnpay.tmn-code`
- `vnpay.hash-secret`
- `vnpay.backend-public-base-url`, `vnpay.frontend-base-url`

## 3. Cookie-based Authentication (important)
This backend uses cookie-based JWT:
- `access_token` cookie (httpOnly) used to authenticate **protected APIs**
- `refresh_token` cookie (httpOnly) used by `/auth/refresh`

In most cases you will:
1. `POST /auth/register`
2. `POST /auth/login` (store cookies)
3. Call protected APIs (game/payment)
4. If access token expires: `POST /auth/refresh`
5. To end session: `POST /auth/logout`

## 4. Build / Run
From `NumberGuessingGame-BE/`:

```bash
# build
./gradlew clean build

# run (recommended for dev)
./gradlew bootRun
```

Server base URL:
- `http://localhost:8080`

JPA:
- `spring.jpa.hibernate.ddl-auto=update` (auto-create/update tables)

## 5. API Base URL
All REST endpoints are under:
- `/api/v1`

## 6. Quick API Test (curl)
### 6.1 Register
```bash
curl -i -X POST "http://localhost:8080/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"player1","password":"secret12"}'
```

Validation rules:
- `username`: length 3..50
- `password`: length 6..255

If validation fails, you will get HTTP **400** with a JSON response like:
```json
{
  "message": "Validation failed",
  "errors": {
    "username": "...",
    "password": "..."
  }
}
```

### 6.2 Login (store cookies)
```bash
curl -i -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"player1","password":"secret12"}' \
  -c cookies.txt
```

### 6.3 Refresh (access token only)
```bash
curl -i -X POST "http://localhost:8080/api/v1/auth/refresh" \
  -b cookies.txt -c cookies.txt
```

Note: your `refresh_token` is validated from the existing cookie, but the endpoint only sets a new `access_token` cookie (it does not rotate refresh).

### 6.4 Logout (clear cookies)
```bash
curl -i -X POST "http://localhost:8080/api/v1/auth/logout" \
  -b cookies.txt -c cookies.txt
```

### 6.5 Get current user
```bash
curl -i -X GET "http://localhost:8080/api/v1/user/me" \
  -b cookies.txt
```

## 7. Game APIs
### 7.1 Start a new round
```bash
curl -i -X POST "http://localhost:8080/api/v1/game/start-round" \
  -b cookies.txt
```

### 7.2 Guess (number 1..5)
```bash
curl -i -X POST "http://localhost:8080/api/v1/game/guess" \
  -H "Content-Type: application/json" \
  -d '{"number":3}' \
  -b cookies.txt
```

### 7.3 Round status
```bash
curl -i -X GET "http://localhost:8080/api/v1/game/round-status" \
  -b cookies.txt
```

### 7.4 Leaderboard
```bash
curl -i -X GET "http://localhost:8080/api/v1/game/leaderboard" \
  -b cookies.txt
```

## 8. VNPay Payment (buy turns)
### 8.1 Create VNPay payment
Endpoint:
- `POST /api/v1/payment/buy-turns`

It returns a `paymentUrl` you must open in a browser to complete payment:
```bash
curl -i -X POST "http://localhost:8080/api/v1/payment/buy-turns" \
  -b cookies.txt
```

### 8.2 VNPay callbacks (backend)
- ReturnUrl (browser): `GET /api/v1/payment/vnpay/return`
- IPN (server-to-server): `GET/POST /api/v1/payment/vnpay/ipn`

Backend verifies `vnp_SecureHash` and, on success, grants turns and records transactions.

## 9. Postman notes
- For protected APIs: make sure Postman is sending the `access_token` cookie.
- After login, enable “Cookies” and save cookies (or use Postman cookie jar).
- If you forget cookies, you will receive HTTP **401** / **403** with a JSON error body (depending on the auth state).