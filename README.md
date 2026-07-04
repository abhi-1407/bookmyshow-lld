BookMyShow - Low Level Design

A simplified BookMyShow-like system designed to practice Low-Level Design (LLD), Object-Oriented Design, concurrency handling, and backend engineering concepts.

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

Non-Functional Requirements

1. The system should be highly available and provide low latency while browsing movies and shows.
2. The system should maintain strong consistency for seat-locking and booking operations to prevent double booking.
3. The system should correctly handle concurrent booking attempts for the same seat.
4. The design should be extensible to support additional seat types, pricing strategies, and payment methods.

Back-of-the-Envelope Estimation

* The system is expected to be read-heavy since browsing movies, shows, and seat availability occurs significantly more frequently than booking operations.
* Booking operations are less frequent but require strong consistency and correct concurrency handling.

Core Entities

To be identified during the design process.

Class Diagram

To be added after identifying the core entities and their relationships.

Design Decisions

To be documented as the design evolves.

Concurrency Handling

To be documented while designing the seat-locking and booking flow.

Design Patterns Used

To be documented as design patterns are introduced.

Future Improvements

To be added after completing the initial implementation.