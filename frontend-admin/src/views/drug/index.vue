<template>
  <div class="drug-page">
    <div class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="药品名称/编码" clearable @keyup.enter="handleSearch" style="width: 220px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-tree-select
            v-model="query.categoryId"
            :data="categoryTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="全部分类"
            clearable
            check-strictly
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 220px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-header">
        <span class="table-title">药品列表</span>
        <el-button type="primary" @click="openDialog(null)">
          <el-icon><Plus /></el-icon>新增药品
        </el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="name" label="药品名称" min-width="140" />
        <el-table-column prop="code" label="药品编码" width="120" />
        <el-table-column prop="genericName" label="通用名" min-width="120" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="manufacturer" label="生产厂商" min-width="140" show-overflow-tooltip />
        <el-table-column prop="spec" label="规格" width="100" />
        <el-table-column prop="unit" label="单位" width="60" />
        <el-table-column prop="price" label="单价(元)" width="90">
          <template #default="{ row }">
            {{ row.price != null ? `¥${Number(row.price).toFixed(2)}` : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑药品' : '新增药品'" width="650px" destroy-on-close>
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="药品名称" prop="name">
              <el-input v-model="dialogForm.name" placeholder="请输入药品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="药品编码" prop="code">
              <el-input v-model="dialogForm.code" placeholder="请输入药品编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="通用名" prop="genericName">
              <el-input v-model="dialogForm.genericName" placeholder="请输入通用名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="药品分类" prop="categoryId">
              <el-tree-select
                v-model="dialogForm.categoryId"
                :data="categoryTree"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                placeholder="请选择分类"
                clearable
                check-strictly
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="规格" prop="spec">
              <el-input v-model="dialogForm.spec" placeholder="请输入规格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="dialogForm.unit" placeholder="如：盒、瓶、支" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="单价" prop="price">
              <el-input-number v-model="dialogForm.price" :precision="2" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="dialogForm.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="生产厂商" prop="manufacturer">
          <el-input v-model="dialogForm.manufacturer" placeholder="请输入生产厂商" />
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
import { getDrugPage, createDrug, updateDrug, deleteDrug } from '@/api/drug'
import { getCategoryTree } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const categoryTree = ref([])

const query = reactive({ keyword: '', categoryId: '', status: '', page: 1, size: 10 })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const dialogFormRef = ref(null)
const editId = ref(null)

const dialogForm = reactive({
  name: '',
  code: '',
  genericName: '',
  categoryId: null,
  manufacturer: '',
  spec: '',
  unit: '',
  price: 0,
  status: 1
})

const dialogRules = {
  name: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入药品编码', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择药品分类', trigger: 'change' }]
}

onMounted(() => {
  fetchData()
  fetchCategoryTree()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getDrugPage(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function fetchCategoryTree() {
  try {
    const res = await getCategoryTree()
    categoryTree.value = res.data
  } catch {
    /* handled */
  }
}

function handleSearch() {
  query.page = 1
  fetchData()
}

function handleReset() {
  query.keyword = ''
  query.categoryId = ''
  query.status = ''
  query.page = 1
  fetchData()
}

function openDialog(row) {
  isEdit.value = !!row
  editId.value = row?.id || null
  if (row) {
    Object.assign(dialogForm, {
      name: row.name,
      code: row.code,
      genericName: row.genericName,
      categoryId: row.categoryId,
      manufacturer: row.manufacturer,
      spec: row.spec,
      unit: row.unit,
      price: row.price,
      status: row.status
    })
  } else {
    Object.assign(dialogForm, {
      name: '', code: '', genericName: '', categoryId: null,
      manufacturer: '', spec: '', unit: '', price: 0, status: 1
    })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await dialogFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateDrug(editId.value, { ...dialogForm })
      ElMessage.success('编辑成功')
    } else {
      await createDrug({ ...dialogForm })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除药品「${row.name}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteDrug(row.id)
    ElMessage.success('删除成功')
    fetchData()
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
</style>
