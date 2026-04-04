import { create } from 'zustand'

export const useBookingStore = create((set) => ({
  selectedTrain: null,
  selectedClass: null,
  journeyDate: null,
  currentBooking: null,
  setSelectedTrain: (t) => set({ selectedTrain: t }),
  setSelectedClass: (c) => set({ selectedClass: c }),
  setJourneyDate: (d) => set({ journeyDate: d }),
  setCurrentBooking: (b) => set({ currentBooking: b }),
  clearBooking: () => set({ selectedTrain: null, selectedClass: null, journeyDate: null, currentBooking: null })
}))
