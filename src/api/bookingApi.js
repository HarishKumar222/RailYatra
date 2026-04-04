import api from './axiosInstance'
export const bookingApi = {
  create:         (data) => api.post('/bookings', data),
  getMyBookings:  ()     => api.get('/bookings/my'),
  getById:        (id)   => api.get(`/bookings/${id}`),
  cancel:         (id)   => api.delete(`/bookings/${id}/cancel`),
  getPNR:         (pnr)  => api.get(`/bookings/pnr/${pnr}`),
  downloadTicket: (id)   => api.get(`/bookings/${id}/ticket`, { responseType: 'blob' }),
  predictWL:      (p)    => api.get('/bookings/wl-predict', { params: p })
}
