export function formatDate(dateStr) {
  if (!dateStr) return ''
  return String(dateStr).slice(0, 10)
}

export function formatDateTime(dateStr) {
  if (!dateStr) return ''
  const str = String(dateStr).replace('T', ' ')
  return str.length >= 16 ? str.slice(0, 16) : str.slice(0, 10)
}

export function formatMonthDay(dateStr) {
  if (!dateStr) return ''
  const str = String(dateStr)
  const month = str.slice(5, 7)
  const day = str.slice(8, 10)
  if (!month || !day) return ''
  return `${month}/${day}`
}

export function countWords(text = '') {
  if (!text) return 0
  const plain = String(text)
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/`[^`]*`/g, ' ')
    .replace(/!\[[^\]]*\]\([^)]*\)/g, ' ')
    .replace(/\[[^\]]*\]\([^)]*\)/g, ' ')
    .replace(/[#>*_~\-|]/g, ' ')
    .replace(/\s+/g, '')
  return plain.length
}

export function pickCoverColor(seed = '') {
  const colors = ['#ff6600', '#409eff', '#67c23a', '#e6a23c', '#909399', '#f56c6c']
  let hash = 0
  for (let i = 0; i < seed.length; i += 1) {
    hash = seed.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}

export function collectTags(articles, limit = 12) {
  const tagMap = new Map()
  articles.forEach((article) => {
    ;(article.tags || []).forEach((tag) => {
      tagMap.set(tag, (tagMap.get(tag) || 0) + 1)
    })
  })
  return [...tagMap.entries()]
    .sort((a, b) => b[1] - a[1])
    .slice(0, limit)
    .map(([name, count]) => ({ name, count }))
}
