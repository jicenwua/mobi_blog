/** 与后端 RoleStatue 枚举对应 */
export const ROLE = {
  CUSTOMER: 'CUSTOMER',
  ADMIN: 'ADMIN',
  MASTER: 'MASTER',
}

export const ADMIN_ROLES = [ROLE.ADMIN, ROLE.MASTER]

export function isAdminRole(roles) {
  if (!roles?.length) return false
  return roles.some((role) => ADMIN_ROLES.includes(String(role).toUpperCase()))
}

export function isMasterRole(roles) {
  if (!roles?.length) return false
  return roles.some((role) => String(role).toUpperCase() === ROLE.MASTER)
}
