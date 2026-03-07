import request from '@/utils/request'

export function getInventoryLogPage(params) {
  return request({ url: '/inventory-logs', method: 'get', params })
}
