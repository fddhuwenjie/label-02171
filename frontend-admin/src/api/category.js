import request from '@/utils/request'

export function getCategoryTree() {
  return request({ url: '/drug-categories/tree', method: 'get' })
}

export function createCategory(data) {
  return request({ url: '/drug-categories', method: 'post', data })
}

export function updateCategory(id, data) {
  return request({ url: `/drug-categories/${id}`, method: 'put', data })
}

export function deleteCategory(id) {
  return request({ url: `/drug-categories/${id}`, method: 'delete' })
}
