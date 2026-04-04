import api from './axiosInstance'
export const trainApi = {
  search:      (params) => api.get('/trains/search', { params }),
  getById:     (id)     => api.get(`/trains/${id}`),
  getStations: ()       => api.get('/trains/stations'),
  getPopular:  ()       => api.get('/trains/popular')
}
