BookMyShow - Low Level Design

A simplified BookMyShow-like system designed to practice Low-Level Design (LLD), Object-Oriented Design, concurrency handling, domain modeling, and backend engineering concepts.

⸻

Functional Requirements

1. A user should be able to select a city.
2. A user should be able to browse movies and shows available in the selected city.
3. A user should be able to view available seats for a selected show.
4. A user should be able to select and temporarily lock seats for a maximum of 5 minutes.
5. The system should prevent other users from booking or locking seats that are already locked by another user.
6. A user should be able to make a payment for the selected seats.
7. On successful payment, the booking should be confirmed.
8. On payment failure or seat-lock expiration, the locked seats should be released.
9. A user should receive a confirmation once the booking is successfully completed.

⸻

Non-Functional Requirements

1. The system should provide high availability and low latency for browsing movies and shows.
2. The system should maintain strong consistency for seat-locking and booking operations to prevent double booking.
3. The system should correctly handle concurrent booking attempts for the same seat.
4. The design should be extensible to support additional seat types, pricing strategies, and payment methods.

⸻

Back-of-the-Envelope Estimation

* Read operations are expected to be significantly higher than write operations.
* Browsing movies, shows, and seat availability is read-heavy.
* Booking operations are less frequent but require strong consistency and concurrency control.

⸻

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

⸻

Entity Relationships

City
└── Theatre
└── Screen
├── Seat
└── Show
├── Movie
└── ShowSeat
└── Seat
User
├── Booking
│    ├── Show
│    └── ShowSeats
│
└── SeatLock
Booking
└── Payment(s)

⸻

Important Design Decisions

Domain Objects vs Persistence Models

The project currently models relationships using object references rather than database identifiers.

For example:

Payment → Booking
Show → Movie
Show → Screen
Seat → Screen

instead of storing fields such as bookingId, movieId, or screenId inside domain objects.

This keeps the domain model focused on object relationships rather than database implementation details.

⸻

Show vs Movie

A Movie contains metadata about a movie.

A Show represents a scheduled screening of a movie on a particular screen at a particular time.

This separation allows the same movie to have multiple shows across different screens, theatres, cities, and timings.

⸻

Seat vs ShowSeat

A Seat represents a physical seat inside a screen.

A ShowSeat represents the state and pricing of a physical seat for a particular show.

For example, the same physical seat can have different states for different shows:

10:00 AM Show → A1 → AVAILABLE
02:00 PM Show → A1 → BOOKED
06:00 PM Show → A1 → LOCKED

Therefore, seat availability belongs to ShowSeat rather than Seat.

⸻

Seat Pricing

Pricing is stored on ShowSeat.

This allows the price of the same physical seat to vary between shows.

BigDecimal is used instead of double for monetary values to avoid floating-point precision issues.

⸻

Seat Locking

Seat locking is modeled using a separate SeatLock entity rather than embedding lock information directly inside ShowSeat.

A SeatLock contains:

* The locked ShowSeat
* The user who owns the lock
* The lock start time

The lock expires after five minutes.

Keeping locking as a separate domain concept allows lock ownership, expiration, validation, and release behavior to be managed independently.

⸻

Booking Domain Invariant

A booking can contain multiple ShowSeats, but every seat in the booking must belong to the same Show.

This is a domain invariant enforced by BookingService.

⸻

Repository Layer

The application uses repository interfaces to separate domain and service logic from persistence implementation details.

Current repositories:

* ShowRepository
* ShowSeatRepository
* SeatLockRepository
* BookingRepository
* PaymentRepository

The current implementation uses in-memory repositories backed by ConcurrentHashMap.

Services depend on repository interfaces rather than concrete implementations.

This allows the persistence implementation to be replaced without modifying business logic.

For example:

BookingService
↓
BookingRepository
↑
InMemoryBookingRepository

The in-memory implementation could later be replaced with a PostgreSQL or JPA-backed implementation.

⸻

Service Layer

SeatLockService

Responsible for:

* Locking selected seats
* Preventing multiple users from acquiring the same seat lock
* Validating lock ownership
* Checking lock expiration
* Releasing locks

BookingService

Responsible for:

* Validating that all selected seats belong to the requested show
* Validating that the user owns valid locks for all selected seats
* Creating a booking in the PENDING state

PaymentService

Responsible for:

* Processing payment attempts
* Validating booking state
* Validating the payment amount
* Updating payment status
* Updating booking status
* Marking seats as booked after successful payment
* Releasing temporary seat locks

⸻

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
Validate ShowSeat domain invariant
↓
Validate lock ownership
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

⸻

Payment Amount Validation

The payment amount is calculated from the prices of all ShowSeats belonging to the booking.

Java Streams are used to calculate the total:

ShowSeats
↓
Map each ShowSeat to its price
↓
Reduce all prices into a single total

The supplied payment amount must match the calculated booking amount.

BigDecimal.compareTo() is used instead of equals() because values with different scales, such as 10.0 and 10.00, should be treated as numerically equal.

⸻

Booking State Validation

Payment processing is only allowed when a booking is in the PENDING state.

Valid transitions:

PENDING → CONFIRMED
PENDING → CANCELLED

Attempting to process payment for an already confirmed or cancelled booking results in an invalid state operation.

State validation should not be confused with idempotency.

True payment idempotency would require mechanisms such as idempotency keys to prevent duplicate requests from being processed concurrently.

⸻

Concurrency Handling

Initial Problem

Seat locking involves a check-then-act operation:

Check whether seat is locked
↓
If unlocked
↓
Create lock

Without synchronization, two threads could execute the operation concurrently:

Thread 1 → checks A1 → AVAILABLE
Thread 2 → checks A1 → AVAILABLE
Thread 1 → creates lock
Thread 2 → creates lock

This creates a race condition.

⸻

Current Approach

The current SeatLockService uses method-level synchronized locking.

This ensures that only one thread can execute the seat-locking critical section at a time.

Thread 1 enters
↓
Validates seats
↓
Creates locks
↓
Exits
Thread 2 enters
↓
Observes existing locks
↓
Fails

⸻

Atomic Multi-Seat Locking

An earlier implementation validated and locked seats in the same loop.

For example, when attempting to lock:

A1, A2, A3

if A3 was already locked, A1 and A2 could already have been locked before the operation failed.

This resulted in a partially updated state.

The current implementation uses two phases:

Phase 1
Validate ALL requested seats
↓
If any seat cannot be locked
Reject the complete operation
↓
Phase 2
Create ALL seat locks

Since both phases execute inside the same synchronized critical section, another thread cannot modify the lock state between validation and lock creation.

This provides all-or-nothing multi-seat locking for the current in-memory implementation.

⸻

Current Concurrency Limitation

Method-level synchronization is coarse-grained.

For example:

User 1 → Booking A1
User 2 → Booking Z10

Even though the users are booking unrelated seats, one request must wait for the other because the entire lockSeats() method is synchronized.

A future improvement is to implement finer-grained per-seat locking using ReentrantLock.

⸻

Exception Handling

The project currently uses:

IllegalArgumentException

Used when the caller provides invalid input.

Examples:

* Incorrect payment amount
* Seats belonging to a different show

IllegalStateException

Used when an operation cannot be performed because of the current system or object state.

Examples:

* Processing payment for a non-pending booking
* Attempting to lock an already locked seat

Custom domain exceptions can be introduced as a future improvement.

⸻

Java Concepts Practiced

* Object-Oriented Design
* Domain Modeling
* Domain Invariants
* Interfaces
* Dependency Inversion
* Repository Pattern
* Java Streams
* Optional
* BigDecimal
* UUID
* ConcurrentHashMap
* synchronized
* Race Conditions
* Check-Then-Act Problems
* Atomic Operations
* Exception Handling

⸻

Design Patterns and Principles Used

Repository Pattern

Persistence logic is abstracted behind repository interfaces.

Dependency Inversion

Services depend on repository abstractions instead of concrete in-memory implementations.

Separation of Concerns

Responsibilities are divided between:

Domain Objects
Repositories
Services

Each layer has a separate responsibility.

⸻

Class Diagram

To be added.

⸻

Testing

Tests will cover the most important business and concurrency scenarios:

* A user can successfully lock available seats.
* A second user cannot lock an already locked seat.
* Expired seat locks can be acquired again.
* Multi-seat locking does not result in partial locks.
* A booking cannot contain seats belonging to different shows.
* A booking cannot be created without valid seat locks.
* Successful payment confirms the booking.
* Successful payment marks seats as booked.
* Failed payment cancels the booking.
* Payment success and failure release temporary seat locks.
* Incorrect payment amounts are rejected.

⸻

Future Improvements

* Per-seat concurrency control using ReentrantLock
* Custom domain exceptions
* Idempotent payment processing using idempotency keys
* Dynamic pricing
* Pricing strategies
* Multiple payment methods and payment gateways
* Booking cancellation and refunds
* Notifications
* Seat recommendations
* Persistent database implementation
* REST APIs using Spring Boot