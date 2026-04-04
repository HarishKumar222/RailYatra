import { create } from 'zustand'
const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0]

export const useSearchStore = create((set) => ({
  searchParams: { from: '', to: '', date: tomorrow, classType: '' },
  searchResults: [],
  isLoading: false,
  error: null,
  setSearchParams: (p) => set((s) => ({ searchParams: { ...s.searchParams, ...p } })),
  setSearchResults: (r) => set({ searchResults: r }),
  setLoading: (v) => set({ isLoading: v }),
  setError: (e) => set({ error: e }),
  clearResults: () => set({ searchResults: [], error: null })
}))
