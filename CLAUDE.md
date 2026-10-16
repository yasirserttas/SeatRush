# CLAUDE.md - SeatRush Project Guide

## 1. Proje Amacı ve Mimari Yapı

**SeatRush**, etkinlik, koltuk, bilet ve kullanıcı yönetimini sağlayan yüksek performanslı bir **Bilet Satış & Rezervasyon Platformudur**.

### Teknoloji Yığını
- **Java 21**, **Spring Boot 3/4**, **Spring Modulith 2.1**
- **Spring Data JPA**, **PostgreSQL**, **Flyway** (Veritabanı migration)
- **MapStruct 1.6**, **Lombok**, **Springdoc OpenAPI**

### Mimari Desen (Modular Monolith / Hexagonal / CQRS)
Proje, **Spring Modulith** mimarisinde **Hexagonal Architecture (Ports & Adapters)** ve **CQRS** (Command Query Responsibility Segregation) prensipleriyle yapılandırılmıştır.

```
src/main/java/com/seatrush/
├── [module_name]/                  # user, event, seat, ticket
│   ├── domain/                     # Pure Java Domain Logic (Entities, Value Objects, Domain Exceptions)
│   ├── application/                # Business Use Cases & Ports
│   │   ├── port/in/                # Inbound Ports (Use Case Interfaces)
│   │   ├── port/out/               # Outbound Ports (Repositories, Event Publishers, External Clients)
│   │   ├── service/                # Domain Services (Business implementation)
│   │   └── command|query/          # Command & Query DTOs / Records
│   └── infrastructure/             # Adapters & Technical Framework Details
│       ├── web/                    # REST Controllers, Request/Response DTOs, ExceptionHandlers
│       ├── persistence/            # JPA Entities, Spring Data Repositories, Adapters, MapStruct Mappers
│       ├── transaction|transactional/# Transactional Decorator Wrappers (@Transactional)
│       ├── config/                 # Spring Java Config (@Configuration Beans)
│       └── messaging|publisher|client/# Event Listeners, Publishers & Inter-module Adapters
```

---

## 2. Ana Modüller ve Kritik Bileşenler

| Modül | Görevi | Kritik Class/Interface & Fonksiyonlar |
| :--- | :--- | :--- |
| **`user`** | Kullanıcı hesap yönetimi, rol ve durum takibi | - `User` (Domain Entity)<br>- `UserCommandUseCase` / `UserQueryUseCase` (Inbound Ports)<br>- `UserCommandService` (Kullanıcı kaydı, güncelleme, şifre değiştirme)<br>- `TransactionalUserCommandUseCase` (Transaction decorator) |
| **`event`** | Etkinlik tanımlama, yayınlama ve kapasite yönetimi | - `Event` (Domain Entity)<br>- `EventCommandService.create()` (Etkinlik oluşturur ve `EventCreatedEvent` yayımlar)<br>- `EventPublisher` / `SpringEventPublisherAdapter` (Event publishing adapter) |
| **`seat`** | Etkinlik koltuklarının üretimi, rezervasyonu ve satışı | - `Seat` (Domain Entity)<br>- `EventCreatedListener` (`EventCreatedEvent` dinleyerek otomatik koltuk üretir)<br>- `SeatCommandService.reserve()` / `release()` / `sell()` |
| **`ticket`** | Bilet rezervasyonu, kod üretimi ve bilet doğrulama | - `Ticket` (Domain Entity - `ACTIVE`, `USED`, `CANCELLED` durumları)<br>- `TicketCommandService.create()` (Kullanıcıya koltuk rezerve edip bilet üretir)<br>- `SeatReservationAdapter` (`seat` modülü ile bilet modülü arasındaki senkron adaptör) |

---

## 3. Veri Akışı (Data Flow) ve API Uçları

### Kritik Veri Akışları (Data Flows)

1. **Etkinlik & Otomatik Koltuk Oluşturma (Asenkron Event Flow):**
   `POST /events` ➔ `EventCommandController` ➔ `EventCommandService` (Save Event & publish `EventCreatedEvent`) ➔ `@ApplicationModuleListener EventCreatedListener` ➔ `SeatCommandUseCase.createSeatsForEvent()` (Toplu koltuk üretimi).

2. **Bilet Satın Alma / Rezervasyon Flow:**
   `POST /tickets` ➔ `TicketCommandController` ➔ `TicketCommandService` ➔ `SeatReservationPort.reserve(seatId)` (`SeatReservationAdapter` ➔ `SeatCommandUseCase.reserve()`) ➔ `Ticket` Domain Entity üretimi & DB Kaydı.

### Kritik API Endpoints

- **User API (`/users`)**
  - `POST /users` - Yeni kullanıcı kaydı
  - `GET /users/{id}`, `GET /users/by-email`, `GET /users/by-username`, `GET /users`
  - `PUT /users/{id}` - Profil güncelleme
  - `PATCH /users/{id}/password`, `PATCH /users/{id}/activate`, `PATCH /users/{id}/deactivate`

- **Event API (`/events`)**
  - `POST /events` - Etkinlik oluşturma (Koltuklar otomatik tetiklenir)
  - `GET /events`, `GET /events/{id}`
  - `PUT /events/{id}`, `PATCH /events/{id}/activate`, `PATCH /events/{id}/cancel`

- **Seat API (`/seats`, `/events/{eventId}/seats`)**
  - `GET /seats`, `GET /seats/{seatId}`, `GET /events/{eventId}/seats`
  - `PATCH /seats/{seatId}/reserve`, `PATCH /seats/{seatId}/release`

- **Ticket API (`/tickets`, `/users/{userId}/tickets`)**
  - `POST /tickets` - Bilet alma/rezerve etme
  - `GET /tickets/{id}`, `GET /tickets/code/{ticketCode}`, `GET /users/{userId}/tickets`
  - `PATCH /tickets/{id}/use`, `PATCH /tickets/{id}/cancel`

---

## 4. Kodlama Standartları ve Kurallar

1. **Strict Layering & Pure Domain Rule:**
   - `domain/` paketi tamamen saf Java'dır. Spring, JPA veya external kütüphane anotasyonu barındırmaz.
   - Spring Anotasyonları (`@Service`, `@Transactional`, `@Repository`) domain katmanına veya domain servislerine sızmamalıdır.

2. **Transaction Yönetimi (Decorator Pattern):**
   - Servis sınıflarında (`*CommandService`) `@Transactional` kullanılmaz.
   - Transaction mantığı `infrastructure/transaction/` altındaki `Transactional*UseCase` decorator sınıflarında tutulur.

3. **Spring Modulith İletişim Kuralları:**
   - Modüller doğrudan birbirinin persistence/entity sınıflarına erişemez.
   - Modüller arası etkileşim ya asenkron domain event'leri (`@ApplicationModuleListener`) ya da explicit Port/Adapter arayüzleri (`SeatReservationPort`) üzerinden sağlanır.

4. **Persistence & DTO Separation:**
   - Domain Model (`User`, `Seat`, `Event`, `Ticket`) ile Persistence JPA Entity (`*JpaEntity`) kesinlikle ayrı tutulur.
   - Dönüşümler MapStruct mapper'ları (`*Mapper`) ile yapılır.
   - Web DTO'ları `Java Record` olarak tanımlanır (`CreateUserRequest`, `TicketResponse`, vb.).

5. **Veritabanı Değişiklikleri (Flyway):**
   - Şema değişiklikleri doğrudan JPA ddl-auto ile yapılmaz (`spring.jpa.hibernate.ddl-auto=validate`).
   - Tüm DDL SQL scriptleri `src/main/resources/db/migration/V*__description.sql` dizinine eklenir.
