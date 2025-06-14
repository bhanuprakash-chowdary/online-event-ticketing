# OETP Scalability Requirements
## Functional
- List events: `GET /v1/events?page={n}&size={m}`—paginated, HATEOAS.
- Get event: `GET /v1/events/{id}`—details (name, tickets).
- Book tickets: `POST /v1/events/{id}/book`—quantity, OAuth2 user.
- Authentication: OAuth2 (Google)—fan logins.
## Non-Functional
- Traffic: 100K fans/min—90% reads (`GET`), 10% writes (`POST`).
- Latency: <100ms reads, <500ms writes.
- Availability: 99.99% (<53min downtime/year).
- Consistency: Eventual for reads, strong for writes.
- Spikes: Handle 10K bookings/min (e.g., concert drop).
- Scale: 1B events/year, global users.
- Cost: Minimize servers—cloud-ready (e.g., AWS).
## Constraints
- Current: 1K fans/min—single Spring app, Postgres, Redis.
- Target: 100K fans/min now, 1M later.
- Infra: Docker local, AWS for prod (future).
