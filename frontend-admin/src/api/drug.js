import request from '@/utils/request'

export function getDrugPage(params) {
  return request({ url: '/drugs', method: 'get', params })
}

export function getDrugList() {
  return request({ url: '/drugs/list', method: 'get' })
}

export function getDrug(id) {
  return request({ url: `/drugs/${id}`, method: 'get' })
}

export function createDrug(data) {
  return request({ url: '/drugs', method: 'post', data })
}

export function updateDrug(id, data) {
  return request({ url: `/drugs/${id}`, method: 'put', data })
}

export function deleteDrug(id) {
  return request({ url: `/drugs/${id}`, method: 'delete' })
}
