import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 发布商品草稿: 在 上传页 → 编辑页 之间传递图片URL与AI生成字段;
 * editId 非 null 表示编辑已有商品(来自商品管理).
 */
export const useDraftStore = defineStore('draft', () => {
  const images = ref([])
  const fields = ref({ title: '', brief: '', description: '', price: null, tags: [], aiGenerated: 0 })
  const editId = ref(null)

  function setDraft(imgs, flds) {
    images.value = imgs
    fields.value = { ...fields.value, ...flds }
  }

  function setEdit(id) {
    editId.value = id
  }

  function reset() {
    images.value = []
    fields.value = { title: '', brief: '', description: '', price: null, tags: [], aiGenerated: 0 }
    editId.value = null
  }

  return { images, fields, editId, setDraft, setEdit, reset }
})
