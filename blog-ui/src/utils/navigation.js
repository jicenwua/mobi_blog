export function openArticleDetail(router, articleId) {
  if (!articleId) return
  const route = router.resolve({ name: 'ArticleDetail', params: { id: articleId } })
  window.open(route.href, '_blank')
}
