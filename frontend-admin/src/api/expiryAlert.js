import request from '@/utils/request'

export function getExpiryAlertPage(params) {
  return request({ url: '/expiry-alerts', method: 'get', params })
}

export function getRecentExpiryAlerts(limit = 10) {
  return request({ url: '/expiry-alerts/recent', method: 'get', params: { limit } })
}

export function triggerExpiryScan() {
  return request({ url: '/expiry-alerts/scan', method: 'post' })
}
