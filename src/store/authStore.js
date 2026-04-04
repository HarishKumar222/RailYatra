import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export const useAuthStore = create(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      login: (userData, token) => {
        localStorage.setItem('ry_token', token)
        set({ user: userData, token, isAuthenticated: true })
      },
      logout: () => {
        localStorage.removeItem('ry_token')
        set({ user: null, token: null, isAuthenticated: false })
      },
      updateUser: (data) => set((s) => ({ user: { ...s.user, ...data } }))
    }),
    { name: 'ry_auth' }
  )
)
