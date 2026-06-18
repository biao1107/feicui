import request from '@/utils/request'

/** 上传商品图片, 返回 {url} */
export const uploadImage = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/merchant/products/upload-image', fd)
}

/** AI 图片转文案 */
export const aiGenerate = (images) =>
  request.post('/merchant/products/ai-generate', { images })

/** 创建草稿 */
export const createDraft = (data) => request.post('/merchant/products', data)

/** 编辑商品 */
export const updateProduct = (id, data) => request.put(`/merchant/products/${id}`, data)

/** 发布(草稿→上架) */
export const publishProduct = (id) => request.post(`/merchant/products/${id}/publish`)

/** 切换上下架 */
export const changeStatus = (id, targetStatus) =>
  request.put(`/merchant/products/${id}/status`, { targetStatus })

/** 删除商品 */
export const deleteProduct = (id) => request.delete(`/merchant/products/${id}`)

/** 商品管理列表 */
export const productList = (params) => request.get('/merchant/products', { params })

/** 商家自己的商品详情 */
export const productDetail = (id) => request.get(`/merchant/products/${id}`)

/** 游客商品详情 */
export const homeProductDetail = (id) => request.get(`/home/products/${id}`)
