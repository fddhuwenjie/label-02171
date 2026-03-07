import request from '@/utils/request'

export function getHospitalPage(params) {
  return request({ url: '/hospitals', method: 'get', params })
}

export function getHospitalList() {
  return request({ url: '/hospitals/list', method: 'get' })
}

export function getHospital(id) {
  return request({ url: `/hospitals/${id}`, method: 'get' })
}

export function createHospital(data) {
  return request({ url: '/hospitals', method: 'post', data })
}

export function updateHospital(id, data) {
  return request({ url: `/hospitals/${id}`, method: 'put', data })
}

export function deleteHospital(id) {
  return request({ url: `/hospitals/${id}`, method: 'delete' })
}
