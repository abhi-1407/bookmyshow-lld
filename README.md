# BookMyShow - Low Level Design

A simplified BookMyShow-like system designed to practice Low-Level Design (LLD), Object-Oriented Design, concurrency handling, and backend engineering concepts.

---

## Functional Requirements

1. A user should be able to select a city.
2. A user should be able to browse movies and shows available in the selected city.
3. A user should be able to view available seats for a selected show.
4. A user should be able to select and temporarily lock seats for a maximum of 5 minutes.
5. The system should prevent other users from booking or locking seats that are already locked by another user.
6. A user should be able to make a payment for the selected seats.
7. On successful payment, the booking should be confirmed.
8. On payment failure or seat-lock expiration, the locked seats should be released.
9. A user should receive a confirmation once the booking is successfully completed.

---

## Non-Functional Requirements

1. The system should be highly available and provide low latency while browsing movies and shows.
2. The system should maintain strong consistency for seat-locking and booking operations to prevent double booking.
3. The system should correctly handle concurrent booking attempts for the same seat.
4. The design should be extensible to support additional seat types, pricing strategies, and payment methods.

---

## Back-of-the-Envelope Estimation

- Read operations are expected to be significantly higher than write operations.
- Browsing movies, shows, and seat availability is read-heavy.
- Booking operations require strong consistency and concurrency control.

---

## Domain Model

### Entities

- User
- City
- Theatre
- Screen
- Movie
- Show
- Seat
- ShowSeat
- SeatLock
- Booking
- Payment

### Enums

- SeatType
- SeatStatus
- BookingStatus
- PaymentStatus

---

## Entity Relationships

```
City
 └── Theatre
      └── Screen
            ├── Seat
            └── Show
                  ├── Movie
                  └── ShowSeat
                           │
                           └── Seat

User
 ├── Booking
 │      ├── Show
 │      └── ShowSeats
 │
 └── SeatLock

Booking
 └── Payment(s)
```

---

## Important Design Decisions

### Show vs Movie

A `Movie` contains immutable movie metadata.

A `Show` represents a scheduled screening of a movie at a particular screen and time.

### Seat vs ShowSeat

`Seat` represents the physical seat inside a screen.

`ShowSeat` represents the availability and pricing of a seat for a specific show.

This allows the same physical seat to have different states across different shows.

### Seat Locking

Seat locking is modeled as a separate entity (`SeatLock`) instead of embedding lock information inside `ShowSeat`.

This allows lock lifecycle management, expiration handling, and improves separation of concerns.

### Booking

A booking represents a booking transaction.

A booking can contain multiple seats, but all seats must belong to the same show.

### Atomic Seat Locking

The current SeatLockService uses method-level synchronization to prevent concurrent threads from executing the seat-locking operation simultaneously.

However, locking multiple seats is currently not atomic. For example, if a user attempts to lock seats A1, A2, and A3, and A3 is already locked, A1 and A2 may have already been locked before the operation fails.

This can leave the system in a partially updated state.

The implementation will be improved to provide all-or-nothing seat locking and finer-grained concurrency control using per-seat locks.

---

## Class Diagram

_To be added._

---

## Repository Layer

_To be added._

---

## Service Layer

_To be added._

---

## Concurrency Handling

_To be added._

---

## Design Patterns Used

_To be added._

---

## Future Improvements

- Dynamic pricing
- Coupon support
- Multiple payment gateways
- Seat recommendations
- Booking cancellation and refunds
- Notifications