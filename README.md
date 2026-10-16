# SeatRush

**Concurrency-safe event ticketing & seat reservation backend**
Java 21 · Spring Boot 4 · Spring Modulith · Hexagonal Architecture · PostgreSQL · Redis · Grafana k6

[Türkçe](#türkçe) | [English](#english)

> **TR — Özet:** 50 kullanıcı aynı koltuğa aynı anda istek attığında, lock olmadan **1 koltuk 10 kişiye satıldı**. `PESSIMISTIC_WRITE` lock ile **tam 1 kişiye** satıldı. Her iki sonuç da k6 ile ölçüldü. Redis cache de ölçüldü: küçük veride daha yavaş çıktı ve nedeni analiz edildi.
>
> **EN — TL;DR:** With 50 users hitting the same seat at once, **one seat was sold to 10 people** without a lock. With a `PESSIMISTIC_WRITE` lock it was sold to **exactly 1**. Both results were measured with k6. The Redis cache was measured too: it was slower on small data, and the README explains why.

---

<a id="türkçe"></a>

## Türkçe

### Bu proje ne anlatıyor?

SeatRush bir bilet satış ve koltuk rezervasyon backend'i. Asıl amacı CRUD yazmak değil, bilet sistemlerindeki klasik soruyu **ölçerek** cevaplamak:

> *50 kişi aynı koltuğa aynı anda tıklarsa ne olur?*

Bu soruyu cevaplarken şu yolu izledim:

1. **Önce hatayı kanıtladım.** k6 ile 50 kullanıcıyı aynı koltuğa aynı anda gönderdim. Lock olmadığı için **1 koltuk 10 kişiye satıldı.**
2. **Sonra çözdüm.** Veritabanı seviyesinde `PESSIMISTIC_WRITE` (`SELECT ... FOR UPDATE`) lock ekledim.
3. **Sonra tekrar ölçtüm.** Aynı test: **1 başarılı, 49 reddedilen istek.** Double booking kalmadı.
4. **Redis cache ekledim ve onu da ölçtüm.** Küçük veri setinde ve tek makinede Redis **daha yavaş** çıktı. Bu sonucu saklamadım, nedenini analiz ettim (aşağıda).

### Öne çıkanlar

| Konu | Ne yaptım |
| :--- | :--- |
| **Race condition** | Hatayı k6 ile tekrarlanabilir şekilde ürettim, pessimistic lock ile çözdüm, tekrar ölçerek doğruladım |
| **Ölçüme dayalı karar** | "Redis hızlandırır" varsayımını test ettim. Küçük veride yavaşlattığını sayılarla gösterdim |
| **Hexagonal Architecture** | `domain/` paketi saf Java. Spring, JPA ya da Lombok bağımlılığı yok |
| **Decorator Pattern** | `@Transactional` ve `@Cacheable` iş kodunda değil, decorator sınıflarında duruyor |
| **Spring Modulith** | Modül sınırlarını `ModuleStructureTests` test olarak doğruluyor. Modüller birbiriyle event ya da açık API üzerinden konuşuyor |
| **Event-driven akış** | Etkinlik oluşturulunca `EventCreated` event'i yayınlanıyor, `seat` modülü koltukları kendisi üretiyor |
| **Güvenlik** | JWT access ve refresh token, BCrypt, rol bazlı yetkilendirme (`ADMIN`, `ORGANIZER`) |
| **Veritabanı** | Flyway migration'ları, `ddl-auto=validate` (şemayı Hibernate değil migration'lar yönetiyor) |

---

### 1. Race Condition: Kanıt → Çözüm → Doğrulama

**Senaryo:** `k6/race-condition-test.js`. 50 sanal kullanıcı (`shared-iterations`, 50 VU, 50 iterasyon) **aynı koltuğa** aynı anda `PATCH /seats/{id}/reserve` isteği atıyor.

| Metrik | Lock yok | `PESSIMISTIC_WRITE` |
| :--- | :---: | :---: |
| Eşzamanlı kullanıcı | 50 | 50 |
| Başarılı rezervasyon (`204`) | **10** (hata) | **1** (doğru) |
| Reddedilen istek (`400`) | 40 | 49 |
| `http_req_duration` avg | 109.9 ms | 99.3 ms |
| `http_req_duration` p95 | 122.57 ms | 121.18 ms |
| Veri tutarlılığı | 1 koltuk 10 kişiye satıldı | 1 koltuk 1 kişiye satıldı |

> Not: `http_req_failed` oranının yüksek çıkması (%78 ve %96) bekleniyor. Reddedilen her rezervasyon `400` dönüyor ve k6 bunu "failed" sayıyor. Asıl bakılması gereken metrik `successful_reserves`.

**Lock yokken neden 50 değil de 10 başarılı rezervasyon?** Çakışma zamanlamaya bağlı. Sadece okuma anları üst üste binen istekler koltuğu `AVAILABLE` görüp rezerve edebiliyor. Geç gelen istekler commit edilmiş `RESERVED` durumunu okuyup reddediliyor. Bu yüzden sayı her çalıştırmada değişebilir. Önemli olan 1'den büyük olması.

Bu ölçekte (50 istek) lock'un gecikmeye **belirgin bir etkisi görülmedi** (p95: 122.57 ms → 121.18 ms). Bu kadar küçük fark ölçüm gürültüsü sınırında. Çok daha yüksek eşzamanlılıkta satır kilidi için bekleme süresi artabilir. Bunu ayrıca ölçmek gerekiyor.

<details>
<summary><b>k6 ekran görüntüleri</b></summary>

**Lock yok, 10 başarılı rezervasyon:**

![Race condition - lock yok](k6kan%C4%B1t/1.png)

**Pessimistic lock ile tam 1 başarılı rezervasyon:**

![Race condition - pessimistic lock](k6kan%C4%B1t/2-racecondition.png)

</details>

#### Neden oluyordu? (Read-Modify-Write)

```
Thread A: SELECT ... WHERE id = ?   → AVAILABLE
Thread B: SELECT ... WHERE id = ?   → AVAILABLE
Thread A: seat.reserve()            → RESERVED (bellekte)
Thread B: seat.reserve()            → RESERVED (bellekte)
Thread A: UPDATE seats ...          → commit
Thread B: UPDATE seats ...          → commit (A'nın yazdığını ezer, hata yok)
```

Domain kuralı (`Seat.reserve()` yalnızca `AVAILABLE` koltuğu rezerve eder) doğru yazılmıştı. Ama her thread koltuğun **eski** halini okuduğu için kural işe yaramadı.

#### Çözüm

```java
// SpringDataSeatRepository.java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM SeatJpaEntity s WHERE s.id = :id")
Optional<SeatJpaEntity> findByIdWithLock(@Param("id") UUID id);
```

PostgreSQL'de bu sorgu `SELECT ... FOR UPDATE` olarak çalışıyor. İlk transaction satırı kilitliyor, diğerleri sırada bekliyor. Kilit açıldığında sıradaki transaction koltuğu `RESERVED` olarak okuyor. Domain kuralı `InvalidSeatStateException` fırlatıyor ve istemciye `400 Bad Request` dönüyor.

Lock **sadece `reserve` işleminde** kullanılıyor. Okuma endpoint'leri kilitlenmiyor.

**Neden optimistic değil de pessimistic lock?** Bilet satışında aynı satır için çakışma *beklenen* durum, istisna değil. Optimistic lock (`@Version`) bu kadar yoğun çakışmada çok fazla retry ve hata üretirdi. Pessimistic lock bu senaryoda daha öngörülebilir davranıyor.

---

### 2. Redis Cache: Beklenmeyen Sonuç ve Analizi

`GET /events/{eventId}/seats` (koltuk listesi) endpoint'ine Redis cache ekledim. Bunu Hexagonal yapıyı bozmadan **Decorator** olarak yaptım:

```
SeatQueryController
      │
      ▼
CachedSeatQueryUseCase          (infrastructure/cache, @Cacheable)
      │  cache miss
      ▼
TransactionalSeatQueryUseCase   (infrastructure/transaction, @Transactional)
      │
      ▼
SeatQueryService                (saf Java, iş mantığı)
      │
      ▼
PostgreSQL
```

#### Cache invalidation: eski veriyi göstermemek

Okuma cache'i tek başına yetmiyor. Koltuk rezerve edildiğinde liste hâlâ `AVAILABLE` gösterirse kullanıcı dolu koltuğa tıklar. Bu yüzden komut tarafına da bir decorator ekledim:

```
SeatCommandController / SeatReservationApi (ticket modülü)
      │
      ▼
CacheEvictingSeatCommandUseCase   (infrastructure/cache)
      │
      ▼
TransactionalSeatCommandUseCase   (@Transactional)
      │
      ▼
SeatCommandService
```

- `reserve`, `release`, `sell`, `delete`, `create` ve `createSeatsForEvent` işlemlerinden sonra **sadece o etkinliğin** cache kaydı (`eventSeats::{eventId}`) siliniyor. Diğer etkinliklerin cache'i duruyor (`allEntries=true` kullanmadım).
- Silme işlemi **transaction commit edildikten sonra** yapılıyor (`TransactionAwareCacheDecorator`). Böylece commit'ten önce gelen bir okuma cache'e eski veriyi geri yazamıyor.
- İşlem hata verirse (örneğin koltuk zaten rezerve edilmişse) cache silinmiyor, çünkü veri değişmedi.
- Davranış `CacheEvictingSeatCommandUseCaseTest` ile test ediliyor.

Sonra ölçtüm. **Senaryo:** `k6/read-performance-test.js`. 20 VU, 10 saniye, küçük veri seti, her şey aynı makinede (localhost).

| Metrik | PostgreSQL (cache yok) | Redis cache | Fark |
| :--- | :---: | :---: | :---: |
| Toplam istek | 3216 | 3114 | -3% |
| Throughput | **319.97 req/s** | 294.46 req/s | -8% |
| avg | **2.61 ms** | 4.57 ms | +75% |
| median | **2.46 ms** | 3.23 ms | +31% |
| p90 | **2.99 ms** | 6.20 ms | +107% |
| p95 | **3.36 ms** | 8.94 ms | +166% |
| p99 | **11.46 ms** | 18.89 ms | +65% |
| max | **20.47 ms** | 568.42 ms | ilk istek / soğuk cache |
| Hata oranı | 0% | 0% | — |

<details>
<summary><b>k6 ekran görüntüleri</b></summary>

**Cache yok (PostgreSQL):**

![Read test - PostgreSQL](k6kan%C4%B1t/redis-1.png)

**Redis cache:**

![Read test - Redis](k6kan%C4%B1t/redis-2.png)

</details>

#### Redis neden daha yavaş çıktı?

- **Veri küçük.** PostgreSQL bu veriyi zaten kendi bellek tamponundan (shared buffers) okuyor. Diske gitmiyor, yani Redis'in ortadan kaldıracağı bir I/O maliyeti yok.
- **Ek ağ adımı.** Redis Docker Desktop (WSL2) içinde çalışıyor. Her istek sanal ağ köprüsünden bir kez daha geçiyor.
- **Serileştirme maliyeti.** Cache hit olduğunda bile `List<SeatView>` Redis'ten okunup Java nesnesine geri çevriliyor.
- **Soğuk başlangıç.** 568 ms'lik `max` değeri ilk cache miss'ten ve Redis bağlantısının ilk kez açılmasından geliyor.

#### Bu sonuçtan ne öğrendim?

> **Cache her durumda hızlandırmaz. Bir trade-off'tur ve ölçülmeden eklenmemelidir.**

Redis'in gerçek değeri tek bir isteği birkaç milisaniye hızlandırmak değil. Asıl değeri **veritabanının yükünü azaltmak**: binlerce eşzamanlı kullanıcı aynı etkinliğin koltuklarını listelediğinde PostgreSQL'in connection pool'unun (HikariCP) tükenmesini önlemek. Bu etkiyi görmek için daha büyük veri, daha yüksek eşzamanlılık ve ayrı makinelerde çalışan servisler gerekiyor. Bu bir sonraki adım (aşağıdaki yol haritasına bakın).

---

### Mimari

```
com.seatrush
├── user/        Kullanıcı hesapları, rol ve durum yönetimi
├── event/       Etkinlik yaşam döngüsü. EventCreated event'ini yayınlar
├── seat/        Koltuk üretimi, rezervasyon, lock ve cache
├── ticket/      Bilet üretimi ve doğrulama. Koltuğu SeatReservationApi ile rezerve eder
├── security/    JWT kimlik doğrulama ve rol bazlı yetkilendirme
└── common/      Ortak hata cevabı (ErrorResponse)

Her modülün içi:
├── domain/           Saf Java: entity, value object, domain exception
├── application/      Use case portları (in/out), servisler, command/query
└── infrastructure/   web, persistence, transaction, cache, messaging, config
```

**Uyduğum kurallar:**

- **Domain saf Java.** `Seat`, `Event`, `Ticket` ve `User` sınıflarında framework anotasyonu yok. İş kuralları (örneğin "sadece `AVAILABLE` koltuk rezerve edilir") burada duruyor.
- **Servislerde `@Transactional` yok.** Transaction yönetimi `Transactional*UseCase` decorator'larında. Bean'leri `*ModuleConfig` sınıfları elle bağlıyor.
- **JPA entity ile domain modeli ayrı.** `SeatJpaEntity` ve `Seat` ayrı sınıflar. Aralarındaki dönüşümü MapStruct yapıyor.
- **Modüller birbirinin içine erişmiyor.** `ticket`, `seat` modülünün repository'sine erişmiyor. Sadece `SeatReservationApi` arayüzünü kullanıyor. `event` ile `seat` ise `@ApplicationModuleListener` üzerinden asenkron haberleşiyor.
- **CQRS.** Command ve Query use case'leri, controller'ları ve portları ayrı.

### Akışlar

**Etkinlik oluşturma → otomatik koltuk üretimi (asenkron):**
`POST /events` → `EventCommandService` → `EventCreated` yayınlanır → `EventCreatedListener` (seat modülü) → `createSeatsForEvent()`

**Bilet alma (senkron, lock'lu):**
`POST /tickets` → `TicketCommandService` → `SeatReservationPort` → `SeatReservationAdapter` → `SeatReservationApi.reserve()` → `SELECT ... FOR UPDATE` → `Ticket` oluşturulur

### Testler

- **Domain testleri:** `SeatTest`, `EventTest`, `TicketTest`, `UserTest` (durum geçişleri ve validasyonlar)
- **Servis testleri:** Command ve Query servislerinin tamamı (`*CommandServiceTest`, `*QueryServiceTest`)
- **Güvenlik testleri:** `JwtProviderTest`, `JwtAuthenticationFilterTests`, `AuthServiceTest`, `CustomUserDetailsServiceTests`
- **Cache testi:** `CacheEvictingSeatCommandUseCaseTest` (doğru etkinliğin cache'i siliniyor mu, hata olunca cache korunuyor mu)
- **Mimari testi:** `ModuleStructureTests`. Spring Modulith modül sınırlarını test olarak doğruluyor
- **Yük testleri:** `k6/` klasöründeki scriptler

```powershell
./mvnw test
```

### Kurulum ve Çalıştırma

**Gereksinimler:** Java 21, Docker, [Grafana k6](https://k6.io/)

```powershell
# 1) PostgreSQL ve Redis'i başlat
docker compose up -d

# 2) Uygulamayı başlat (Flyway şemayı otomatik oluşturur)
./mvnw spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

**Konfigürasyon:** DB bağlantısı, JWT secret ve Redis ayarları environment variable'lardan okunuyor (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `REDIS_HOST`, `REDIS_PORT`). Tanımlı değilse yerel geliştirme için varsayılan değerler kullanılıyor. Liste için `.env.example` dosyasına bakın. Production'da `JWT_SECRET` ve `DB_PASSWORD` mutlaka değiştirilmeli.

**Yük testlerini çalıştırma:**

```powershell
# 1) Test verisi hazırla: kullanıcı, etkinlik ve koltuklar. Çıktıda TOKEN, EVENT_ID ve SEAT_ID yazar
k6 run k6/setup-test-data.js

# 2) Bu değerleri race-condition-test.js ve reset-seat.js dosyalarına yapıştır
k6 run k6/race-condition-test.js

# 3) Testi tekrar koşmadan önce koltuğu AVAILABLE durumuna geri al
k6 run k6/reset-seat.js

# 4) Okuma performansı testi (EVENT_ID'yi script içinde güncelle)
k6 run k6/read-performance-test.js
```

> Etkinlik oluşturmak için `ADMIN` ya da `ORGANIZER` rolü gerekiyor. `setup-test-data.js` 403 alırsa, scriptin konsola yazdığı SQL komutuyla kullanıcının rolünü güncelle.

### Bilinen Eksikler ve Yol Haritası

- [x] **Cache invalidation:** koltuk durumu değişince ilgili etkinliğin cache'i commit sonrası siliniyor (`CacheEvictingSeatCommandUseCase`).
- [ ] **Gerçekçi cache benchmark:** büyük veri seti (10k+ koltuk), 500+ VU ve ayrı host'ta çalışan DB/Redis ile ölçüp HikariCP pool metriklerini karşılaştırmak.
- [ ] **Concurrency integration testi:** k6 senaryosunu Testcontainers ve `ExecutorService` ile JUnit testine çevirip CI'da otomatik koşturmak.
- [ ] **Alternatif lock stratejileri:** optimistic lock (`@Version`) ve Redis distributed lock ile aynı senaryoyu karşılaştırmak.
- [ ] **Rezervasyon süresi:** `RESERVED` koltuğun belli bir süre sonra otomatik olarak `AVAILABLE` durumuna dönmesi.
- [x] **Konfigürasyon:** DB şifresi, JWT secret ve Redis ayarları environment variable'lardan okunuyor. `docker-compose.yml` ile altyapı tek komutla ayağa kalkıyor.

### Teknolojiler

| Katman | Teknoloji |
| :--- | :--- |
| Dil | Java 21 |
| Framework | Spring Boot 4.1, Spring Security, Spring Data JPA, Spring Cache |
| Mimari | Spring Modulith 2.1, Hexagonal (Ports & Adapters), CQRS, Decorator |
| Veri | PostgreSQL 16, Redis, Flyway |
| Mapping | MapStruct 1.6, Lombok |
| Güvenlik | JJWT 0.13 (access + refresh token), BCrypt |
| API Dokümantasyonu | springdoc-openapi (Swagger UI) |
| Test | JUnit 5, Spring Modulith Test, Testcontainers, Grafana k6 |

---

<a id="english"></a>

## English

### What is this project about?

SeatRush is a ticketing and seat reservation backend. The goal is not to show CRUD. The goal is to answer the classic ticketing question **with measurements**:

> *What happens when 50 people click the same seat at the same moment?*

Here is how I approached it:

1. **I proved the bug first.** I used k6 to send 50 users at the same seat at the same moment. With no locking, **one seat was booked by 10 people.**
2. **I fixed it.** I added a database-level `PESSIMISTIC_WRITE` lock (`SELECT ... FOR UPDATE`).
3. **I measured again.** Same test: **1 success, 49 rejected requests.** No more double booking.
4. **I added a Redis cache and measured that too.** On a small dataset on a single machine, Redis turned out **slower**. I kept that result in the README and analyzed why (see below).

### Highlights

| Topic | What I did |
| :--- | :--- |
| **Race condition** | Reproduced the bug with k6, fixed it with a pessimistic lock, and confirmed the fix with a second run |
| **Data-driven decisions** | Tested the assumption that "Redis makes it faster" and showed with numbers that it made reads slower on small data |
| **Hexagonal Architecture** | The `domain/` package is plain Java, with no Spring, JPA or Lombok dependencies |
| **Decorator Pattern** | `@Transactional` and `@Cacheable` sit in decorator classes, not in business code |
| **Spring Modulith** | `ModuleStructureTests` checks module boundaries as a test. Modules talk to each other only through events or explicit APIs |
| **Event-driven flow** | Creating an event publishes `EventCreated`, and the `seat` module generates the seats on its own |
| **Security** | JWT access and refresh tokens, BCrypt, role-based authorization (`ADMIN`, `ORGANIZER`) |
| **Database** | Flyway migrations with `ddl-auto=validate`, so migrations own the schema, not Hibernate |

---

### 1. Race Condition: Proof → Fix → Verification

**Scenario:** `k6/race-condition-test.js`. 50 virtual users (`shared-iterations`, 50 VUs, 50 iterations) send `PATCH /seats/{id}/reserve` to **the same seat** at the same moment.

| Metric | No lock | `PESSIMISTIC_WRITE` |
| :--- | :---: | :---: |
| Concurrent users | 50 | 50 |
| Successful reservations (`204`) | **10** (bug) | **1** (correct) |
| Rejected requests (`400`) | 40 | 49 |
| `http_req_duration` avg | 109.9 ms | 99.3 ms |
| `http_req_duration` p95 | 122.57 ms | 121.18 ms |
| Data integrity | 1 seat booked by 10 people | 1 seat booked by 1 person |

> Note: the high `http_req_failed` rate (78% and 96%) is expected. Every rejected reservation returns `400`, and k6 counts that as "failed". The metric to look at is `successful_reserves`.

**Why 10 successful reservations without a lock, and not 50?** It depends on timing. Only requests whose reads overlap see the seat as `AVAILABLE` and reserve it. Later requests read the committed `RESERVED` state and get rejected. So the number can change from run to run. What matters is that it's greater than 1.

At this scale (50 requests) the lock had **no noticeable effect on latency** (p95: 122.57 ms → 121.18 ms). A difference this small is within measurement noise. Under much higher concurrency, waiting on the row lock could add latency, and that needs to be measured separately.

<details>
<summary><b>k6 screenshots</b></summary>

**No lock, 10 successful reservations:**

![Race condition - no lock](k6kan%C4%B1t/1.png)

**Pessimistic lock, exactly 1 successful reservation:**

![Race condition - pessimistic lock](k6kan%C4%B1t/2-racecondition.png)

</details>

#### Why did it happen? (Read-Modify-Write)

```
Thread A: SELECT ... WHERE id = ?   → AVAILABLE
Thread B: SELECT ... WHERE id = ?   → AVAILABLE
Thread A: seat.reserve()            → RESERVED (in memory)
Thread B: seat.reserve()            → RESERVED (in memory)
Thread A: UPDATE seats ...          → commit
Thread B: UPDATE seats ...          → commit (overwrites A, no error)
```

The domain rule was correct: `Seat.reserve()` only reserves an `AVAILABLE` seat. It still failed, because every thread was reading an **old** copy of the seat.

#### The fix

```java
// SpringDataSeatRepository.java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM SeatJpaEntity s WHERE s.id = :id")
Optional<SeatJpaEntity> findByIdWithLock(@Param("id") UUID id);
```

On PostgreSQL this runs as `SELECT ... FOR UPDATE`. The first transaction locks the row and the others wait in line. When the lock is released, the next transaction reads the seat as `RESERVED`. The domain rule then throws `InvalidSeatStateException`, and the client gets `400 Bad Request`.

The lock is used **only for `reserve`**. Read endpoints are never locked.

**Why pessimistic and not optimistic locking?** In ticketing, many users competing for the same row is *expected*, not an edge case. Optimistic locking (`@Version`) would cause a lot of retries and failures under contention this high. A pessimistic lock behaves more predictably here.

---

### 2. Redis Cache: An Unexpected Result and Why

I added a Redis cache to `GET /events/{eventId}/seats` (the seat listing). I added it as a **Decorator** so the hexagonal structure stays intact:

```
SeatQueryController
      │
      ▼
CachedSeatQueryUseCase          (infrastructure/cache, @Cacheable)
      │  cache miss
      ▼
TransactionalSeatQueryUseCase   (infrastructure/transaction, @Transactional)
      │
      ▼
SeatQueryService                (plain Java business logic)
      │
      ▼
PostgreSQL
```

#### Cache invalidation: never show stale seats

A read cache is not enough on its own. If a seat gets reserved but the list still shows `AVAILABLE`, users will click a seat that is already taken. So I added a decorator on the command side too:

```
SeatCommandController / SeatReservationApi (ticket module)
      │
      ▼
CacheEvictingSeatCommandUseCase   (infrastructure/cache)
      │
      ▼
TransactionalSeatCommandUseCase   (@Transactional)
      │
      ▼
SeatCommandService
```

- After `reserve`, `release`, `sell`, `delete`, `create` and `createSeatsForEvent`, only **that event's** cache entry (`eventSeats::{eventId}`) is evicted. Other events keep their cache (no `allEntries=true`).
- Eviction happens **after the transaction commits** (`TransactionAwareCacheDecorator`), so a read that arrives before the commit can't put old data back into the cache.
- If the command fails (for example, the seat is already reserved), the cache is left alone, because the data didn't change.
- This behaviour is covered by `CacheEvictingSeatCommandUseCaseTest`.

Then I measured it. **Scenario:** `k6/read-performance-test.js`. 20 VUs, 10 seconds, small dataset, everything on the same machine (localhost).

| Metric | PostgreSQL (no cache) | Redis cache | Delta |
| :--- | :---: | :---: | :---: |
| Total requests | 3216 | 3114 | -3% |
| Throughput | **319.97 req/s** | 294.46 req/s | -8% |
| avg | **2.61 ms** | 4.57 ms | +75% |
| median | **2.46 ms** | 3.23 ms | +31% |
| p90 | **2.99 ms** | 6.20 ms | +107% |
| p95 | **3.36 ms** | 8.94 ms | +166% |
| p99 | **11.46 ms** | 18.89 ms | +65% |
| max | **20.47 ms** | 568.42 ms | first request / cold cache |
| Error rate | 0% | 0% | — |

<details>
<summary><b>k6 screenshots</b></summary>

**No cache (PostgreSQL):**

![Read test - PostgreSQL](k6kan%C4%B1t/redis-1.png)

**Redis cache:**

![Read test - Redis](k6kan%C4%B1t/redis-2.png)

</details>

#### Why was Redis slower?

- **The data is small.** PostgreSQL already serves it from its own memory buffers (shared buffers) without touching the disk, so there is no I/O cost for Redis to remove.
- **Extra network hop.** Redis runs inside Docker Desktop (WSL2), so every request crosses a virtual network bridge one more time.
- **Serialization cost.** Even on a cache hit, the `List<SeatView>` has to be read from Redis and turned back into Java objects.
- **Cold start.** The 568 ms `max` comes from the first cache miss and the first Redis connection being opened.

#### What I learned

> **A cache is not automatically faster. It is a trade-off, and it should be measured before it is added.**

The real value of Redis is not making one request a few milliseconds faster. Its real value is **taking load off the database**: when thousands of users list seats for the same event at once, it stops PostgreSQL's connection pool (HikariCP) from running out. Seeing that effect needs a larger dataset, higher concurrency and services on separate machines. That is the next step (see the roadmap below).

---

### Architecture

```
com.seatrush
├── user/        User accounts, roles and status
├── event/       Event lifecycle. Publishes EventCreated
├── seat/        Seat generation, reservation, locking and caching
├── ticket/      Ticket issuing and validation. Reserves seats through SeatReservationApi
├── security/    JWT authentication and role-based authorization
└── common/      Shared error response (ErrorResponse)

Inside each module:
├── domain/           Plain Java: entities, value objects, domain exceptions
├── application/      Use case ports (in/out), services, commands and queries
└── infrastructure/   web, persistence, transaction, cache, messaging, config
```

**Rules I followed:**

- **The domain is plain Java.** `Seat`, `Event`, `Ticket` and `User` have no framework annotations. Business rules (for example, "only an `AVAILABLE` seat can be reserved") live here.
- **No `@Transactional` in services.** Transactions are handled by `Transactional*UseCase` decorators. The `*ModuleConfig` classes wire the beans by hand.
- **JPA entities and domain models are separate.** `SeatJpaEntity` and `Seat` are different classes, and MapStruct converts between them.
- **Modules don't reach into each other.** `ticket` never touches the `seat` repository. It only uses the `SeatReservationApi` interface. `event` and `seat` talk asynchronously through `@ApplicationModuleListener`.
- **CQRS.** Commands and queries have separate use cases, controllers and ports.

### Flows

**Create event → seats generated automatically (async):**
`POST /events` → `EventCommandService` → publishes `EventCreated` → `EventCreatedListener` (seat module) → `createSeatsForEvent()`

**Buy a ticket (sync, locked):**
`POST /tickets` → `TicketCommandService` → `SeatReservationPort` → `SeatReservationAdapter` → `SeatReservationApi.reserve()` → `SELECT ... FOR UPDATE` → `Ticket` is created

### Tests

- **Domain tests:** `SeatTest`, `EventTest`, `TicketTest`, `UserTest` (state transitions and validation)
- **Service tests:** every command and query service (`*CommandServiceTest`, `*QueryServiceTest`)
- **Security tests:** `JwtProviderTest`, `JwtAuthenticationFilterTests`, `AuthServiceTest`, `CustomUserDetailsServiceTests`
- **Cache test:** `CacheEvictingSeatCommandUseCaseTest` (is the right event's cache evicted, and is the cache kept when the command fails)
- **Architecture test:** `ModuleStructureTests` checks Spring Modulith module boundaries as a test
- **Load tests:** the scripts in the `k6/` folder

```powershell
./mvnw test
```

### Getting Started

**Requirements:** Java 21, Docker, [Grafana k6](https://k6.io/)

```powershell
# 1) Start PostgreSQL and Redis
docker compose up -d

# 2) Start the app (Flyway creates the schema automatically)
./mvnw spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

**Configuration:** the DB connection, JWT secret and Redis settings are read from environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `REDIS_HOST`, `REDIS_PORT`). If they aren't set, local development defaults are used. See `.env.example` for the list. In production, `JWT_SECRET` and `DB_PASSWORD` must be changed.

**Running the load tests:**

```powershell
# 1) Create test data: a user, an event and its seats. The output prints TOKEN, EVENT_ID and SEAT_ID
k6 run k6/setup-test-data.js

# 2) Paste those values into race-condition-test.js and reset-seat.js
k6 run k6/race-condition-test.js

# 3) Set the seat back to AVAILABLE before running the test again
k6 run k6/reset-seat.js

# 4) Read performance test (update EVENT_ID inside the script)
k6 run k6/read-performance-test.js
```

> Creating events requires the `ADMIN` or `ORGANIZER` role. If `setup-test-data.js` gets a 403, update the user's role with the SQL command the script prints to the console.

### Known Limitations & Roadmap

- [x] **Cache invalidation:** when a seat's status changes, that event's cache is evicted after commit (`CacheEvictingSeatCommandUseCase`).
- [ ] **Realistic cache benchmark:** measure with a large dataset (10k+ seats), 500+ VUs and DB/Redis on separate hosts, and compare HikariCP pool metrics.
- [ ] **Concurrency integration test:** turn the k6 scenario into a JUnit test with Testcontainers and an `ExecutorService`, and run it automatically in CI.
- [ ] **Other locking strategies:** run the same scenario with optimistic locking (`@Version`) and a Redis distributed lock, and compare.
- [ ] **Reservation timeout:** automatically set a `RESERVED` seat back to `AVAILABLE` after a set time.
- [x] **Configuration:** the DB password, JWT secret and Redis settings are read from environment variables, and `docker-compose.yml` starts the infrastructure with one command.

### Tech Stack

| Layer | Technology |
| :--- | :--- |
| Language | Java 21 |
| Framework | Spring Boot 4.1, Spring Security, Spring Data JPA, Spring Cache |
| Architecture | Spring Modulith 2.1, Hexagonal (Ports & Adapters), CQRS, Decorator |
| Data | PostgreSQL 16, Redis, Flyway |
| Mapping | MapStruct 1.6, Lombok |
| Security | JJWT 0.13 (access + refresh tokens), BCrypt |
| API Docs | springdoc-openapi (Swagger UI) |
| Testing | JUnit 5, Spring Modulith Test, Testcontainers, Grafana k6 |
