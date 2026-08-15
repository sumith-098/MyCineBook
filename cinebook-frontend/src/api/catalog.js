import api, { call } from './client'

export const catalogApi = {
  featuredMovies: () => call(api.get('/api/catalog/movies/featured')),
  searchMovies: (q) => call(api.get('/api/catalog/movies', { params: q ? { q } : {} })),
  movieDetail: (movieId) => call(api.get(`/api/catalog/movies/${movieId}`)),
  theaters: () => call(api.get('/api/catalog/theaters')),
  nearbyTheaters: (lat, lng) => call(api.get('/api/catalog/theaters/nearby', { params: { lat, lng } })),
  theatersMap: () => call(api.get('/api/catalog/theaters/map')),
  showtimeInfo: (showtimeId) => call(api.get(`/api/catalog/showtimes/${showtimeId}`)),

  // owner
  ownerDashboard: () => call(api.get('/api/catalog/owner/dashboard')),
  ownerTheaters: () => call(api.get('/api/catalog/owner/theaters')),
  addTheater: (payload) => call(api.post('/api/catalog/owner/theaters', payload)),
  ownerMovies: () => call(api.get('/api/catalog/owner/movies')),
  addMovie: (payload) => call(api.post('/api/catalog/owner/movies', payload)),
  toggleMovie: (movieId) => call(api.patch(`/api/catalog/owner/movies/${movieId}/toggle`)),
  uploadPoster: (movieId, file) => {
    const form = new FormData()
    form.append('file', file)
    return call(api.post(`/api/catalog/owner/movies/${movieId}/poster`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }))
  },
  ownerShowtimes: (movieId) => call(api.get(`/api/catalog/owner/movies/${movieId}/showtimes`)),
  addShowtime: (movieId, payload) => call(api.post(`/api/catalog/owner/movies/${movieId}/showtimes`, payload)),
  screenLayouts: (theaterId) => call(api.get(`/api/catalog/owner/theaters/${theaterId}/screen-layout`)),
  saveScreenLayout: (theaterId, payload) => call(api.put(`/api/catalog/owner/theaters/${theaterId}/screen-layout`, payload)),
}
