export const DEFAULT_FOLDER_ID = 0
export const DEFAULT_FOLDER_NAME = '默认收藏夹'

export function isDefaultFolder(folder) {
  if (!folder) return false
  return Boolean(
    folder.defaultFolder
    || folder.favoriteId === DEFAULT_FOLDER_ID
    || folder.favoriteName === DEFAULT_FOLDER_NAME,
  )
}

export function sortFavoriteFolders(folders = []) {
  return [...folders].sort((a, b) => {
    if (isDefaultFolder(a)) return -1
    if (isDefaultFolder(b)) return 1
    return String(a.favoriteName).localeCompare(String(b.favoriteName), 'zh-CN')
  })
}

/** 去重：默认收藏夹只保留一个（兼容旧数据与虚拟默认夹） */
export function dedupeFavoriteFolders(folders = []) {
  const result = []
  let hasDefault = false
  const seenIds = new Set()

  for (const folder of folders) {
    if (isDefaultFolder(folder)) {
      if (hasDefault) continue
      hasDefault = true
      result.push(folder)
      continue
    }

    const id = folder.favoriteId
    if (id != null && seenIds.has(id)) continue
    if (id != null) seenIds.add(id)
    result.push(folder)
  }

  return sortFavoriteFolders(result)
}

export function folderKey(folder) {
  if (isDefaultFolder(folder)) {
    return `default-${folder.favoriteId ?? DEFAULT_FOLDER_ID}`
  }
  return String(folder.favoriteId)
}
