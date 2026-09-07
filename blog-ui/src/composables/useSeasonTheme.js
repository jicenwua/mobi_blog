import { ref, watch } from 'vue'

const STORAGE_KEY = 'blog-season-theme'

const SEASONS = [
  { key: 'spring', label: '春', color: '#ffb6c1' },
  { key: 'summer', label: '夏', color: '#67c23a' },
  { key: 'autumn', label: '秋', color: '#ffb347' },
  { key: 'winter', label: '冬', color: '#64b5f6' },
]

const SEASON_CLASSES = SEASONS.map((s) => `season-theme--${s.key}`)
const DEFAULT_SEASON = 'spring'

function resolveInitialSeason() {
  return localStorage.getItem(STORAGE_KEY) ?? DEFAULT_SEASON
}

const activeSeason = ref(resolveInitialSeason())

function applySeasonClass(season) {
  const el = document.documentElement
  SEASON_CLASSES.forEach((cls) => el.classList.remove(cls))
  if (season) {
    el.classList.add(`season-theme--${season}`)
  }
}

watch(activeSeason, (value) => {
  if (value) {
    localStorage.setItem(STORAGE_KEY, value)
  } else {
    localStorage.removeItem(STORAGE_KEY)
  }
  applySeasonClass(value)
}, { immediate: true })

function selectSeason(key) {
  activeSeason.value = activeSeason.value === key ? '' : key
}

export function useSeasonTheme() {
  return { SEASONS, activeSeason, selectSeason }
}
