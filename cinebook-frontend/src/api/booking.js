import api, { call } from './client'

export const bookingApi = {
  bookedSeats: (showtimeId) => call(api.get('/api/bookings/booked-seats', { params: { showtimeId } })),
  quote: (payload) => call(api.post('/api/bookings/quote', payload)),
  // book: (payload) => call(api.post('/api/bookings', payload)),
  myBookings: () => call(api.get('/api/bookings/my')),
  bookingDetail: (bookingId) => call(api.get(`/api/bookings/${bookingId}`)),
  cancelBooking: (bookingId) => call(api.post(`/api/bookings/${bookingId}/cancel`)),
  addReview: (bookingId, payload) => call(api.post(`/api/bookings/${bookingId}/reviews`, payload)),
  movieReviews: (movieId) => call(api.get(`/api/bookings/movies/${movieId}/reviews`)),
}
