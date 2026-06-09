import request from '@/utils/request'

export function getExpiryAlertPage(params) {
  return request({ url: '/expiry-alerts', method: 'get', params })
}

export function getActiveAlerts() {
  return request({ url: '/expiry-alerts/active', method: 'get' })
}

export function scanExpiryAlerts() {
  return request({ url: '/expiry-alerts', method: 'post' })
}

export function resolveAlert(id) {
  return request({ url: `/expiry-alerts/${id}/resolve`, method: 'put' })
}
