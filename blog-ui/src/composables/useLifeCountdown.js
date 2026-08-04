import { computed, onMounted, onUnmounted, ref } from 'vue'

export function useLifeCountdown() {
  const now = ref(new Date())
  let timer = null

  onMounted(() => {
    timer = setInterval(() => {
      now.value = new Date()
    }, 1000)
  })

  onUnmounted(() => {
    if (timer) clearInterval(timer)
  })

  const todayProgress = computed(() => {
    const current = now.value
    const passed = current.getHours() * 3600 + current.getMinutes() * 60 + current.getSeconds()
    return Math.min(100, Math.round((passed / 86400) * 100))
  })

  const weekProgress = computed(() => {
    const current = now.value
    const dayIndex = (current.getDay() + 6) % 7
    const passed = dayIndex * 86400 + current.getHours() * 3600 + current.getMinutes() * 60 + current.getSeconds()
    return Math.min(100, Math.round((passed / (7 * 86400)) * 100))
  })

  const monthProgress = computed(() => {
    const current = now.value
    const daysInMonth = new Date(current.getFullYear(), current.getMonth() + 1, 0).getDate()
    const passedDays = current.getDate() - 1
    const passed = passedDays * 86400 + current.getHours() * 3600 + current.getMinutes() * 60 + current.getSeconds()
    return Math.min(100, Math.round((passed / (daysInMonth * 86400)) * 100))
  })

  const yearProgress = computed(() => {
    const current = now.value
    const start = new Date(current.getFullYear(), 0, 1)
    const end = new Date(current.getFullYear() + 1, 0, 1)
    const total = (end - start) / 1000
    const passed = (current - start) / 1000
    return Math.min(100, Math.round((passed / total) * 100))
  })

  const todayElapsedHours = computed(() => now.value.getHours())

  const weekElapsedDays = computed(() => (now.value.getDay() + 6) % 7 + 1)

  const monthElapsedDays = computed(() => now.value.getDate())

  const yearElapsedMonths = computed(() => now.value.getMonth() + 1)

  return {
    todayProgress,
    weekProgress,
    monthProgress,
    yearProgress,
    todayElapsedHours,
    weekElapsedDays,
    monthElapsedDays,
    yearElapsedMonths,
  }
}
