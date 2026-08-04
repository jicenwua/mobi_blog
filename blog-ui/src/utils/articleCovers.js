const imageModules = import.meta.glob('@image/*.{jpg,jpeg,png,webp,gif}', {
  eager: true,
  import: 'default',
})

/** @image 目录下的全部封面图 */
export const ARTICLE_COVER_POOL = Object.values(imageModules)

function hashSeed(seed = '') {
  let hash = 0
  for (let i = 0; i < seed.length; i += 1) {
    hash = seed.charCodeAt(i) + ((hash << 5) - hash)
  }
  return Math.abs(hash)
}

/** 根据文章标识从 @image 中稳定随机取一张封面 */
export function getArticleCover(seed = '') {
  if (!ARTICLE_COVER_POOL.length) return ''
  const index = hashSeed(seed) % ARTICLE_COVER_POOL.length
  return ARTICLE_COVER_POOL[index]
}

/** 从 @image 中随机取一张封面 */
export function getRandomArticleCover() {
  if (!ARTICLE_COVER_POOL.length) return ''
  return ARTICLE_COVER_POOL[Math.floor(Math.random() * ARTICLE_COVER_POOL.length)]
}
