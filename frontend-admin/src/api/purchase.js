import request from '@/utils/request'

export function getPurchaseOrderPage(params) {
  return request({ url: '/purchase-orders', method: 'get', params })
}

export function getPurchaseOrder(id) {
  return request({ url: `/purchase-orders/${id}`, method: 'get' })
}

export function createPurchaseOrder(data) {
  return request({ url: '/purchase-orders', method: 'post', data })
}

export function approvePurchaseOrder(id) {
  return request({ url: `/purchase-orders/${id}/approve`, method: 'put' })
}

export function receivePurchaseOrder(id) {
  return request({ url: `/purchase-orders/${id}/receive`, method: 'put' })
}

export function deletePurchaseOrder(id) {
  return request({ url: `/purchase-orders/${id}`, method: 'delete' })
}
