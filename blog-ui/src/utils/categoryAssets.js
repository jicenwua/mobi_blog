import cover108755155 from '@image/108755155_p0_master1200.jpg'
import cover142078358 from '@image/142078358_p0_master1200.jpg'
import cover142112096 from '@image/142112096_p0_master1200.jpg'

import iconBingqilin from '@/assets/icon/bingqilin.svg'
import iconDangao from '@/assets/icon/dangao.svg'
import iconHanbao from '@/assets/icon/hanbao.svg'
import iconHuoguo from '@/assets/icon/huoguo.svg'
import iconJitui from '@/assets/icon/jitui.svg'
import iconShala from '@/assets/icon/shala.svg'
import iconShutiao from '@/assets/icon/shutiao.svg'

/** 分类封面图（固定映射，仅在增删分类时调整） */
export const CATEGORY_COVERS = {
  database: cover108755155,
  middleware: cover142078358,
  spring: cover142112096,
  ai: cover142112096,
}

/** 标签图标池（按名称哈希稳定取图） */
export const TAG_ICON_POOL = [
  iconHuoguo,
  iconHanbao,
  iconDangao,
  iconBingqilin,
  iconShala,
  iconShutiao,
  iconJitui,
]

export function getCategoryCover(code) {
  return CATEGORY_COVERS[code] || cover108755155
}

export function getTagIcon(tagName = '') {
  let hash = 0
  for (let i = 0; i < tagName.length; i += 1) {
    hash = tagName.charCodeAt(i) + ((hash << 5) - hash)
  }
  return TAG_ICON_POOL[Math.abs(hash) % TAG_ICON_POOL.length]
}

export function sortTags(tags = []) {
  return [...tags].sort((a, b) =>
    String(a.name).localeCompare(String(b.name), 'zh-CN'),
  )
}
