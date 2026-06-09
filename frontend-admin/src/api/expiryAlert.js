import request from '@/utils/request'

export function getExpiryAlertPage(params) {
  return request({ url: '/expiry-alerts', method: 'get', params })
}

export function getActiveExpiryAlerts() {
  return request({ url: '/expiry-alerts/active', method: 'get' })
}

export function scanExpiryAlerts() {
  return request({ url: '/expiry-alerts/scan', method: 'get' })
}

export function resolveExpiryAlert(id) {
  return request({ url: `/expiry-alerts/${id}/resolve`, method: 'put' })
}
