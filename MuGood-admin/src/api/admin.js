import http from './http'

export const loginAdminAPI = (data) => {
  return http.post('/admin/login', data)
}

export const getSummaryAPI = () => {
  return http.get('/admin/summary')
}

export const getGoodsPageAPI = (params) => {
  return http.get('/admin/goods', { params })
}

export const getGoodsDetailAPI = (id) => {
  return http.get(`/admin/goods/${id}`)
}

export const getAdminOptionsAPI = () => {
  return http.get('/admin/options')
}

export const createGoodsAPI = (data) => {
  return http.post('/admin/goods', data)
}

export const updateGoodsAPI = (id, data) => {
  return http.put(`/admin/goods/${id}`, data)
}

export const deleteGoodsAPI = (id) => {
  return http.delete(`/admin/goods/${id}`)
}

export const updateGoodsStatusAPI = (id, status) => {
  return http.put(`/admin/goods/${id}/status`, { status })
}

export const uploadImageAPI = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/admin/upload', formData)
}

export const getCategoryListAPI = () => {
  return http.get('/admin/categories')
}

export const createCategoryAPI = (data) => {
  return http.post('/admin/categories', data)
}

export const updateCategoryAPI = (id, data) => {
  return http.put(`/admin/categories/${id}`, data)
}

export const deleteCategoryAPI = (id) => {
  return http.delete(`/admin/categories/${id}`)
}

export const getOrderPageAPI = (params) => {
  return http.get('/admin/orders', { params })
}

export const updateOrderStateAPI = (id, orderState) => {
  return http.put(`/admin/orders/${id}/state`, { orderState })
}

export const getBannerListAPI = () => {
  return http.get('/admin/banners')
}

export const createBannerAPI = (data) => {
  return http.post('/admin/banners', data)
}

export const updateBannerAPI = (id, data) => {
  return http.put(`/admin/banners/${id}`, data)
}

export const deleteBannerAPI = (id) => {
  return http.delete(`/admin/banners/${id}`)
}

export const getUserPageAPI = (params) => {
  return http.get('/admin/users', { params })
}

export const updateUserStatusAPI = (id, status) => {
  return http.put(`/admin/users/${id}/status`, { status })
}

export const updateUserPasswordAPI = (id, password) => {
  return http.put(`/admin/users/${id}/password`, { password })
}
