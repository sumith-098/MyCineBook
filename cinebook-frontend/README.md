# CineBook — React Frontend

Vite + React 19 + Tailwind v4 + react-router-dom v7. Talks to **one** base URL: the api-gateway
(`VITE_API_BASE_URL`, defaults to `http://localhost:8080`) — never the individual backend
services directly.

## Running it locally
```bash
npm install
cp .env.example .env
npm run dev
```
Requires all 7 backend services + the gateway running (see the backend README) for real data —
without them, pages will show their error/empty states, which is expected.

## What's built
**Customer-facing:**
- **Home** — featured movies, marquee-style hero
- **Movies** — search/browse all now-showing movies
- **Movie Detail** — poster, rating, description, date/showtime picker, reviews
- **Seat Picker** — interactive seat map, live server-priced booking summary (ticket-stub
  component), Pay at Counter or Pay Online (full Razorpay checkout integration)
- **Booking Confirmation** + **My Bookings** (cancel, review) — all as the ticket-stub design
- **Login** (customer/owner/admin tabs) and **Register** (customer + owner, with OTP step)

**Owner Dashboard** (`/owner`):
- Theaters — list + add
- Movies — list, add, hide/show, real poster photo upload, per-movie showtime management
- Screen Layout — seat category editor (name/price/color) + row builder (label/seat count/
  category) with a live seat-grid preview before saving, and the active-bookings conflict
  warning surfaced with a "save anyway" confirm step

**Admin Dashboard** (`/admin`):
- Overview — platform stats, recent bookings
- Owner Approvals — approve pending theater owner signups
- Settlements — commission rate config, per-owner earned/commission/paid/pending breakdown,
  mark-as-paid

Every one of these calls the real backend endpoints documented in the backend README — there's
no mock data anywhere in this app.


## Design system
Dark, violet-tinted near-black (not flat `#000`) with two cinema-specific accents — **marquee
gold** (signage) and **velvet red** (curtain) — rather than one arbitrary bright accent.
**Fraunces** (dramatic serif) for display/headlines, **Inter** for UI, **JetBrains Mono** for
anything that reads like it was printed on a ticket (booking refs, seat codes, prices). All
tokens live in `src/index.css` under `@theme`.

The signature element is the **ticket stub** (`src/components/TicketStub.jsx`) — a physically
accurate torn cinema ticket with a perforated dashed seam and punched notch circles, reused for
the booking summary, confirmation screen, and My Bookings list. Ratings render as a torn
ticket-corner badge on the poster rather than a generic star pill, tying the "is it good"
signal to the same ticket motif as everything else.

## Structure
```
src/
  api/        — one file per backend service (client.js has the axios instance + JWT/refresh interceptor)
  context/    — AuthContext (login/logout, token + role state)
  components/ — shared UI (Navbar, MovieCard, SeatGrid, TicketStub, RatingBadge, DashboardTabs, StatCard, ...)
  pages/      — customer-facing routes
  pages/owner/  — Owner Dashboard tabs
  pages/admin/  — Admin Dashboard tabs
```

## Register/design note
Owner and Admin dashboards intentionally look different from the customer-facing pages — dense
data tables, forms, and stat cards rather than the ticket-stub motif. That's a deliberate
register shift for a different job (getting work done vs. browsing/booking), not an
inconsistency — the dark palette and type system stay the same across both.

## This completes the full-stack rebuild
All 7 backend services, the api-gateway, and now the complete frontend (customer + owner +
admin) are built. If anything doesn't work end-to-end once you run it — mismatched field names,
a missing endpoint, a CORS issue — tell me what you're seeing and I'll fix it.

