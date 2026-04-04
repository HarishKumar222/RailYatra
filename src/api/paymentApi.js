import api from './axiosInstance'
export const paymentApi = {
  createOrder: (bookingId) => api.post(`/payments/create-order?bookingId=${bookingId}`),
  verify:      (data)      => api.post('/payments/verify', data)
}
