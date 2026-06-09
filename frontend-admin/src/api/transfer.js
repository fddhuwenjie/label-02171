import request from '@/utils/request'

export function getTransferPage(params) {
  return request({ url: '/transfers', method: 'get', params })
}

export function getTransfer(id) {
  return request({ url: `/transfers/${id}`, method: 'get' })
}

export function createTransfer(data) {
  return request({ url: '/transfers', method: 'post', data })
}

export function approveTransfer(id) {
  return request({ url: `/transfers/${id}/approve`, method: 'put' })
}

export function rejectTransfer(id, rejectReason) {
  return request({ url: `/transfers/${id}/reject`, method: 'put', data: { rejectReason } })
}

export function shipTransfer(id) {
  return request({ url: `/transfers/${id}/ship`, method: 'put' })
}

export function completeTransfer(id) {
  return request({ url: `/transfers/${id}/complete`, method: 'put' })
}
