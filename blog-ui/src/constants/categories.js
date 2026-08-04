/** 与后端 ArticleCategory 枚举对应 */
export const ARTICLE_CATEGORIES = [
  { code: 'database', label: '数据库' },
  { code: 'middleware', label: '中间件' },
  { code: 'spring', label: 'Spring框架' },
  { code: 'ai', label: 'AI' },
]

export function getCategoryLabel(code) {
  return ARTICLE_CATEGORIES.find((item) => item.code === code)?.label || code
}
