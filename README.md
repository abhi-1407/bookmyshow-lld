BookMyShow - Low Level Design

A simplified BookMyShow-like system built to practice Low-Level Design (LLD), Object-Oriented Design, concurrency handling, and backend engineering concepts.

Functional Requirements

1. A user should be able to select a city.
2. A user should be able to browse movies and shows available in the selected city.
3. A user should be able to view available seats for a selected show.
4. A user should be able to temporarily lock seats for a maximum of 5 minutes.
5. The system should prevent multiple users from locking or booking the same seat concurrently.
6. A user should be able to create a booking for locked seats.
7. A user should be able to make a payment for the booking.
8. On successful payment, the booking should be confirmed.
9. On payment failure or seat-lock expiration, the temporary locks should be released.

Non-Functional Requirements

1. The system should provide high availability and low latency for browsing movies and shows.
2. Seat-locking and booking operations should maintain strong consistency.
3. The system should correctly handle concurrent booking attempts for the same seats.
4. The design should be extensible to support additional pricing strategies, seat types, and payment methods.

Back-of-the-Envelope Estimation

* The system is expected to be read-heavy.
* Browsing movies, shows, and seat availability occurs significantly more frequently than booking operations.
* Booking operations are less frequent but require strong consistency and concurrency control.

Domain Model

Entities

* User
* City
* Theatre
* Screen
* Movie
* Show
* Seat
* ShowSeat
* SeatLock
* Booking
* Payment

Enums

* SeatType
* SeatStatus
* BookingStatus
* PaymentStatus

Important Design Decisions

Show vs Movie

A Movie contains metadata about a movie.

A Show represents a scheduled screening of a movie on a particular screen at a particular time.

This allows the same movie to have multiple shows across different screens, theatres, cities, and timings.

Seat vs ShowSeat

A Seat represents a physical seat inside a screen.

A ShowSeat represents the state and pricing of a physical seat for a specific show.

The same physical seat can therefore have different availability states across different shows.

10:00 AM Show → A1 → AVAILABLE
02:00 PM Show → A1 → BOOKED
06:00 PM Show → A1 → AVAILABLE

Seat Pricing

Pricing is stored on ShowSeat.

This allows the price of the same physical seat to vary between shows.

BigDecimal is used for monetary values to avoid floating-point precision issues.

Seat Locking

Seat locking is modeled using a separate SeatLock entity.

A SeatLock contains:

* The locked ShowSeat
* The user who owns the lock
* The lock creation time

The lock expires after five minutes.

Separating SeatLock from ShowSeat allows lock ownership, expiration, validation, and release behavior to be managed independently.

Booking Domain Invariant

A booking can contain multiple ShowSeats, but every seat must belong to the same Show.

This invariant is validated by BookingService.

Repository Layer

The project uses repository interfaces to separate business logic from persistence implementation details.

Current repositories:

* ShowRepository
* ShowSeatRepository
* SeatLockRepository
* BookingRepository
* PaymentRepository

The current implementations use ConcurrentHashMap for in-memory persistence.

Services depend on repository abstractions instead of concrete implementations.

BookingService
↓
BookingRepository
↑
InMemoryBookingRepository

The in-memory repositories can later be replaced with database-backed implementations without modifying the service layer.

Service Layer

SeatLockService

Responsible for:

* Locking selected seats
* Preventing concurrent locking of the same seats
* Validating lock ownership
* Validating lock expiration
* Releasing temporary locks

BookingService

Responsible for:

* Validating that selected seats belong to the requested show
* Validating seat-lock ownership
* Creating a booking in the PENDING state

PaymentService

Responsible for:

* Validating booking state
* Calculating and validating the booking amount
* Processing payment attempts
* Updating payment status
* Updating booking status
* Marking seats as booked
* Releasing temporary seat locks

Booking Flow

User selects Show
↓
User selects ShowSeats
↓
SeatLockService.lockSeats()
↓
Seats temporarily locked
↓
BookingService.createBooking()
↓
Validate seat ownership and show invariant
↓
Booking created as PENDING
↓
PaymentService.processPayment()
↙                           ↘
SUCCESS                        FAILURE
↓                              ↓
Payment SUCCESS                 Payment FAILED
↓                              ↓
Booking CONFIRMED               Booking CANCELLED
↓                              ↓
ShowSeats BOOKED                Seat locks released
↓
Temporary locks removed

Concurrency Handling

Seat locking involves a check-then-act operation:

Check whether seat is locked
↓
If unlocked
↓
Create SeatLock

Without concurrency control, multiple threads could observe the same seat as available and attempt to lock it simultaneously.

Initial Approach: Method-Level Synchronization

The initial implementation used a synchronized method.

This prevented race conditions but introduced coarse-grained locking.

Users attempting to book unrelated seats would unnecessarily block each other.

Current Approach: Per-Seat ReentrantLock

The current implementation maintains one ReentrantLock per ShowSeat.

ShowSeat A1 → ReentrantLock A1
ShowSeat A2 → ReentrantLock A2
ShowSeat B1 → ReentrantLock B1

This allows users booking unrelated seats to proceed concurrently while preventing concurrent modification of the same seat.

The locks are stored in a ConcurrentHashMap.

ConcurrentHashMap<ShowSeatId, ReentrantLock>

computeIfAbsent() is used to atomically retrieve or create the lock associated with a seat.

Preventing Deadlocks

A booking can contain multiple seats.

Without consistent lock ordering, two threads could deadlock:

Thread 1 locks A1 → waits for A2
Thread 2 locks A2 → waits for A1

Before acquiring locks, seats are sorted by ID.

Thread 1 → A1 → A2
Thread 2 → A1 → A2

This establishes a consistent global lock ordering and prevents circular wait.

Atomic Multi-Seat Locking

The implementation follows a two-phase approach:

Acquire all per-seat ReentrantLocks
↓
Validate ALL requested seats
↓
If any validation fails
Reject the entire operation
↓
Otherwise create ALL SeatLocks
↓
Release ReentrantLocks

Validating all seats before creating any SeatLock prevents partially locked bookings.

ReentrantLock vs SeatLock

The project uses two different locking concepts.

ReentrantLock provides thread-level synchronization inside the application.

SeatLock is a domain entity representing a user’s temporary reservation of a seat.

ReentrantLock
→ Which thread can currently access the critical section?
SeatLock
→ Which user owns the seat reservation for five minutes?

Lock Lifecycle

ReentrantLocks are not removed from the lock registry after use.

Removing a lock carelessly could allow multiple lock objects to exist for the same seat while other threads still hold references to the previous lock.

Temporary domain SeatLocks are removed after successful payment or payment failure.

Concurrent Booking Simulation

The application includes a concurrent booking simulation using:

* ExecutorService
* CountDownLatch
* AtomicReference

Two users attempt to lock the same seats concurrently.

Example:

Abhilash and Rahul attempt to lock seats A1 and A2 concurrently.
Rahul successfully locks A1 and A2.
Abhilash attempts to acquire the same seats and is rejected.
Rahul creates a booking.
Booking Status → PENDING
Payment succeeds.
Payment Status → SUCCESS
Booking Status → CONFIRMED
A1 Status → BOOKED
A2 Status → BOOKED
Temporary SeatLocks → RELEASED

CountDownLatch is used to ensure both threads begin competing for the seats at approximately the same time.

AtomicReference is used to safely record which user successfully acquired the seat locks.

Payment Amount Validation

The total booking amount is calculated from the prices of the selected ShowSeats.

Java Streams are used to:

ShowSeats
↓
Map each ShowSeat to its price
↓
Reduce all prices into one total

BigDecimal.compareTo() is used for numeric comparison instead of equals().

Booking State Validation

Payment can only be processed for bookings in the PENDING state.

Valid state transitions:

PENDING → CONFIRMED
PENDING → CANCELLED

State validation prevents invalid booking transitions.

True payment idempotency would require an additional mechanism such as idempotency keys.

Exception Handling

IllegalArgumentException

Used when the caller provides invalid input.

Examples:

* Incorrect payment amount
* Seats belonging to a different show

IllegalStateException

Used when an operation cannot be performed because of the current state.

Examples:

* Processing payment for a non-pending booking
* Attempting to lock an already locked seat

Java and Backend Concepts Practiced

* Object-Oriented Design
* Low-Level Design
* Domain Modeling
* Domain Invariants
* Repository Pattern
* Dependency Inversion
* Separation of Concerns
* Java Streams
* Optional
* BigDecimal
* UUID
* ConcurrentHashMap
* synchronized
* ReentrantLock
* ExecutorService
* CountDownLatch
* AtomicReference
* Race Conditions
* Deadlock Prevention
* Lock Ordering
* Check-Then-Act Problems
* Multi-Seat Atomic Locking
* Exception Handling

Design Patterns and Principles Used

Repository Pattern

Persistence logic is abstracted behind repository interfaces.

Dependency Inversion

Services depend on repository abstractions instead of concrete in-memory implementations.

Separation of Concerns

Responsibilities are separated between domain objects, repositories, and services.

Current Limitations

* Concurrency control works within a single JVM instance.
* Multiple application instances would require database-level concurrency control or distributed coordination.
* The current lock registry can grow as new ShowSeat IDs are encountered.
* Payment processing is simulated.
* Custom domain exceptions have not yet been introduced.

Future Improvements

* Unit and concurrency tests using JUnit
* Persistent database implementation
* Database-level concurrency handling
* Idempotent payment processing
* Custom domain exceptions
* Dynamic pricing
* Pricing strategies
* Multiple payment gateways
* Booking cancellation and refunds
* Notifications
* Seat recommendations
* REST APIs using Spring Boot