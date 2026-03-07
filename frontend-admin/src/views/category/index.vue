<template>
  <div class="category-page">
    <div class="table-card">
      <div class="table-header">
        <span class="table-title">药品分类管理</span>
        <el-button type="primary" @click="openDialog(null, null)">
          <el-icon><Plus /></el-icon>新增根分类
        </el-button>
      </div>

      <el-tree
        v-loading="loading"
        class="category-tree"
        :data="treeData"
        :props="{ label: 'name', children: 'children' }"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <span class="node-label">
              <el-icon class="node-icon" v-if="data.children?.length"><Folder /></el-icon>
              <el-icon class="node-icon leaf" v-else><Document /></el-icon>
              {{ data.name }}
            </span>
            <span class="node-actions">
              <el-button type="primary" link size="small" @click.stop="openDialog(null, data.id)">
                <el-icon><Plus /></el-icon>子分类
              </el-button>
              <el-button type="primary" link size="small" @click.stop="openDialog(data, data.parentId)">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-button type="danger" link size="small" @click.stop="handleDelete(data)">
                <el-icon><Delete /></el-icon>删除
              </el-button>
            </span>
          </div>
        </template>
      </el-tree>

      <el-empty v-if="!loading && treeData.length === 0" description="暂无分类数据" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="480px" destroy-on-close>
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="dialogForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="dialogForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCategoryTree, createCategory, updateCategory, deleteCategory } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const treeData = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const dialogFormRef = ref(null)
const editId = ref(null)

const dialogForm = reactive({
  name: '',
  parentId: null,
  sortOrder: 0
})

const dialogRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

onMounted(() => {
  fetchTree()
})

async function fetchTree() {
  loading.value = true
  try {
    const res = await getCategoryTree()
    treeData.value = res.data
  } finally {
    loading.value = false
  }
}

function openDialog(row, parentId) {
  isEdit.value = !!row
  editId.value = row?.id || null
  dialogForm.name = row?.name || ''
  dialogForm.sortOrder = row?.sortOrder || 0
  dialogForm.parentId = isEdit.value ? row.parentId : (parentId || null)
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await dialogFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateCategory(editId.value, { ...dialogForm })
      ElMessage.success('编辑成功')
    } else {
      await createCategory({ ...dialogForm })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchTree()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(data) {
  try {
    await ElMessageBox.confirm(`确定删除分类「${data.name}」吗？删除后其子分类也将被删除。`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCategory(data.id)
    ElMessage.success('删除成功')
    fetchTree()
  } catch {
    /* cancelled */
  }
}
</script>

<style lang="scss" scoped>
.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.category-tree {
  margin-top: 8px;
  padding: 8px 0;

  :deep(.el-tree-node) {
    margin-bottom: 4px;
  }

  :deep(.el-tree-node__content) {
    height: 48px;
    padding: 0 16px;
    border-radius: 8px;
    transition: all 0.2s ease;

    &:hover {
      background: #F5F7FA;
    }
  }

  :deep(.el-tree-node.is-current > .el-tree-node__content) {
    background: #FFF2E8;
    color: var(--primary-color);
  }

  :deep(.el-tree-node__expand-icon) {
    font-size: 16px;
    color: #8C8C8C;
  }
}

.tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 48px;
  padding: 0 8px 0 0;

  .node-label {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    color: #333;
    font-weight: 500;

    .node-icon {
      font-size: 18px;
      color: #FF7A45;
      flex-shrink: 0;

      &.leaf {
        color: #52C41A;
      }
    }
  }

  .node-actions {
    display: flex;
    align-items: center;
    gap: 4px;
    opacity: 0.85;

    .el-button {
      margin-left: 0;
      padding: 4px 8px;
      font-size: 13px;
    }

    .el-icon {
      margin-right: 2px;
      font-size: 14px;
    }
  }
}
</style>
