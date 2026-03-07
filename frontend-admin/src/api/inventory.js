import request from '@/utils/request'

export function getInventoryPage(params) {
  return request({ url: '/inventory', method: 'get', params })
}

export function getWarnings() {
  return request({ url: '/inventory/warnings', method: 'get' })
}

export function adjustInventory(data) {
  return request({ url: '/inventory/adjust', method: 'post', data })
}

export function getInventoryQuantity(drugId, hospitalId) {
  return request({ url: '/inventory/quantity', method: 'get', params: { drugId, hospitalId } })
}

export function getAvailableQuantity(drugId, hospitalId, excludeTransferId) {
  return request({ url: '/inventory/available', method: 'get', params: { drugId, hospitalId, excludeTransferId } })
}
