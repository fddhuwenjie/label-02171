import request from '@/utils/request'

export function getExpiryAlertPage(params) {
  return request({ url: '/expiry-alerts', method: 'get', params })
}

export function getActiveExpiryAlerts(limit = 20) {
  return request({ url: '/expiry-alerts/active', method: 'get', params: { limit } })
}

export function getExpiryAlertCount() {
  return request({ url: '/expiry-alerts/count', method: 'get' })
}

export function resolveExpiryAlert(id) {
  return request({ url: `/expiry-alerts/${id}/resolve`, method: 'put' })
}

export function triggerExpiryScan() {
  return request({ url: '/expiry-alerts/scan', method: 'put' })
}
